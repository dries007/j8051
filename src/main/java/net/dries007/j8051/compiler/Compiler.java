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

import net.dries007.j8051.compiler.components.Bytes;
import net.dries007.j8051.compiler.components.Component;
import net.dries007.j8051.compiler.components.Symbol;
import net.dries007.j8051.compiler.components.UnsolvedComponent;
import net.dries007.j8051.compiler.exceptions.CompileException;
import net.dries007.j8051.compiler.exceptions.PreprocessorException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author Dries007
 */
public class Compiler
{
    public final String preprocessed, symbolsFound, firstResolve;

    public final LinkedList<Component> components = new LinkedList<>();
    public final HashMap<String, Symbol> symbols = new HashMap<>();

    public Compiler(String src) throws PreprocessorException
    {
        components.add(new UnsolvedComponent(Preprocessor.process(src)));
        preprocessed = stringify(false);
        Symbol.findSymbols(components, symbols);
        symbolsFound = stringify(true);
        Bytes.findBytes(components);
        //noinspection StatementWithEmptyBody
        while (Symbol.resolveSymbols(components, symbols));
        firstResolve = stringify(true);
    }

    private String stringify(boolean changeNewlines)
    {
        StringBuilder stringBuilder = new StringBuilder();
        for (Component component : components)
        {
            stringBuilder.append(changeNewlines ? component.toString().replaceAll("[\\r\\n]+", " ") : component).append('\n');
        }
        return stringBuilder.toString();
    }

    public Object[][] getSymbols() throws CompileException
    {
        ArrayList<Object[]> data = new ArrayList<>(symbols.size());
        for (Symbol symbol : symbols.values())
        {
            data.add(new Object[]{symbol.key, symbol.type, symbol.isDefined() ? Integer.toHexString(symbol.intValue) : "_UNDEFINED_", symbol.isDefined() ? symbol.intValue : "_UNDEFINED_"});
        }
        return data.toArray(new Object[data.size()][]);
    }
}
