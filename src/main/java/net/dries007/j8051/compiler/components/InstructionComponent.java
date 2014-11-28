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

import net.dries007.j8051.compiler.Instruction;
import net.dries007.j8051.util.exceptions.CompileException;
import net.dries007.j8051.util.exceptions.SymbolUndefinedException;
import net.dries007.j8051.util.exceptions.SymbolUnknownException;

import java.util.*;
import java.util.regex.Matcher;

/**
 * @author Dries007
 */
public class InstructionComponent extends Component
{
    public final Instruction.Type type;
    public Instruction instruction;
    private Object[] objects;

    private InstructionComponent(int startOffset, Matcher matcher, Instruction.Type type)
    {
        super(matcher.start() + startOffset, matcher.end() + startOffset);
        this.type = type;
        if (Instruction.SIMPLE_INSTRUCTIONS.containsKey(type)) instruction = Instruction.SIMPLE_INSTRUCTIONS.get(type);
    }

    @Override
    public String toString()
    {
        return "INSTRUCTION: \t" + (instruction == null ? type : instruction);
    }

    @Override
    protected Object getContents()
    {
        return instruction;
    }

    @Override
    protected Object getSubType()
    {
        return type;
    }

    @Override
    public Integer getSize(Map<String, Symbol> symbols)
    {
        return instruction.size;
    }

    @Override
    public void tryResolve(HashMap<String, Symbol> symbols) throws SymbolUndefinedException
    {
        int i = 0;
        data = new int[instruction.size];
        data[i++] = instruction.opcode;
        for (Instruction.Argument argument : instruction.arguments)
        {
//            if (argument.addsByteCode() && argument != Instruction.Argument.REL)
//            {
//                if (objects[i - 1] instanceof Integer) data[i] = ((Integer) objects[i - 1]) & 0xFF;
//                else data[i] = argument.symbolType.evaluator.evaluate((String) objects[i - 1], symbols);
//            }
        }
    }

    public static void resolveInstructions(LinkedList<Component> components, HashMap<String, Symbol> symbols)throws CompileException
    {
        if (components.isEmpty()) return;
        ListIterator<Component> i = components.listIterator();
        Component prev = i.next();
        while (i.hasNext())
        {
            Component current = i.next();
            if (prev instanceof InstructionComponent && current instanceof UnsolvedComponent)
            {
                InstructionComponent instructionComponent = (InstructionComponent) prev;
                UnsolvedComponent unsolvedComponent = (UnsolvedComponent) current;
                String[] arguments = unsolvedComponent.contents.split(",\\s*");
                if (instructionComponent.instruction == null)
                {
                    List<Instruction> instructions = instructionComponent.type.getInstructions();
                    for (Instruction instruction : instructions)
                    {
                        if (instructionComponent.matches(symbols, instruction, arguments))
                        {
                            i.remove();
                            prev.setSrcEnd(current.getSrcEnd());
                            break;
                        }
                    }
                }
            }
            prev = current;
        }
    }

    private boolean matches(HashMap<String, Symbol> symbols, Instruction instruction, String[] args) throws CompileException
    {
        if (instruction.arguments.length != args.length) return false;
        Object[] data = new Object[args.length];
        for (int i = 0; i < args.length; i++)
        {
            Instruction.Argument argument = instruction.arguments[i];
            if (argument.string != null)
            {
                if (argument.string.equalsIgnoreCase(args[i]))
                {
                    data[i] = args[i];
                    continue;
                }
                else return false;
            }

            if (argument.prefix != null && argument.prefix == args[i].charAt(0)) args[i] = args[i].substring(1);
            try
            {
                data[i] = argument.symbolType.evaluator.evaluate(args[i], symbols);
            }
            catch (SymbolUnknownException e)
            {
                return false;
            }
            catch (SymbolUndefinedException e)
            {
                data[i] = args[i];
            }
        }
        this.instruction = instruction;
        this.objects = data;
        return true;
    }

    public static void findInstructions(List<Component> components)
    {
        for (Instruction.Type type : Instruction.Type.values())
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

                    UnsolvedComponent pre = new UnsolvedComponent(component.getSrcStart(), src.substring(0, matcher.start()));
                    if (pre.shouldAdd()) i.add(pre);

                    InstructionComponent instructionComponent = new InstructionComponent(pre.getSrcEnd(), matcher, type);
                    i.add(instructionComponent);

                    UnsolvedComponent post = new UnsolvedComponent(instructionComponent.getSrcEnd(), src.substring(matcher.end()));
                    if (post.shouldAdd()) i.add(post);
                }
            }
        }
    }
}
