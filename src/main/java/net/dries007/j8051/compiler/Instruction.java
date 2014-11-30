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

import net.dries007.j8051.compiler.components.Symbol;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * The 8051 instruction set
 * More info: http://dries007.net/8051/InstructionSet.htm
 *
 * @author Dries007
 */
public class Instruction
{
    public static final EnumMap<Type, Instruction>       SIMPLE_INSTRUCTIONS       = new EnumMap<Type, Instruction>(Type.class);
    public static final EnumMap<Type, List<Instruction>> TYPE_INSTRUCTION_ENUM_MAP = new EnumMap<Type, List<Instruction>>(Type.class);
    public static final Instruction[]                    INSTRUCTIONS              = new Instruction[0x100];

    static
    {
        for (Type type : Type.values()) TYPE_INSTRUCTION_ENUM_MAP.put(type, new LinkedList<Instruction>());

        int opcode = 0;
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.NOP);                                                  //0x00
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.AJMP, Argument.ADDR11);                                //0x01
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 3, Type.LJMP, Argument.ADDR16);                                //0x02
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.RR, Argument.A);                                       //0x03
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.INC, Argument.A);                                      //0x04
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.INC, Argument.DIRECT);                                 //0x05
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.INC, Argument.AT_R0);                                  //0x06
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.INC, Argument.AT_R1);                                  //0x07
        for (int i = 0; i < 8; i++)
            INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.INC, Argument.R[i]);                               //0x08 -> 0x0F
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 3, Type.JBC, Argument.BIT, Argument.REL);                      //0x10
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.ACALL, Argument.ADDR11);                               //0x11
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 3, Type.LCALL, Argument.ADDR16);                               //0x12
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.RRC, Argument.A);                                      //0x13
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.DEC, Argument.A);                                      //0x14
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.DEC, Argument.DIRECT);                                 //0x15
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.DEC, Argument.AT_R0);                                  //0x16
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.DEC, Argument.AT_R1);                                  //0x17
        for (int i = 0; i < 8; i++)
            INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.DEC, Argument.R[i]);                               //0x18 -> 0x1F
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 3, Type.JB, Argument.BIT, Argument.REL);                       //0x20
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.AJMP, Argument.ADDR11);                                //0x21
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.RET);                                                  //0x22
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.RL, Argument.A);                                       //0x23
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.ADD, Argument.A, Argument.DATA);                       //0x24
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.ADD, Argument.A, Argument.DIRECT);                     //0x25
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.ADD, Argument.A, Argument.AT_R0);                      //0x26
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.ADD, Argument.A, Argument.AT_R1);                      //0x27
        for (int i = 0; i < 8; i++)
            INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.ADD, Argument.A, Argument.R[i]);                   //0x28 -> 0x2F
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 3, Type.JNB, Argument.BIT, Argument.REL);                      //0x30
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.ACALL, Argument.ADDR11);                               //0x31
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.RETI);                                                 //0x32
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.RLC, Argument.A);                                      //0x33
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.ADDC, Argument.A, Argument.DATA);                      //0x34
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.ADDC, Argument.A, Argument.DIRECT);                    //0x35
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.ADDC, Argument.A, Argument.AT_R0);                     //0x36
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.ADDC, Argument.A, Argument.AT_R1);                     //0x37
        for (int i = 0; i < 8; i++)
            INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.ADDC, Argument.A, Argument.R[i]);                  //0x38 -> 0x3F
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.JC, Argument.REL);                                     //0x40
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.AJMP, Argument.ADDR11);                                //0x41
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.ORL, Argument.DIRECT, Argument.A);                     //0x42
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 3, Type.ORL, Argument.DIRECT, Argument.DATA);                  //0x43
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.ORL, Argument.A, Argument.DATA);                       //0x44
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.ORL, Argument.A, Argument.DIRECT);                     //0x45
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.ORL, Argument.A, Argument.AT_R0);                      //0x46
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.ORL, Argument.A, Argument.AT_R1);                      //0x47
        for (int i = 0; i < 8; i++)
            INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.ORL, Argument.A, Argument.R[i]);                   //0x48 -> 0x4F
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.JNC, Argument.REL);                                    //0x50
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.ACALL, Argument.ADDR11);                               //0x51
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.ANL, Argument.DIRECT, Argument.A);                     //0x52
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 3, Type.ANL, Argument.DIRECT, Argument.DATA);                  //0x53
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.ANL, Argument.A, Argument.DATA);                       //0x54
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.ANL, Argument.A, Argument.DIRECT);                     //0x55
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.ANL, Argument.A, Argument.AT_R0);                      //0x56
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.ANL, Argument.A, Argument.AT_R1);                      //0x57
        for (int i = 0; i < 8; i++)
            INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.ANL, Argument.A, Argument.R[i]);                   //0x58 -> 0x5F
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.JZ, Argument.REL);                                     //0x60
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.AJMP, Argument.ADDR11);                                //0x61
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.XRL, Argument.DIRECT, Argument.A);                     //0x62
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 3, Type.XRL, Argument.DIRECT, Argument.DATA);                  //0x63
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.XRL, Argument.A, Argument.DATA);                       //0x64
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.XRL, Argument.A, Argument.DIRECT);                     //0x65
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.XRL, Argument.A, Argument.AT_R0);                      //0x66
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.XRL, Argument.A, Argument.AT_R1);                      //0x67
        for (int i = 0; i < 8; i++)
            INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.XRL, Argument.A, Argument.R[i]);                   //0x68 -> 0x6F
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.JNZ, Argument.REL);                                    //0x70
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.ACALL, Argument.ADDR11);                               //0x71
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.ORL, Argument.C, Argument.BIT);                        //0x72
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.JMP, Argument.AT_A_PLUS_DPTR);                         //0x73
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.MOV, Argument.A, Argument.DATA);                       //0x74
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 3, Type.MOV, Argument.DIRECT, Argument.DATA);                  //0x75
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.MOV, Argument.AT_R0, Argument.DATA);                   //0x76
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.MOV, Argument.AT_R1, Argument.DATA);                   //0x77
        for (int i = 0; i < 8; i++)
            INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.MOV, Argument.R[i], Argument.DATA);                //0x78 -> 0x7F
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.SJMP, Argument.REL);                                   //0x80
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.AJMP, Argument.ADDR11);                                //0x81
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.ANL, Argument.C, Argument.BIT);                        //0x82
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.MOVC, Argument.A, Argument.AT_A_PLUS_PC);              //0x83
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.DIV, Argument.AB);                                     //0x84
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 3, true, Type.MOV, Argument.DIRECT, Argument.DIRECT);                //0x85 THIS OPERATION REVERSES THE OPERANDS!
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.MOV, Argument.DIRECT, Argument.AT_R0);                 //0x86
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.MOV, Argument.DIRECT, Argument.AT_R1);                 //0x87
        for (int i = 0; i < 8; i++)
            INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.MOV, Argument.DIRECT, Argument.R[i]);              //0x88 -> 0x8F
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 3, Type.MOV, Argument.DPTR, Argument.DATA16);                  //0x90
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.ACALL, Argument.ADDR11);                               //0x91
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.MOV, Argument.BIT, Argument.C);                        //0x92
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.MOVC, Argument.A, Argument.AT_A_PLUS_DPTR);            //0x93
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.SUBB, Argument.A, Argument.DATA);                      //0x94
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.SUBB, Argument.A, Argument.DIRECT);                    //0x95
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.SUBB, Argument.A, Argument.AT_R0);                     //0x96
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.SUBB, Argument.A, Argument.AT_R1);                     //0x97
        for (int i = 0; i < 8; i++)
            INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.SUBB, Argument.A, Argument.R[i]);                  //0x98 -> 0x9F
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.ORL, Argument.C, Argument.SLASH_BIT);                  //0xA0
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.AJMP, Argument.ADDR11);                                //0xA1
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.MOV, Argument.C, Argument.BIT);                        //0xA2
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.INC, Argument.DPTR);                                   //0xA3
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.MUL, Argument.AB);                                     //0xA4
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.Undefined);                                            //0xA5
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.MOV, Argument.AT_R0, Argument.DIRECT);                 //0xA6
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.MOV, Argument.AT_R1, Argument.DIRECT);                 //0xA7
        for (int i = 0; i < 8; i++)
            INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.MOV, Argument.R[i], Argument.DIRECT);              //0xA8 -> 0xAF
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.ANL, Argument.C, Argument.SLASH_BIT);                  //0xB0
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.ACALL, Argument.ADDR11);                               //0xB1
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.CPL, Argument.BIT);                                    //0xB2
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.CPL, Argument.C);                                      //0xB3
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 3, Type.CJNE, Argument.A, Argument.DATA, Argument.REL);        //0xB4
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 3, Type.CJNE, Argument.A, Argument.DIRECT, Argument.REL);      //0xB5
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 3, Type.CJNE, Argument.AT_R0, Argument.DATA, Argument.REL);    //0xB6
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 3, Type.CJNE, Argument.AT_R1, Argument.DATA, Argument.REL);    //0xB7
        for (int i = 0; i < 8; i++)
            INSTRUCTIONS[opcode] = new Instruction(opcode++, 3, Type.CJNE, Argument.R[i], Argument.DATA, Argument.REL); //0xB8 -> 0xBF
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.PUSH, Argument.DIRECT);                                //0xC0
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.AJMP, Argument.ADDR11);                                //0xC1
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.CLR, Argument.BIT);                                    //0xC2
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.CLR, Argument.C);                                      //0xC3
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.SWAP, Argument.A);                                     //0xC4
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.XCH, Argument.A, Argument.DIRECT);                     //0xC5
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.XCH, Argument.A, Argument.AT_R0);                      //0xC6
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.XCH, Argument.A, Argument.AT_R1);                      //0xC7
        for (int i = 0; i < 8; i++)
            INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.XCH, Argument.A, Argument.R[i]);                   //0xC8 -> 0xCF
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.POP, Argument.DIRECT);                                 //0xD0
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.ACALL, Argument.ADDR11);                               //0xD1
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.SETB, Argument.BIT);                                   //0xD2
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.SETB, Argument.C);                                     //0xD3
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.DA);                                                   //0xD4
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 3, Type.DJNZ, Argument.DIRECT, Argument.REL);                  //0xD5
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.XCHD, Argument.A, Argument.AT_R0);                     //0xD6
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.XCHD, Argument.A, Argument.AT_R1);                     //0xD7
        for (int i = 0; i < 8; i++)
            INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.DJNZ, Argument.R[i], Argument.REL);                //0xD8 -> 0xDF
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.MOVX, Argument.A, Argument.AT_DPTR);                   //0xE0
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.AJMP, Argument.ADDR11);                                //0xE1
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.MOVX, Argument.A, Argument.AT_R0);                     //0xE2
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.MOVX, Argument.A, Argument.AT_R1);                     //0xE3
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.CLR, Argument.A);                                      //0xE4
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.MOV, Argument.A, Argument.DIRECT);                     //0xE5
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.MOV, Argument.A, Argument.AT_R0);                      //0xE6
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.MOV, Argument.A, Argument.AT_R1);                      //0xE7
        for (int i = 0; i < 8; i++)
            INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.MOV, Argument.A, Argument.R[i]);                   //0xE8 -> 0xEF
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.MOVX, Argument.AT_DPTR, Argument.A);                   //0xF0
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.ACALL, Argument.ADDR11);                               //0xF1
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.MOVX, Argument.AT_R0, Argument.A);                     //0xF2
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.MOVX, Argument.AT_R1, Argument.A);                     //0xF3
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.CPL, Argument.A);                                      //0xF4
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 2, Type.MOV, Argument.DIRECT, Argument.A);                     //0xF5
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.MOV, Argument.AT_R0, Argument.A);                      //0xF6
        INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.MOV, Argument.AT_R1, Argument.A);                      //0xF7
        for (int i = 0; i < 8; i++)
            INSTRUCTIONS[opcode] = new Instruction(opcode++, 1, Type.MOV, Argument.R[i], Argument.A);                   //0xF8 -> 0xFF
    }

    public final Type       type;
    public final int        opcode;
    public final int        size;
    public final Argument[] arguments;
    public final boolean    reverseOperands;

    private Instruction(int opcode, int size, boolean reverseOperands, Type type, Argument... arguments)
    {
        this.opcode = opcode;
        this.size = size;
        this.reverseOperands = reverseOperands;
        this.type = type;
        this.arguments = arguments;
        TYPE_INSTRUCTION_ENUM_MAP.get(type).add(this);
        if (arguments.length == 0) SIMPLE_INSTRUCTIONS.put(type, this);
    }

    private Instruction(int opcode, int size, Type type, Argument... arguments)
    {
        this(opcode, size, false, type, arguments);
    }

    @Override
    public String toString()
    {
        return type + " " + Arrays.toString(arguments);
    }

    public static enum Argument
    {
        ADDR11(1, null, Symbol.Type.LABEL), // 5 msbit of next instruction (A) + 3 msbit of opcode (B) + 1 byte (C) = new address -> AAAA ABBB  CCCC CCCC
        ADDR16(2, null, Symbol.Type.LABEL), // 2 bytes = new address
        DIRECT(1, null, Symbol.Type.DATA),
        REL(1, null, Symbol.Type.LABEL), // rel + next instruction = new address. rel = 2's complement!!
        BIT(1, null, Symbol.Type.BIT),
        DATA(1, '#', Symbol.Type.DATA),
        DATA16(2, '#', Symbol.Type.DATA),
        SLASH_BIT(1, '/', Symbol.Type.BIT),

        A("A"),
        AB("AB"),
        AT_R0("@R0"),
        AT_R1("@R1"),
        R0("R0"),
        R1("R1"),
        R2("R2"),
        R3("R3"),
        R4("R4"),
        R5("R5"),
        R6("R6"),
        R7("R7"),
        C("C"),
        AT_DPTR("@DPTR"),
        DPTR("DPTR"),
        AT_A_PLUS_DPTR("@A+DPTR"),
        AT_A_PLUS_PC("@A+PC");

        static Argument[] R = {Argument.R0, Argument.R1, Argument.R2, Argument.R3, Argument.R4, Argument.R5, Argument.R6, Argument.R7};

        public final String      string;
        public final Character   prefix;
        public final Symbol.Type symbolType;
        public final int         bytesAdded;

        Argument(String string)
        {
            this.bytesAdded = 0;
            this.string = string;
            this.prefix = null;
            this.symbolType = null;
        }

        Argument(int bytesAdded, Character prefix, Symbol.Type symbolType)
        {
            this.bytesAdded = bytesAdded;
            this.prefix = prefix;
            this.symbolType = symbolType;
            this.string = null;
        }
    }

    public static enum Type
    {
        ACALL, ADD, ADDC, AJMP, ANL, CJNE, CLR, CPL, DA, DEC, DIV, DJNZ, INC, JB, JBC, JC, JMP, JNB, JNC, JNZ, JZ, LCALL, LJMP, MOV, MOVC, MOVX, MUL, NOP, ORL, POP, PUSH, RET, RETI, RL, RLC, RR, RRC, SETB, SJMP, SUBB, SWAP, XCH, XCHD, XRL, Undefined;

        public Pattern pattern = Pattern.compile("(?:^|\\s+)" + this.name() + "(?:\\s+|$)", CASE_INSENSITIVE);

        public List<Instruction> getInstructions()
        {
            return Instruction.TYPE_INSTRUCTION_ENUM_MAP.get(this);
        }
    }
}
