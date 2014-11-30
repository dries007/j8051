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
import net.dries007.j8051.util.exceptions.AddressOutOfRandException;
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
    public       Instruction      instruction;
    private      Object[]         objects;

    private InstructionComponent(int startOffset, Matcher matcher, Instruction.Type type)
    {
        super(matcher.start() + startOffset, matcher.end() + startOffset);
        this.type = type;
        if (Instruction.SIMPLE_INSTRUCTIONS.containsKey(type)) instruction = Instruction.SIMPLE_INSTRUCTIONS.get(type);
    }

    public static void resolveInstructions(LinkedList<Component> components, HashMap<String, Symbol> symbols) throws CompileException
    {
        if (components.isEmpty()) return;
        ListIterator<Component> i = components.listIterator();
        Component prev = i.next();
        while (i.hasNext())
        {
            Component current = i.next();
            if (prev instanceof InstructionComponent && current instanceof SrcComponent)
            {
                InstructionComponent instructionComponent = (InstructionComponent) prev;
                SrcComponent srcComponent = (SrcComponent) current;
                String[] arguments = srcComponent.contents.split(",\\s*");
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

    public static void findInstructions(List<Component> components)
    {
        for (Instruction.Type type : Instruction.Type.values())
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

                    SrcComponent pre = new SrcComponent(component.getSrcStart(), src.substring(0, matcher.start()));
                    if (pre.shouldAdd()) i.add(pre);

                    InstructionComponent instructionComponent = new InstructionComponent(pre.getSrcEnd(), matcher, type);
                    i.add(instructionComponent);

                    SrcComponent post = new SrcComponent(instructionComponent.getSrcEnd(), src.substring(matcher.end()));
                    if (post.shouldAdd()) i.add(post);
                }
            }
        }
    }

    @Override
    public String toString()
    {
        return "INSTRUCTION: \t" + (instruction == null ? type : instruction);
    }

    @Override
    protected Object getContents()
    {
        return Arrays.toString(objects);
    }

    @Override
    protected Object getSubType()
    {
        return instruction == null ? type : instruction;
    }

    @Override
    public Integer getSize(Map<String, Symbol> symbols)
    {
        return instruction.size;
    }

    @Override
    public void tryResolve(final int currentLocation, HashMap<String, Symbol> symbols) throws SymbolUndefinedException, CompileException
    {
        if (instruction == null) throw new CompileException(this, "Unresolved instruction: " + type + " " + Arrays.toString(objects));
        int datai = 1, dataj = 0;
        data = new int[instruction.size];
        data[0] = instruction.opcode;
        for (Instruction.Argument argument : instruction.arguments)
        {
            if (argument.bytesAdded != 0)
            {
                if (objects[dataj] instanceof Integer)
                {
                    for (int i = 0; i < argument.bytesAdded; i++)
                    {
                        data[datai++] = (((Integer) objects[dataj]) >>> ((argument.bytesAdded - 1 - i) * 8)) & 0xFF;
                    }
                }
                else if (argument == Instruction.Argument.ADDR11)
                {
                    int value = argument.symbolType.evaluator.evaluate((String) objects[dataj], symbols);
                    final int next = currentLocation + instruction.size;
                    if ((next & 0xF800) != (value & 0xF800)) throw new AddressOutOfRandException(objects[dataj] + " is out of range."); // 5 msbit must match
                    data[0] = (data[0] & 0x1F) | ((value & 0x700) >>> 3); // Set the 3 msbit of the opcode to the 3 lsbit of the msbyte of the address
                    data[datai++] = value & 0xFF; // set next byte to the lsbyte of the address
                }
                else
                {
                    int value = argument.symbolType.evaluator.evaluate((String) objects[dataj], symbols);
                    if (argument == Instruction.Argument.REL)
                    {
                        value -= (currentLocation + instruction.size);
                        if (value > 127 || value < -128) throw new AddressOutOfRandException(objects[dataj] + " is out of range.");
                        value &= 0xFF;
                    }
                    for (int i = 0; i < argument.bytesAdded; i++)
                    {
                        data[datai++] = (value >>> ((argument.bytesAdded - 1 - i) * 8)) & 0xFF;
                    }
                }
            }
            dataj++;
        }
        if (instruction.reverseOperands) // Because of 0x85
        {
            int swap = data[1];
            data[1] = data[2];
            data[2] = swap;
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

            if (argument.prefix != null)
            {
                if (argument.prefix != args[i].charAt(0)) return false;
                args[i] = args[i].substring(1);
            }
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
}
