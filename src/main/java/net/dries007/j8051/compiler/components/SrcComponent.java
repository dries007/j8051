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

import net.dries007.j8051.util.exceptions.SymbolUndefinedException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Dries007
 */
public class SrcComponent extends Component
{
    public final String contents;

    public SrcComponent(int srcLine, String contents)
    {
        super(srcLine);
        this.contents = contents.trim();
    }

    @Override
    public String toString()
    {
        return "UNSOLVED: " + contents;
    }

    public boolean shouldAdd()
    {
        return !this.contents.isEmpty();
    }

    @Override
    protected Object getContents()
    {
        return contents;
    }

    @Override
    protected Object getSubType()
    {
        return "";
    }

    @Override
    public Integer getSize(Map<String, Symbol> symbols)
    {
        throw new IllegalStateException("Method should never be used.");
    }

    @Override
    public void tryResolve(int currentLocation, HashMap<String, Symbol> symbols) throws SymbolUndefinedException
    {
        throw new IllegalStateException("Method should never be used.");
    }
}
