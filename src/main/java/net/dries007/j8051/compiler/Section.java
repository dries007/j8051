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

package net.dries007.j8051.compiler;

import net.dries007.j8051.compiler.components.Component;
import net.dries007.j8051.util.exceptions.CompileException;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * @author Dries007
 */
public class Section
{
    private final ArrayList<Integer> hexList = new ArrayList<>();
    public final Integer startaddress;

    public Section(Integer startaddress)
    {
        this.startaddress = startaddress;
    }

    public void addToHexFile(LinkedList<String> lines) throws CompileException
    {
        for (int i = 0; i <= hexList.size() / 0x20; i++)
        {
            final int length = Math.min(0x20, hexList.size() - 0x20 * i);
            final int address = (0x20 * i);
            int sum = length + (address & 0xFF) + (address >>> 8);
            StringBuilder line = new StringBuilder(75); // 75 = normal line length
            line.append(String.format(":%02X%04X00", length, startaddress + address));
            for (int j = 0; j < length; j++)
            {
                if (hexList.get(address + j) > 0xFF) throw new CompileException("One byte can't be more then 0xFF.");
                sum += hexList.get(address + j);
                line.append(String.format("%02X", hexList.get(address + j)));
            }
            line.append(String.format("%02X", ((~sum) + 1) & 0xFF));
            lines.add(line.toString());
        }
    }

    public void addData(Component component)
    {
        for (int b : component.getData()) hexList.add(b);
    }

    public void addToHexTable(LinkedList<String[]> data)
    {
        int offset = startaddress / 16;
        int global = 0;
        for (int i = 0; i <= hexList.size() / 16; i++)
        {
            String[] line = new String[17];
            line[0] = String.format("0x%02X - 0x%2X", offset + i, offset + i + 16);
            for (int j = 0; j < 16; j++)
            {
                if (i == 0 && j < startaddress % 16) continue;
                if (hexList.size() == global) break;
                line[1 + j] = String.format("%02X", hexList.get(global++));
            }
            data.add(line);
        }
    }

    public int getSize()
    {
        return hexList.size();
    }
}
