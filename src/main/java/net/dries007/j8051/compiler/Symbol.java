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

import net.dries007.j8051.util.Constants;
import net.dries007.j8051.util.Helper;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.dries007.j8051.util.IntegerEvaluator.EVALUATOR;
import static net.dries007.j8051.util.IntegerEvaluator.EVALUATOR_BITS;

/**
 * @author Dries007
 */
public class Symbol
{
    private final String  key;
    private final Type    type;
    private Integer       intValue;
    private String        stringValue;

    public Symbol(Type type, Matcher matcher)
    {
        this.key = matcher.group(1);
        this.type = type;
        if (type != Type.LABEL)
        {
            stringValue = matcher.group(2);
            try
            {
                intValue = Helper.parseToInt(stringValue);
            }
            catch (NumberFormatException ignored)
            {
                // This parsing is done to avoid using the IntegerEvaluator later.
            }
        }
    }

    public void resolve(HashMap<String, Symbol> symbolList)
    {
        intValue = (type == Type.BIT ? EVALUATOR_BITS : EVALUATOR).evaluate(stringValue, symbolList);
    }

    public String getKey()
    {
        return key;
    }

    public Integer getIntValue()
    {
        return intValue;
    }

    public String getStringValue()
    {
        return stringValue;
    }

    public Type getType()
    {
        return type;
    }

    public boolean isDefined()
    {
        return intValue != null;
    }

    public static enum Type
    {
        // Label must be first
        LABEL(Constants.LABEL), EQU(Constants.EQU), DATA(Constants.DATA), BIT(Constants.BIT);

        public final Pattern pattern;

        Type(Pattern pattern)
        {
            this.pattern = pattern;
        }
    }
}
