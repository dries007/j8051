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

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Doesn't do anything on instantiation
 *
 * @author Dries007
 */
public class Compiler
{
    public static final int SRC = 0;            // Nothing done
    public static final int PRE = 1;            // Preprocessor done
    public static final int INSTRUCTIONS = 2;   // Instructions done
    public static final int CONSTANTS = 3;      // Constants done

    public enum DataType
    {
        EQU, DATA, BIT, LABLEL
    }
    private HashMap<String, Integer>  dataValueMap     = new HashMap<>();
    private HashMap<String, DataType> dataTypeMap = new HashMap<>();

    private int              status = SRC;
    private LinkedList<Node> nodes  = new LinkedList<>();

    private final String src;
    private       String preProcessed;

    public Compiler(String src)
    {
        this.src = src;
    }

    public String getPreProcessed()
    {
        doWork(PRE);
        return preProcessed;
    }

    public Object[][] getConstantsData()
    {
        doWork(CONSTANTS);
        Object[] keys = dataTypeMap.keySet().toArray();
        Object[][] data = new Object[keys.length][4];
        for (int i = 0; i < keys.length; i++)
        {
            data[i] = new Object[]{keys[i], dataTypeMap.get(keys[i]).name(), Integer.toHexString(dataValueMap.get(keys[i])), Integer.toString(dataValueMap.get(keys[i]))};
        }
        return data;
    }

    public void doWork(int target)
    {
        if (status >= target) return;
        if (status < PRE)
        {
            preProcessed = Preprocessor.process(src, nodes);
            status = PRE;
        }
        if (status >= target) return;
        if (status < CONSTANTS)
        {
            ConstantsDirectives.resolveConstants(nodes, dataValueMap, dataTypeMap);
            status = CONSTANTS;
        }
    }
}
