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

import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Warning: Regex madness ahead.
 *
 * @author Dries007
 */
public class Macro
{
    private String   name;
    private String[] args;
    private String   text;
    private Pattern  pattern;

    public Macro(Matcher matcher, ListIterator<Line> iterator)
    {
        name = matcher.group(1);
        args = matcher.group(2) != null ? matcher.group(2).split(", ?") : null;
        text = matcher.group(3);
        while (text.charAt(text.length() - 1) == '\\')
        {
            Line line = iterator.next();
            if (line.done) continue;
            line.done = true;

            text = text.substring(0, text.length() - 1) + " " + line.code;
        }
        if (args != null)
        {
            StringBuilder patternBuilder = new StringBuilder();
            patternBuilder.append(name).append("\\(");
            for (int i = 0; i < args.length; i++)
            {
                patternBuilder.append("([^,]+?)");
                if (i != args.length - 1) patternBuilder.append(", ?");
            }
            patternBuilder.append("\\)");
            pattern = Pattern.compile(patternBuilder.toString());
        }
    }

    public String acton(String line)
    {
        if (args == null) return line.replace(name, text);
        Matcher matcher = pattern.matcher(line);
        if (!matcher.find()) return line;
        String replacement = text;
        for (int i = 0; i < args.length; i++)
        {
            replacement = replacement.replace(args[i], matcher.group(i + 1));
        }
        return matcher.replaceFirst(replacement);
    }
}
