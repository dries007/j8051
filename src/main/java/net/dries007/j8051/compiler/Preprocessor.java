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
import net.dries007.j8051.compiler.exceptions.IncludeException;
import net.dries007.j8051.compiler.exceptions.PreprocessorException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.regex.Matcher;

import static net.dries007.j8051.util.Constants.*;

/**
 * Warning: Regex madness ahead.
 *
 * @author Dries007
 */
public class Preprocessor
{

    private Preprocessor()
    {
    }

    public static HashMap<String, Macro> process(LinkedList<Line> lines) throws CompileException
    {
        HashMap<String, Macro> symbols = new HashMap<>();
        LinkedList<Boolean> ifList = new LinkedList<>();

        ListIterator<Line> i = lines.listIterator();
        while (i.hasNext())
        {
            Line line = i.next();
            if (line.done) continue;
            if (line.src.charAt(0) == PREFIX_PRECOMPILER) // Initial check is fast
            {
                Matcher matcher = INCLUDE_A.matcher(line.src);
                if (matcher.matches())
                {
                    line.done = true;
                    include(i, new File(matcher.group(1)));
                    continue;
                }
                matcher = INCLUDE_R.matcher(line.src);
                if (matcher.matches())
                {
                    line.done = true;
                    include(i, new File(Main.includeFile, matcher.group(1)));
                    continue;
                }
                matcher = DEFINE.matcher(line.src);
                if (matcher.matches())
                {
                    line.done = true;
                    if (symbols.containsKey(matcher.group(1))) throw new PreprocessorException(matcher.group(1) + " already defined.");
                    symbols.put(matcher.group(1), new Macro(matcher, i));
                    continue;
                }
                matcher = UNDEFINE.matcher(line.src);
                if (matcher.matches())
                {
                    line.done = true;
                    symbols.remove(matcher.group(1));
                    continue;
                }
                matcher = IFDEF.matcher(line.src);
                if (matcher.matches())
                {
                    line.done = true;
                    ifList.add(symbols.containsKey(matcher.group(1)));
                    continue;
                }
                matcher = IFNDEF.matcher(line.src);
                if (matcher.matches())
                {
                    line.done = true;
                    ifList.add(!symbols.containsKey(matcher.group(1)));
                    continue;
                }
                matcher = ELSE.matcher(line.src);
                if (matcher.matches())
                {
                    line.done = true;
                    ifList.add(!ifList.removeLast());
                    continue;
                }
                matcher = ENDIF.matcher(line.src);
                if (matcher.matches())
                {
                    line.done = true;
                    ifList.removeLast();
                    continue;
                }
            }
            boolean changes;
            do
            {
                changes = false;
                for (String key : symbols.keySet())
                {
                    if (line.code.contains(key))
                    {
                        String oldLine = line.src;
                        line.code = symbols.get(key).acton(line.code);
                        if (!oldLine.equals(line.code)) changes = true;
                    }
                }
            } while (changes);
            line.done = !(ifList.isEmpty() || ifList.getLast());
        }
        return symbols;
    }

    private static void include(ListIterator<Line> i, File file) throws IncludeException
    {
        try
        {
            String fileName = file.getName();
            int lineCounter = 0;
            for (Object src : FileUtils.readLines(file, PROPERTIES.getProperty(ENCODING, "Cp1252")))
            {
                Line line = new Line(fileName, lineCounter++, (String) src);
                if (!line.done) i.add(line);
            }
        }
        catch (IOException e)
        {
            throw new IncludeException(e);
        }
    }
}
