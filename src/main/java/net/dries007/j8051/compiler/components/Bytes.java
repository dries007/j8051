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
import net.dries007.j8051.util.exceptions.SymbolUndefinedException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Dries007
 */
public class Bytes extends Component
{
    public final Type     type;
    public final Object[] objects;
    private int size = -1;

    private Bytes(int srcLine, Matcher matcher, Type type)
    {
        super(srcLine);
        this.type = type;
        if (type == Type.DS) this.objects = new String[]{matcher.group(1), matcher.group(2)};
        else this.objects = matcher.group(1).split(",\\s*");
    }

    public static void findBytes(List<Component> components)
    {
        for (Type type : Type.values())
        {
            ListIterator<Component> i = components.listIterator(components.size());
            while (i.hasPrevious())
            {
                Component component = i.previous();
                if (component instanceof SrcComponent)
                {
                    String src = ((SrcComponent) component).contents;

                    Matcher matcher = type.pattern.matcher(src);
                    if (!matcher.find()) continue;
                    i.remove();

                    SrcComponent pre = new SrcComponent(component.getSrcLine(), src.substring(0, matcher.start()));
                    if (pre.shouldAdd()) i.add(pre);

                    Bytes bytes = new Bytes(pre.getSrcLine(), matcher, type);
                    i.add(bytes);

                    SrcComponent post = new SrcComponent(pre.getSrcLine(), src.substring(matcher.end()));
                    if (post.shouldAdd()) i.add(post);
                }
            }
        }
    }

    @Override
    public String toString()
    {
        return "BYTES: \t" + type + " \t " + Arrays.toString(objects);
    }

    @Override
    protected Object getContents()
    {
        return Arrays.toString(objects);
    }

    @Override
    protected Object getSubType()
    {
        return type;
    }

    @Override
    public Integer getSize(Map<String, Symbol> symbols)
    {
        if (size != -1) return size;
        switch (type)
        {
            case DB:
                return size = objects.length;
            case DW:
                return size = 2 * objects.length;
            case DS:
                return size = IntegerEvaluator.EVALUATOR.evaluate((String) objects[0], symbols);
        }
        throw new IllegalStateException("Type unknown: " + type);
    }

    @Override
    public void tryResolve(int currentLocation, HashMap<String, Symbol> symbols) throws SymbolUndefinedException
    {
        data = new int[getSize(symbols)];
        switch (type)
        {
            case DB:
                for (int i = 0; i < objects.length; i++) data[i] = IntegerEvaluator.EVALUATOR.evaluate((String) objects[i], symbols);
                break;
            case DW:
                for (int i = 0; i < objects.length; i++)
                {
                    int word = IntegerEvaluator.EVALUATOR.evaluate((String) objects[i], symbols);
                    data[2 * i] = (word & 0xFF00) >>> 8;
                    data[2 * i + 1] = word & 0xFF;
                }
                break;
            case DS:
                int setByte = objects[1] == null ? 0 : IntegerEvaluator.EVALUATOR.evaluate((String) objects[1], symbols);
                for (int i = 0; i < size; i++) data[i] = setByte;
                break;
            default:
                throw new IllegalStateException("Type unknown: " + type);
        }
    }

    private static enum Type
    {
        DB(Constants.DB), DW(Constants.DW), DS(Constants.DS);

        public final Pattern pattern;

        Type(Pattern pattern)
        {
            this.pattern = pattern;
        }
    }
}
