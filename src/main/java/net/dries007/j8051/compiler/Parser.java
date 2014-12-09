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
import java.util.*;

import static net.dries007.j8051.util.Constants.*;

/**
 * @author Dries007
 */
public class Parser
{
    public final Symbol                        currentLocation = new Symbol();
    public final LinkedList<Component>         components      = new LinkedList<>();
    public final LinkedList<Section>           sections        = new LinkedList<>();
    public final HashMap<String, Symbol>       symbols         = new HashMap<>();
    public final LinkedHashMap<String, String> includeFiles    = new LinkedHashMap<>();
    public final String src;
    public       String postPre;

    private Stage stage = Stage.INIT;

    public Parser(String src)
    {
        this.src = src.replaceAll("\\r\\n", "\n");
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
            if (component instanceof Symbol && ((Symbol) component).type == Symbol.Type.ORG) currentLocation.intValue = ((Symbol) component).intValue;
            component.address = currentLocation.intValue;
            if (component instanceof SrcComponent) throw new CompileException(component, "Unsolved src: " + ((SrcComponent) component).contents);
            if (!component.isResolved())
            {
                try
                {
                    component.tryResolve(currentLocation.intValue, symbols);
                    component.setResolved(true);

                    if (component instanceof Symbol && ((Symbol) component).type == Symbol.Type.LABEL) i.remove();

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
        for (Component component : components) data.add(component.getDebug());
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
        LinkedList<String[]> data = new LinkedList<>();
        for (Section section : sections) section.addToHexTable(data);
        return data.toArray(new String[data.size()][]);
    }

    private void makeHexFile() throws IOException, CompileException
    {
        File file = new File(Main.srcFile.getParentFile(), FilenameUtils.getBaseName(Main.srcFile.getName()) + ".hex");
        if (file.exists()) file.delete();
        LinkedList<String> lines = new LinkedList<>();
        for (Section section : sections) section.addToHexFile(lines);
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
                    public void work(Parser parser) throws Exception
                    {
                    }
                },
        MAKE_HEX(DONE)
                {
                    @Override
                    public void work(Parser parser) throws Exception
                    {
                        int lastSize = -1;
                        int lastStart = -1;
                        Section currentSection = null;
                        for (Component component : parser.components)
                        {
                            if (component instanceof Symbol)
                            {
                                if (((Symbol) component).type == Symbol.Type.ORG)
                                {
                                    if (currentSection != null)
                                    {
                                        lastSize = currentSection.getSize();
                                        parser.sections.add(currentSection);
                                    }

                                    if (lastStart != -1 && lastStart + lastSize > ((Symbol) component).intValue) throw new CompileException(component, "Section overlap!");

                                    currentSection = new Section(((Symbol) component).intValue);
                                    lastStart = currentSection.startaddress;
                                }
                                else if (((Symbol) component).type == Symbol.Type.END)
                                {
                                    if (currentSection != null)
                                    {
                                        lastSize = currentSection.getSize();
                                        parser.sections.add(currentSection);
                                    }
                                    currentSection = null;
                                }
                            }
                            else
                            {
                                if (currentSection == null) throw new CompileException(component, "Component doesn't belong to a code section.");
                                currentSection.addData(component);
                            }
                        }
                        if (currentSection != null) parser.sections.add(currentSection);
                        parser.makeHexFile();
                    }
                },
        RESOLVE_ALL(MAKE_HEX)
                {
                    @Override
                    public void work(Parser parser) throws Exception
                    {
                        //noinspection StatementWithEmptyBody
                        while (parser.resolveAll()) ;
                    }
                },
        RESOLVE_INSTRUCTIONS(RESOLVE_ALL)
                {
                    @Override
                    public void work(Parser parser) throws Exception
                    {
                        InstructionComponent.resolveInstructions(parser.components, parser.symbols);
                    }
                },
        RESOLVE_SYMBOLS(RESOLVE_INSTRUCTIONS)
                {
                    @Override
                    public void work(Parser parser) throws Exception
                    {
                        //noinspection StatementWithEmptyBody
                        while (Symbol.resolveSymbols(parser.components, parser.symbols)) ;
                    }
                },
        FIND_INSTRUCTIONS(RESOLVE_SYMBOLS)
                {
                    @Override
                    public void work(Parser parser) throws Exception
                    {
                        InstructionComponent.findInstructions(parser.components);
                    }
                },
        FIND_BYTES(FIND_INSTRUCTIONS)
                {
                    @Override
                    public void work(Parser parser) throws Exception
                    {
                        Bytes.findBytes(parser.components);
                    }
                },
        FIND_SYMBOLS(FIND_BYTES)
                {
                    @Override
                    public void work(Parser parser) throws Exception
                    {
                        Symbol.findSymbols(parser.components, parser.symbols);
                    }
                },
        PREPROCESSOR(FIND_SYMBOLS)
                {
                    @Override
                    public void work(Parser parser) throws Exception
                    {
                        Preprocessor.process(parser.components, parser.src, parser.includeFiles);
                        StringBuilder stringBuilder = new StringBuilder();
                        for (Component component : parser.components) stringBuilder.append(((SrcComponent) component).contents).append('\n');
                        parser.postPre = stringBuilder.toString();
                        parser.symbols.put("$", parser.currentLocation);
                    }
                },
        INIT(PREPROCESSOR)
                {
                    @Override
                    public void work(Parser parser) throws Exception
                    {

                    }
                };

        public final Stage nextStep;

        Stage(Stage nextStep)
        {
            this.nextStep = nextStep;
        }

        public abstract void work(Parser parser) throws Exception;
    }
}
