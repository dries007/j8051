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

import net.dries007.j8051.Main;
import net.dries007.j8051.compiler.exceptions.CompileException;
import net.dries007.j8051.compiler.exceptions.PreprocessorException;
import net.dries007.j8051.util.Constants;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;

import static net.dries007.j8051.util.Constants.ENCODING;
import static net.dries007.j8051.util.Constants.PROPERTIES;

/**
 * Warning: Regex madness ahead.
 * @author Dries007
 */
public class Preprocessor
{
    public static final char         PREFIX = '#';

    private Preprocessor()
    {
    }

    static String process(String text, LinkedList<Node> nodes) throws CompileException
    {
        HashMap<String, Macro> symbols = new HashMap<>();
        StringBuilder stringBuilder = new StringBuilder(text.length());
        LinkedList<Boolean> ifList = new LinkedList<>();
        ArrayList<String> list = new ArrayList<>(Arrays.asList(text.split("[\r\n]+")));
        boolean lastBlanck = false;
        for (int i = 0; i < list.size(); i++)
        {
            String line = list.get(i).trim();
            line = line.replaceFirst(";.*", "");
            if (line.length() == 0)
            {
                if (!lastBlanck)
                {
                    stringBuilder.append('\n');
                    lastBlanck = true;
                }
                continue;
            }
            lastBlanck = false;
            if (line.charAt(0) == PREFIX)
            {
                Matcher matcher = Constants.INCLUDE_A.matcher(line);
                if (matcher.matches())
                {
                    list.remove(i);
                    try
                    {
                        //noinspection unchecked
                        list.addAll(i, FileUtils.readLines(new File(matcher.group(1)), PROPERTIES.getProperty(ENCODING, "Cp1252")));
                    }
                    catch (IOException e)
                    {
                        throw new CompileException(e);
                    }
                    continue;
                }
                matcher = Constants.INCLUDE_R.matcher(line);
                if (matcher.matches())
                {
                    list.remove(i);
                    try
                    {
                        //noinspection unchecked
                        list.addAll(i, FileUtils.readLines(new File(Main.includeFile, matcher.group(1)), PROPERTIES.getProperty(ENCODING, "Cp1252")));
                    }
                    catch (IOException e)
                    {
                        throw new CompileException(e);
                    }
                    continue;
                }
                matcher = Constants.DEFINE.matcher(line);
                if (matcher.matches())
                {
                    if (symbols.containsKey(matcher.group(1))) throw new PreprocessorException(matcher.group(1) + " already defined.");
                    symbols.put(matcher.group(1), new Macro(matcher));
                    continue;
                }
                matcher = Constants.UNDEFINE.matcher(line);
                if (matcher.matches())
                {
                    symbols.remove(matcher.group(1));
                    continue;
                }
                matcher = Constants.IFDEF.matcher(line);
                if (matcher.matches())
                {
                    ifList.add(symbols.containsKey(matcher.group(1)));
                    System.out.println(ifList.getLast());
                    continue;
                }
                matcher = Constants.IFNDEF.matcher(line);
                if (matcher.matches())
                {
                    ifList.add(!symbols.containsKey(matcher.group(1)));
                    System.out.println(ifList.getLast());
                    continue;
                }
                matcher = Constants.ELSE.matcher(line);
                if (matcher.matches())
                {
                    ifList.add(!ifList.removeLast());
                    System.out.println(ifList.getLast());
                    continue;
                }
                matcher = Constants.ENDIF.matcher(line);
                if (matcher.matches())
                {
                    ifList.removeLast();
                    continue;
                }
            }
            else
            {
                if (ifList.isEmpty() || ifList.getLast())
                {
                    boolean changes;
                    do
                    {
                        changes = false;
                        for (String key : symbols.keySet())
                        {
                            if (line.contains(key))
                            {
                                String oldLine = line;
                                line = symbols.get(key).acton(line);
                                if (!oldLine.equals(line)) changes = true;
                            }
                        }
                    } while (changes);
                    nodes.add(new UnresolvedNode(line));
                    stringBuilder.append(line).append('\n');
                }
            }
        }
        return stringBuilder.toString();
    }
}