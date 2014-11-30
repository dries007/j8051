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

import net.dries007.j8051.util.Helper;
import net.dries007.j8051.util.exceptions.CompileException;
import net.dries007.j8051.util.exceptions.SymbolUndefinedException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Dries007
 */
public abstract class Component
{
    public    int     address;
    protected int[]   data;
    private int start, end;
    private   boolean resolved;

    protected Component(int start, int end)
    {
        this.start = start;
        this.end = end;
    }

    public int getSrcStart()
    {
        return start;
    }

    public int getSrcEnd()
    {
        return end;
    }

    public Object[] getDebug()
    {
        return new Object[]{getSrcStart(), getSrcEnd(), this.getClass().getSimpleName().replace("Component", ""), getSubType(), getContents(), String.format("0x%04X", address), Helper.toHexString(getData())};
    }

    protected abstract Object getContents();

    protected abstract Object getSubType();

    public void setSrcEnd(int end)
    {
        this.end = end;
    }

    public abstract Integer getSize(Map<String, Symbol> symbols);

    public boolean isResolved()
    {
        return resolved;
    }

    public int[] getData()
    {
        return data;
    }

    public abstract void tryResolve(int currentLocation, HashMap<String, Symbol> symbols) throws SymbolUndefinedException, CompileException;

    public void setResolved(boolean resolved)
    {
        this.resolved = resolved;
    }
}
