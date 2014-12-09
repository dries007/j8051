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
 */

package net.dries007.j8051;

import net.dries007.j8051.compiler.Parser;
import net.dries007.j8051.gui.MainGui;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.io.File;

import static net.dries007.j8051.util.Constants.*;

/**
 * @author Dries007
 */
public class Main
{
    public static File srcFile;
    public static File includeFile;
    private static boolean enablegui = true;

    public static void main(String[] args) throws Exception
    {
        parseArgs(args);

        if (srcFile == null)
        {
            if (PROPERTIES.containsKey(SRC_FILE)) Main.setSrcFile(new File(PROPERTIES.getProperty(SRC_FILE)));
        }

        if (enablegui)
        {
            try
            {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
            catch (Exception e)
            {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            }
            MainGui.MAIN_GUI.init();
        }
        else
        {
            if (Main.srcFile == null || !Main.srcFile.exists())
            {
                System.out.println("No source file give. Set via argument 'file'.");
                System.exit(1);
            }
            System.out.println(" -=- File used -=- ");
            System.out.println(Main.srcFile.getAbsolutePath());
            Parser parser = new Parser(FileUtils.readFileToString(Main.srcFile, PROPERTIES.getProperty(ENCODING, ENCODING_DEFAULT)));
            while (parser.hasWork()) parser.doWork();
            System.out.println(" -=- Done -=- ");
        }
    }

    private static void parseArgs(String[] args)
    {
        for (int i = 0; i < args.length; i++)
        {
            switch (args[i].toLowerCase())
            {
                case "nogui":
                    enablegui = false;
                    break;
                case "file":
                    i++;
                    if (i < args.length)
                    {
                        setSrcFile(new File(args[i]));
                    }
                    else throw new RuntimeException("File expected as argument after 'file'.");
            }
        }
    }

    public static void setSrcFile(File srcFile)
    {
        if (!srcFile.exists()) srcFile = null;
        Main.srcFile = srcFile;
        PROPERTIES.setProperty(SRC_FILE, srcFile == null ? "" : srcFile.getAbsolutePath());
        if (includeFile == null) includeFile = Main.srcFile.getParentFile();
        saveProperties();
    }

    public static void setIncludeFolder(File includeFile)
    {
        if (!includeFile.exists() || !includeFile.isDirectory()) includeFile = null;
        Main.includeFile = includeFile;
        PROPERTIES.setProperty(INCLUDEDIR, includeFile == null ? "" : includeFile.getAbsolutePath());
        saveProperties();
    }
}
