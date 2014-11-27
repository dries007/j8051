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

package net.dries007.j8051.util;

import net.dries007.j8051.Main;
import org.apache.commons.io.FilenameUtils;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * @author Dries007
 */
public class Constants
{
    /*
     * Strings
     */
    public static final String SETTINGS_FILENAME  = "./j8051.properties";
    public static final String SETTINGS_COMMENT   = "Settings file for j8051";
    public static final String SRC_FILE           = "srcFile";
    public static final String WINDOW_H           = "window.h";
    public static final String WINDOW_W           = "window.w";
    public static final String WINDOW_X           = "window.x";
    public static final String WINDOW_Y           = "window.y";
    public static final String AUTO_LOAD          = "auto.load";
    public static final String AUTO_COMPILE       = "auto.compile";
    public static final String AUTO_SAVE          = "auto.save";
    public static final String ENCODING           = "encoding";
    public static final String FONT_NAME          = "font.name";
    public static final String FONT_STYLE         = "font.style";
    public static final String FONT_SIZE          = "font.size";
    public static final String TABSIZE            = "tabSize";
    public static final String INCLUDEDIR         = "includedir";
    /*
     * Prefixes
     */
    public static final char   PREFIX_PRECOMPILER = '#';
    public static final char   PREFIX_COMMENT     = ';';

    /*
     * Regex
     */
    public static final Pattern INCLUDE_A = Pattern.compile("^#include \"(.*)\"$", CASE_INSENSITIVE);
    public static final Pattern INCLUDE_R = Pattern.compile("^#include <(.*)>$", CASE_INSENSITIVE);
    public static final Pattern DEFINE    = Pattern.compile("^#define (\\w+?)(?:\\((.*)\\))? (.*)$", CASE_INSENSITIVE);
    public static final Pattern UNDEFINE  = Pattern.compile("^#undefine (\\w+?)$", CASE_INSENSITIVE);
    public static final Pattern IFDEF     = Pattern.compile("^#ifdef (\\w+?)$", CASE_INSENSITIVE);
    public static final Pattern IFNDEF    = Pattern.compile("^#ifndef (\\w+?)$", CASE_INSENSITIVE);
    public static final Pattern ELSE      = Pattern.compile("^#else  ?$", CASE_INSENSITIVE);
    public static final Pattern ENDIF     = Pattern.compile("^#endif  ?$", CASE_INSENSITIVE);

    public static final Pattern STRING = Pattern.compile("\"(.*)\"");
    public static final Pattern CHAR   = Pattern.compile("'(.)'");

    public static final Pattern EQU   = Pattern.compile("(\\w+)\\s+equ\\s", CASE_INSENSITIVE);
    public static final Pattern DATA  = Pattern.compile("(\\w+)\\s+data\\s", CASE_INSENSITIVE);
    public static final Pattern BIT   = Pattern.compile("(\\w+)\\s+bit\\s", CASE_INSENSITIVE);
    public static final Pattern LABEL = Pattern.compile("(\\w+):");

    public static final Pattern DB = Pattern.compile("db\\s+([\\da-fx]+[hb]?(?:,\\s*(?:[\\da-fx]+[hb]?))*)", CASE_INSENSITIVE);
    public static final Pattern DW = Pattern.compile("dw\\s+([\\da-fx]+[hb]?(?:,\\s*(?:[\\da-fx]+[hb]?))*)", CASE_INSENSITIVE);
    public static final Pattern DS = Pattern.compile("ds\\s+([\\da-fx]+[hb]?)(?:\\s+<<\\s+([\\da-fx]+[hb]?))?", CASE_INSENSITIVE);

    public static final Pattern ORG = Pattern.compile("org", CASE_INSENSITIVE);
    public static final Pattern END = Pattern.compile("end", CASE_INSENSITIVE);

    // Properties used for persistent stuff like preferences
    public static final Properties PROPERTIES = new Properties();

    static
    {
        try
        {
            File settings = new File(SETTINGS_FILENAME);
            if (settings.exists())
            {
                PROPERTIES.load(new FileReader(settings));
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if (PROPERTIES.containsKey(SRC_FILE)) Main.setSrcFile(new File(PROPERTIES.getProperty(SRC_FILE)));

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    File settings = new File(SETTINGS_FILENAME);
                    if (!settings.exists()) settings.createNewFile();
                    PROPERTIES.store(new FileWriter(settings), SETTINGS_COMMENT);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }));
    }

    public static final FileFilter ASM_FILE_FILTER = new FileFilter()
    {
        @Override
        public boolean accept(File f)
        {
            return FilenameUtils.getExtension(f.getName()).equalsIgnoreCase("asm") || f.isDirectory();
        }

        @Override
        public String getDescription()
        {
            return "*.asm | ASM text fimes";
        }
    };

    public static final FileFilter FOLDER_FILTER = new FileFilter()
    {
        @Override
        public boolean accept(File f)
        {
            return f.isDirectory();
        }

        @Override
        public String getDescription()
        {
            return "Directories";
        }
    };
}
