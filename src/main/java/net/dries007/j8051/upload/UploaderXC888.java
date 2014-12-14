/*
 * Copyright (c) 2014, Dries007
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *  Neither the name of the project nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package net.dries007.j8051.upload;

import gnu.io.NRSerialPort;
import net.dries007.j8051.Main;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import java.io.*;
import java.util.List;

import static net.dries007.j8051.util.Constants.*;

/**
 * @author Dries007
 */
public class UploaderXC888 extends Uploader
{
    public static final int INIT = 0x80;

    public static final int TYPE_HEADER = 0x00;
    public static final int TYPE_DATA   = 0x01;
    public static final int TYPE_EOT    = 0x02;

    public static final int MODE_DL_XRAM       = 0x00;
    public static final int MODE_EX_XRAM       = 0x01;
    public static final int MODE_DL_FLASH      = 0x02;
    public static final int MODE_EX_FLASH      = 0x03;
    public static final int MODE_ERASE_FLASH   = 0x04;
    public static final int MODE_PROTECT_FLASH = 0x06;
    public static final int MODE_READ_FLASH    = 0x12;

    public static final int OPTION_ERASE_ALL = 0xC0;

    public static final int RESPONSE_OK           = 0x55;
    public static final int RESPONSE_CS_ERROR     = 0xFE;
    public static final int RESPONSE_FLASH_LOCKED = 0xFD;
    public static final int RESPONSE_VERIFY_ERROR = 0xFC;
    public static final int RESPONSE_BLOCK_ERROR  = 0xFF;

    public static final int PAGE_SIZE = 0x40; // 64 bytes
    //public static final int PAGE_SIZE = 0x20; // 32 bytes

    public void upload(String comPort, Integer baudRate, ProgressMonitor pm) throws Exception
    {
        NRSerialPort serial = new NRSerialPort(comPort, baudRate);
        if (!serial.connect()) throw new IOException("COM port did not connect.");
        Page[] pages = parseFile();
        int count = 0;
        for (Page page : pages) if (page != null) count++;
        pm.setMaximum(count);
        try
        {
            InputStream in = serial.getInputStream();
            OutputStream out = serial.getOutputStream();
            sendData(in, out, false, INIT); // Init communication
            sendData(in, out, true, TYPE_HEADER, MODE_ERASE_FLASH, 0x00, 0x00, 0x00, 0x00, OPTION_ERASE_ALL); // Clear all flash

            for (int pageNr = 0; pageNr < pages.length; pageNr++)
            {
                Page page = pages[pageNr];
                int address = pageNr * PAGE_SIZE;
                pm.setNote(String.format("Uploading 0x%04X -> 0x%04X", address, address + PAGE_SIZE));
                if (page != null)
                {
                    sendData(in, out, true, TYPE_HEADER, MODE_DL_FLASH, address >>> 8, address & 0xFF, PAGE_SIZE + 3, 0x00, 0x00);
                    sendData(in, out, true, page.data);
                }
                pm.setProgress(pageNr);
            }
            sendData(in, out, true, TYPE_HEADER, MODE_EX_FLASH, 0x00, 0x00, 0x00, 0x00, 0x00); // Execute from flash
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        finally
        {
            serial.disconnect();
        }
    }

    private Page[] parseFile() throws Exception
    {
        Page[] pages = new Page[0xFFFF / PAGE_SIZE];

        File hexFile = new File(Main.srcFile.getParentFile(), FilenameUtils.getBaseName(Main.srcFile.getName()) + ".hex");
        if (!hexFile.exists()) throw new FileNotFoundException(hexFile.getAbsolutePath());
        //noinspection unchecked
        for (String line : (List<String>) FileUtils.readLines(hexFile, PROPERTIES.getProperty(ENCODING, ENCODING_DEFAULT)))
        {
            if (line.length() == 0 || line.charAt(0) != ':') continue;
            int type = Integer.parseInt(line.substring(7, 9), 16);
            if (type != 0) break;
            int address = Integer.parseInt(line.substring(3, 7), 16);
            int pageNr = address / PAGE_SIZE;
            if (pages[pageNr] == null) pages[pageNr] = new Page();
            Page page = pages[pageNr];
            page.parse(address, Integer.parseInt(line.substring(1, 3), 16), line);
        }

        return pages;
    }

    private void sendData(InputStream ins, OutputStream outs, boolean sendCs, int... data) throws IOException, InterruptedException
    {
        int cs = 0x00;
        for (int bt : data)
        {
            outs.write(bt);
            cs ^= bt;
        }
        //System.out.printf("Send %s", Helper.toHexString(data)); // todo: debug
        if (sendCs)
        {
            outs.write(cs);
            //System.out.printf(" with CS: 0x%02X", cs); // todo: debug
        }
        //System.out.println();
        outs.flush();
        int timeout = 10;
        while ((cs = ins.read()) == -1)
        {
            Thread.sleep(1); // Because some operations take a bit of time.
            timeout--;
            if (timeout == 0) throw new IOException("Timed out while waiting for a response!");
        }
        switch (cs)
        {
            case RESPONSE_OK:
                return;
            case RESPONSE_CS_ERROR:
                throw new IOException("Checksum Error");
            case RESPONSE_FLASH_LOCKED:
                throw new IOException("Flash Locked");
            case RESPONSE_VERIFY_ERROR:
                throw new IOException("Verify Error");
            case RESPONSE_BLOCK_ERROR:
                throw new IOException("Block Error");
            default:
                throw new IOException(String.format("Unknown response: 0x%02X", cs));
        }
    }

    private static class Page
    {
        int[] data = new int[PAGE_SIZE + 2];

        public Page()
        {
            data[0] = TYPE_EOT;
            data[1] = 0x20;
        }

        public void parse(int address, int length, String line) throws Exception
        {
            int sum = length + (address & 0xFF) + (address >>> 8);
            address %= PAGE_SIZE;
            for (int i = 0; i < length; i++)
            {
                int bt = Integer.parseInt(line.substring(9 + 2 * i, 9 + 2 * i + 2), 16);
                data[address + i + 2] = bt;
                sum += bt;
            }
            if (Integer.parseInt(line.substring(9 + 2 * length, 9 + 2 * length + 2), 16) != (((~sum) + 1) & 0xFF)) throw new Exception("Checksum error in hex file!");
        }
    }

    @Override
    public String toString()
    {
        return "XC888";
    }
}
