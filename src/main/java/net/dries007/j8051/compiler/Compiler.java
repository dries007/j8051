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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * @author Dries007
 */
public class Compiler
{
    public static final int                       SRC          = 0; // Nothing done
    public static final int                       PRE          = 1; // Preprocessor done
    public static final int                       CONSTANTS    = 2; // Constants found
    public static final int                       INSTRUCTIONS = 3; // Instructions found
    public static final int                       LOCATIONS    = 4; // Memory locations mapped

    private final LinkedList<Line> lines = new LinkedList<>();
    private final HashMap<String, Symbol> Symbols = new HashMap<>();

    private int status = SRC;
    private String afterPrecompile, afterInstructions, afterConstants;

    public Compiler(String src)
    {
        String[] split = src.split("[\\r\\n]+");

        String file = Main.srcFile.getName();
        for (int i = 0; i < split.length; i++)
        {
            Line line = new Line(file, i, split[i]);
            if (!line.done) lines.add(line);
        }
        System.out.printf("SRC: %d\n", lines.size());
    }

    public void doWork(int target) throws CompileException
    {
        if (status >= target) return;
        if (status < PRE)
        {
            LinkedList<Line> lines = new LinkedList<>();
            Preprocessor.process(lines);
            System.out.printf("PRE: %d\n", lines.size());
            afterPrecompile = makeString();
            status = PRE;
        }
        if (status >= target) return;
        if (status < CONSTANTS)
        {
            Directives.findConstants(lines, Symbols);
            System.out.printf("CONSTANTS: %d\n", lines.size());
            afterConstants = makeString();
            status = CONSTANTS;
        }
        if (status >= target) return;
        if (status < INSTRUCTIONS)
        {
            Instruction.process(lines);
            System.out.printf("INSTRUCTIONS: %d\n", lines.size());
            afterInstructions = makeString();
            status = INSTRUCTIONS;
        }

    }

    public String getAfterPrecompile() throws CompileException
    {
        doWork(PRE);
        return afterPrecompile;
    }

    public String getAfterConstants() throws CompileException
    {
        doWork(CONSTANTS);
        return afterConstants;
    }

    public Object[][] getSymbols() throws CompileException
    {
        doWork(CONSTANTS);
        ArrayList<Object[]> data = new ArrayList<>(Symbols.size());
        for (Symbol symbol : Symbols.values())
        {
            data.add(new Object[]{symbol.getKey(), symbol.getType(), symbol.getStringValue(), symbol.isDefined() ? Integer.toHexString(symbol.getIntValue()) : "_UNDEFINED_", symbol.isDefined() ? symbol.getIntValue() : "_UNDEFINED_"});
        }
        return data.toArray(new Object[data.size()][]);
    }

    public String getAfterInstructions() throws CompileException
    {
        doWork(INSTRUCTIONS);
        return afterInstructions;
    }

    private String makeString()
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("  | File                 | Line  | Code\n");
        stringBuilder.append("--+----------------------+-------+------------------------------------------\n");
        ListIterator<Line> i = lines.listIterator();
        while (i.hasNext())
        {
            Line line = i.next();
            stringBuilder.append(String.format("%c | %-20s | %5d | %s", line.done ? 'Y' : 'N', line.file, line.line, line.code)).append('\n');
        }
        return stringBuilder.toString();
    }

    public enum DataType
    {
        EQU, DATA, BIT, LABLEL
    }
}
