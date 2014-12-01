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
import net.dries007.j8051.compiler.components.*;
import net.dries007.j8051.util.exceptions.CompileException;
import net.dries007.j8051.util.exceptions.SymbolUndefinedException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

import static net.dries007.j8051.util.Constants.*;

/**
 * @author Dries007
 */
public class Compiler
{
    public final Symbol                  currentLocation = new Symbol();
    public final LinkedList<Component>   components      = new LinkedList<>();
    public final HashMap<String, Symbol> symbols         = new HashMap<>();
    public final HashMap<String, String> includeFiles    = new HashMap<>();
    public String src;
    public  Integer[] hex   = new Integer[0];
    private Stage     stage = Stage.INIT;

    public Compiler(String src)
    {
        this.src = src;
    }

    public boolean hasWork()
    {
        return stage != Stage.DONE;
    }

    public void doWork() throws Exception
    {
        stage = stage.nextStep;
        stage.work(this);
    }

    private boolean resolveAll() throws CompileException
    {
        boolean resolvedAny = false;
        currentLocation.intValue = 0;
        ListIterator<Component> i = components.listIterator();
        while (i.hasNext())
        {
            Component component = i.next();
            component.address = currentLocation.intValue;
            if (component instanceof SrcComponent) throw new CompileException(component, "Unsolved src: " + ((SrcComponent) component).contents);
            if (!component.isResolved())
            {
                try
                {
                    component.tryResolve(currentLocation.intValue, symbols);
                    component.setResolved(true);

                    if (component instanceof Symbol) i.remove();

                    resolvedAny = true;
                }
                catch (ArrayIndexOutOfBoundsException e)
                {
                    throw new CompileException(component, "", e);
                }
                catch (SymbolUndefinedException ignored)
                {

                }
            }
            currentLocation.intValue += component.getSize(symbols);
        }
        currentLocation.intValue = null;
        return resolvedAny;
    }

    public Object[][] getComponents()
    {
        ArrayList<Object[]> data = new ArrayList<>(components.size());
        for (Component component : components)
        {
            data.add(component.getDebug());
        }
        return data.toArray(new Object[data.size()][]);
    }

    public Object[][] getSymbols()
    {
        ArrayList<Object[]> data = new ArrayList<>(symbols.size());
        for (Symbol symbol : symbols.values())
        {
            if (getStage() == Stage.DONE && symbol == currentLocation) continue;
            data.add(new Object[]{symbol.key, symbol.type, symbol.isDefined() ? String.format("0x%04X", symbol.intValue) : "_UNDEFINED_", symbol.isDefined() ? String.format("%04d", symbol.intValue) : "_UNDEFINED_", symbol.stringValue});
        }
        return data.toArray(new Object[data.size()][]);
    }

    public Object[][] getHexTable()
    {
        ArrayList<String[]> data = new ArrayList<>(hex.length / 16);
        for (int i = 0; i <= hex.length / 16; i++)
        {
            String[] line = new String[17];
            line[0] = String.format("0x%02X - 0x%2X", i, i + 16);
            for (int j = 0; j < 16; j++)
            {
                if (hex.length == (i * 16) + j) break;
                line[1 + j] = String.format("%02X", hex[(i * 16) + j]);
            }
            data.add(line);
        }
        return data.toArray(new String[data.size()][]);
    }

    private void makeHexFile() throws IOException, CompileException
    {
        File file = new File(Main.srcFile.getParentFile(), FilenameUtils.getBaseName(Main.srcFile.getName()) + ".hex");
        if (file.exists()) file.delete();
        LinkedList<String> lines = new LinkedList<>();
        for (int i = 0; i <= hex.length / 0x20; i++)
        {
            final int length = Math.min(0x20, hex.length - 0x20 * i);
            final int address = 0x20 * i;
            int sum = length + (address & 0xFF) + (address >>> 8) + 0x00;
            StringBuilder line = new StringBuilder(75); // 75 = normal line length
            line.append(String.format(":%02X%04X00", length, address));
            for (int j = 0; j < length; j++)
            {
                if (hex[address + j] > 0xFF) throw new CompileException("One byte can't be more then 0xFF.");
                sum += hex[address + j];
                line.append(String.format("%02X", hex[address + j]));
            }
            line.append(String.format("%02X", ((~sum) + 1) & 0xFF));
            lines.add(line.toString());
        }
        lines.add(":00000001FF");
        FileUtils.writeLines(file, PROPERTIES.getProperty(ENCODING, ENCODING_DEFAULT), lines);
    }

    public Stage getStage()
    {
        return stage;
    }

    public static enum Stage
    {
        DONE(null)
                {
                    @Override
                    public void work(Compiler compiler) throws Exception
                    {
                    }
                },
        MAKE_HEX(DONE)
                {
                    @Override
                    public void work(Compiler compiler) throws Exception
                    {
                        LinkedList<Integer> hexList = new LinkedList<>();
                        for (Component component : compiler.components) for (int b : component.getData()) hexList.add(b);
                        compiler.hex = hexList.toArray(new Integer[hexList.size()]);
                        compiler.makeHexFile();
                    }
                },
        RESOLVE_ALL(MAKE_HEX)
                {
                    @Override
                    public void work(Compiler compiler) throws Exception
                    {
                        //noinspection StatementWithEmptyBody
                        while (compiler.resolveAll()) ;
                    }
                },
        RESOLVE_INSTRUCTIONS(RESOLVE_ALL)
                {
                    @Override
                    public void work(Compiler compiler) throws Exception
                    {
                        InstructionComponent.resolveInstructions(compiler.components, compiler.symbols);
                    }
                },
        RESOLVE_SYMBOLS(RESOLVE_INSTRUCTIONS)
                {
                    @Override
                    public void work(Compiler compiler) throws Exception
                    {
                        //noinspection StatementWithEmptyBody
                        while (Symbol.resolveSymbols(compiler.components, compiler.symbols)) ;
                    }
                },
        FIND_INSTRUCTIONS(RESOLVE_SYMBOLS)
                {
                    @Override
                    public void work(Compiler compiler) throws Exception
                    {
                        InstructionComponent.findInstructions(compiler.components);
                    }
                },
        FIND_BYTES(FIND_INSTRUCTIONS)
                {
                    @Override
                    public void work(Compiler compiler) throws Exception
                    {
                        Bytes.findBytes(compiler.components);
                    }
                },
        FIND_SYMBOLS(FIND_BYTES)
                {
                    @Override
                    public void work(Compiler compiler) throws Exception
                    {
                        Symbol.findSymbols(compiler.components, compiler.symbols);
                    }
                },
        PREPROCESSOR(FIND_SYMBOLS)
                {
                    @Override
                    public void work(Compiler compiler) throws Exception
                    {
                        compiler.src = Preprocessor.process(compiler.src, compiler.includeFiles);
                        compiler.components.add(new SrcComponent(0, compiler.src));
                        compiler.symbols.put("$", compiler.currentLocation);
                    }
                },
        INIT(PREPROCESSOR)
                {
                    @Override
                    public void work(Compiler compiler) throws Exception
                    {

                    }
                };

        public final Stage nextStep;

        Stage(Stage nextStep)
        {
            this.nextStep = nextStep;
        }

        public abstract void work(Compiler compiler) throws Exception;
    }
}
