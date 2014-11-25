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

import net.dries007.j8051.compiler.exceptions.CompileException;
import net.dries007.j8051.util.Helper;
import net.dries007.j8051.util.IntegerEvaluator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.regex.Matcher;

import static net.dries007.j8051.util.Constants.*;

/**
 * @author Dries007
 */
public class Directives
{
    private Directives()
    {
    }

    static void resolveConstants(LinkedList<Line> lines, HashMap<String, Integer> constantsValueMap, HashMap<String, Compiler.DataType> constantsTypeMap) throws CompileException
    {
        System.out.println("Start " + lines.size());
        int prevLines, i = 0;
        do
        {
            long start = System.currentTimeMillis();
            prevLines = lines.size();
            lines = doResolve(lines, constantsValueMap, constantsTypeMap);
            System.out.println("Pass: " + i++ + " Time: " + (System.currentTimeMillis() - start) + " Size: " + prevLines + " Leftovers: " + lines.size());
            System.out.println(lines);
        }
        while (!lines.isEmpty() && (prevLines != lines.size()));

        if (!lines.isEmpty()) throw new CompileException("Unresolved lines: " + lines);
    }

    private static LinkedList<Line> doResolve(LinkedList<Line> lines, HashMap<String, Integer> constantsValueMap, HashMap<String, Compiler.DataType> constantsTypeMap) throws CompileException
    {
        LinkedList<Line> leftovers = new LinkedList<>();
        ListIterator<Line> i = lines.listIterator();
        while (i.hasNext())
        {
            Line line = i.next();
            if (line.done) continue;
            Matcher matcher = EQU.matcher(line.code);
            if (matcher.find())
            {
                if (constantsValueMap.containsKey(matcher.group(1))) throw new CompileException("EQU double define: " + matcher.group());
                constantsTypeMap.put(matcher.group(1), Compiler.DataType.EQU);
                try
                {
                    constantsValueMap.put(matcher.group(1), Helper.parseToInt(matcher.group(2)));
                    line.done = true;
                    continue;
                }
                catch (NumberFormatException e)
                {
                    try
                    {
                        constantsValueMap.put(matcher.group(1), IntegerEvaluator.EVALUATOR.evaluate(matcher.group(2), constantsValueMap));
                        line.done = true;
                        continue;
                    }
                    catch (NumberFormatException e1)
                    {
                        leftovers.add(line);
                    }
                }
            }
            matcher = DATA.matcher(line.code);
            if (matcher.find())
            {
                if (constantsValueMap.containsKey(matcher.group(1))) throw new CompileException("DATA double define: " + matcher.group());
                constantsTypeMap.put(matcher.group(1), Compiler.DataType.DATA);
                try
                {
                    constantsValueMap.put(matcher.group(1), Helper.parseToInt(matcher.group(2)));
                    line.done = true;
                    continue;
                }
                catch (NumberFormatException e)
                {
                    try
                    {
                        constantsValueMap.put(matcher.group(1), IntegerEvaluator.EVALUATOR.evaluate(matcher.group(2), constantsValueMap));
                        line.done = true;
                        continue;
                    }
                    catch (NumberFormatException e1)
                    {
                        leftovers.add(line);
                    }
                }
            }
            matcher = BIT.matcher(line.code);
            if (matcher.find())
            {
                if (constantsValueMap.containsKey(matcher.group(1))) throw new CompileException("BIT double define: " + matcher.group());
                constantsTypeMap.put(matcher.group(1), Compiler.DataType.BIT);
                try
                {
                    constantsValueMap.put(matcher.group(1), Helper.parseToInt(matcher.group(2)));
                    line.done = true;
                    continue;
                }
                catch (NumberFormatException e)
                {
                    try
                    {
                        constantsValueMap.put(matcher.group(1), IntegerEvaluator.EVALUATOR.evaluate(matcher.group(2), constantsValueMap));
                        line.done = true;
                        continue;
                    }
                    catch (NumberFormatException e1)
                    {
                        leftovers.add(line);
                    }
                }
            }
        }
        return leftovers;
    }
}
