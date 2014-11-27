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

package net.dries007.j8051.compiler.components;

import net.dries007.j8051.util.Constants;
import net.dries007.j8051.util.IntegerEvaluator;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Dries007
 */
public class Symbol implements Component
{
    public final String  key;
    public final Type    type;
    public Integer       intValue;

    public Symbol(Type type, String key)
    {
        this.key = key;
        this.type = type;
    }

    public static void findSymbols(List<Component> components, Map<String, Symbol> symbols)
    {
        for (Type type : Type.values())
        {
            ListIterator<Component> i = components.listIterator(components.size());
            while (i.hasPrevious())
            {
                Component component = i.previous();
                if (component instanceof UnsolvedComponent)
                {
                    String src = ((UnsolvedComponent) component).contents;

                    Matcher matcher = type.pattern.matcher(src);
                    if (!matcher.find()) continue;
                    i.remove();

                    String pre = src.substring(0, matcher.start()).trim();
                    if (!pre.isEmpty()) i.add(new UnsolvedComponent(pre));

                    Symbol symbol = new Symbol(type, matcher.group(1));
                    symbols.put(matcher.group(1), symbol);
                    i.add(symbol);

                    String post = src.substring(matcher.end()).trim();
                    if (!post.isEmpty()) i.add(new UnsolvedComponent(post));
                }
            }
        }
    }

    public static boolean resolveSymbols(List<Component> components, Map<String, Symbol> symbols)
    {
        boolean resolvedAny = false;
        for (Type type : Type.values())
        {
            if (type.evaluator == null) continue;

            ListIterator<Component> i = components.listIterator();
            Component prev = null;
            while (i.hasNext())
            {
                if (prev == null)
                {
                    prev = i.next();
                    continue;
                }
                Component current = i.next();
                if (prev instanceof Symbol && current instanceof UnsolvedComponent && !((Symbol) prev).isDefined())
                {
                    try
                    {
                        ((Symbol) prev).intValue = type.evaluator.evaluate(((UnsolvedComponent) current).contents.replaceAll("[\\r\\n]+", " "), symbols);
                        i.remove();
                        resolvedAny = true;
                    }
                    catch (NumberFormatException ignored)
                    {

                    }
                }
                prev = current;
            }
        }
        return resolvedAny;
    }

    public boolean isDefined()
    {
        return intValue != null;
    }

    @Override
    public String toString()
    {
        return "SYMBOL: \t" + key + '\t' + type + (intValue == null ? null : "\t0x" + Integer.toHexString(intValue));
    }

    public static enum Type
    {
        LABEL(Constants.LABEL, null), EQU(Constants.EQU, IntegerEvaluator.EVALUATOR), DATA(Constants.DATA, IntegerEvaluator.EVALUATOR), BIT(Constants.BIT, IntegerEvaluator.EVALUATOR_BITS);

        public final Pattern pattern;
        public final IntegerEvaluator evaluator;

        Type(Pattern pattern, IntegerEvaluator evaluator)
        {
            this.pattern = pattern;
            this.evaluator = evaluator;
        }
    }
}
