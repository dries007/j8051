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

package net.dries007.j8051.preprocessor;

import net.dries007.j8051.Main;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Dries007
 */
public class Preprocessor
{
    public static final Preprocessor PREPROCESSOR = new Preprocessor();
    public static final char         PREFIX = '#';
    public static final Pattern      INCLUDE_A = Pattern.compile("^#include[\\s]+\"(.*)\"$", Pattern.CASE_INSENSITIVE);
    public static final Pattern      INCLUDE_R = Pattern.compile("^#include[\\s]+<(.*)>$", Pattern.CASE_INSENSITIVE);
    //public static final Pattern      DEFINE = Pattern.compile("^#define[\\s]+([\\S]*)(.*)$", Pattern.CASE_INSENSITIVE);
    public static final Pattern      DEFINE = Pattern.compile("^#define[\\s]+(\\w+?)(\\(((\\w*?)(,\\s*?)?)+\\))? (.*)$", Pattern.CASE_INSENSITIVE);

    private HashMap<String, Macro> symbols = new HashMap<>();

    private Preprocessor()
    {

    }

    public String process(String text) throws Exception
    {
        StringBuilder stringBuilder = new StringBuilder(text.length());

        String[] lines = text.split("[\r\n]+");
        for (String line : lines)
        {
            if (line.charAt(0) == PREFIX)
            {
                Matcher matcher = INCLUDE_A.matcher(line);
                if (matcher.matches())
                {
                    stringBuilder.append(process(FileUtils.readFileToString(new File(matcher.group(1))))).append('\n');
                    continue;
                }
                matcher = INCLUDE_R.matcher(line);
                if (matcher.matches())
                {
                    stringBuilder.append(process(FileUtils.readFileToString(new File(Main.includeFile, matcher.group(1))))).append('\n');
                    continue;
                }
                matcher = DEFINE.matcher(line);
                if (matcher.matches())
                {
                    symbols.put(matcher.group(1), new Macro(matcher));
                    continue;
                }
            }
            else stringBuilder.append(line).append('\n');
        }
        return stringBuilder.toString();
    }
}
