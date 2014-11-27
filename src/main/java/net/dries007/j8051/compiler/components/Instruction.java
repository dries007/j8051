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

import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * The 8051 instruction set
 * More info: http://dries007.net/8051/InstructionSet.htm
 *
 * @author Dries007
 */
public class Instruction implements Component
{
    public static final HashMap<Pattern, Instruction> SIMPLE_INSTRUCTIONS = new HashMap<>();
    public static final Instruction[]                 INSTRUCTIONS        = new Instruction[0x100];

    static
    {
        int opcode = 0;
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "NOP");                                                       //0x00
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "AJMP addr11", 2);                                            //0x01
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "LJMP addr16", 3);                                            //0x02
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "RR A");                                                      //0x03
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "INC A");                                                     //0x04
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "INC direct", 2);                                             //0x05
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "INC @R0");                                                   //0x06
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "INC @R1");                                                   //0x07
        for (int i = 0; i < 8; i++) INSTRUCTIONS[opcode] = new Instruction(opcode++, "INC R" + i);                     //0x08 -> 0x0F
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "JBC bit,rel", 3);                                           //0x10
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "ACALL addr11", 2);                                           //0x11
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "LCALL addr16", 3);                                           //0x12
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "RRC A");                                                     //0x13
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "DEC A");                                                     //0x14
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "DEC direct", 2);                                             //0x15
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "DEC @R0");                                                    //0x16
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "DEC @R1");                                                    //0x17
        for (int i = 0; i < 8; i++) INSTRUCTIONS[opcode] = new Instruction(opcode++, "DEC R" + i);                      //0x18 -> 0x1F
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "JB bit,rel", 3);                                             //0x20
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "AJMP addr11", 2);                                             //0x21
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "RET");                                                        //0x22
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "RL A");                                                       //0x23
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "ADD A,#data", 2);                                            //0x24
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "ADD A,addr16", 2);                                           //0x25
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "ADD A,@R0");                                                 //0x26
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "ADD A,@R1");                                                 //0x27
        for (int i = 0; i < 8; i++) INSTRUCTIONS[opcode] = new Instruction(opcode++, "ADD A,R" + i);                   //0x28 -> 0x2F
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "JNB bit,rel", 3);                                            //0x30
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "ACALL addr11", 2);                                            //0x31
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "RETI");                                                       //0x32
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "RLC A");                                                      //0x33
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "ADDC A,#data", 2);                                           //0x34
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "ADDC A,addr16", 2);                                          //0x35
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "ADDC A,@R0");                                                //0x36
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "ADDC A,@R1");                                                //0x37
        for (int i = 0; i < 8; i++) INSTRUCTIONS[opcode] = new Instruction(opcode++, "ADD A,R" + i);                   //0x38 -> 0x3F
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "JC rel", 2);                                                  //0x40
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "AJMP addr11", 2);                                             //0x41
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "ORL direct,A", 2);                                            //0x42
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "ORL direct,#data", 3);                                        //0x43
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "ORL A,#data", 2);                                            //0x44
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "ORL A,direct", 2);                                           //0x45
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "ORL A,@R0");                                                 //0x46
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "ORL A,@R1");                                                 //0x47
        for (int i = 0; i < 8; i++) INSTRUCTIONS[opcode] = new Instruction(opcode++, "ORL A,R" + i);                   //0x48 -> 0x4F
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "JNC rel", 2);                                                 //0x50
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "ACALL addr11", 2);                                            //0x51
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "ANL direct,A", 2);                                            //0x52
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "ANL direct,#data", 3);                                        //0x53
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "ANL A,#data", 2);                                            //0x54
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "ANL A,direct", 2);                                           //0x55
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "ANL A,@R0");                                                 //0x56
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "ANL A,@R1");                                                 //0x57
        for (int i = 0; i < 8; i++) INSTRUCTIONS[opcode] = new Instruction(opcode++, "ANL A,R" + i);                   //0x58 -> 0x5F
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "JZ rel", 2);                                                  //0x60
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "AJMP addr11", 2);                                             //0x61
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "XRL direct,A", 2);                                            //0x62
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "XRL direct,#data", 3);                                        //0x63
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "XRL A,#data", 2);                                            //0x64
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "XRL A,direct", 2);                                           //0x65
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "XRL A,@R0");                                                 //0x66
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "XRL A,@R1");                                                 //0x67
        for (int i = 0; i < 8; i++) INSTRUCTIONS[opcode] = new Instruction(opcode++, "XRL A,R" + i);                   //0x68 -> 0x6F
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "JNZ rel", 2);                                                 //0x70
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "ACALL addr11", 2);                                            //0x71
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "ORL C,bit", 2);                                               //0x72
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "JMP @A+DPTR");                                                //0x73
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "MOV A,#data", 2);                                             //0x74
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "MOV direct,#data", 3);                                        //0x75
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "MOV @R0,#data", 2);                                           //0x76
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "MOV @R1,#data", 2);                                           //0x77
        for (int i = 0; i < 8; i++) INSTRUCTIONS[opcode] = new Instruction(opcode++, "MOV R" + i + ",#data");           //0x78 -> 0x7F
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "SJMP rel", 2);                                                //0x80
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "AJMP addr11", 2);                                             //0x81
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "ANL C,bit", 2);                                               //0x82
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "MOVC A,@A+PC");                                               //0x83
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "DIV AB");                                                     //0x84
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "MOV direct,direct", 3);                                       //0x85
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "MOV direct,@R0", 2);                                          //0x86
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "MOV direct,@R1", 2);                                          //0x87
        for (int i = 0; i < 8; i++) INSTRUCTIONS[opcode] = new Instruction(opcode++, "MOV direct,R" + i, 2);            //0x88 -> 0x8F
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "MOV DPTR,#data16", 3);                                        //0x90
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "ACALL addr11", 2);                                            //0x91
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "MOV bit,C", 2);                                               //0x92
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "MOVC A,@A+DPTR");                                             //0x93
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "SUBB A,#data", 2);                                            //0x94
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "SUBB A,direct", 2);                                           //0x95
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "SUBB A,@R0");                                                 //0x96
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "SUBB A,@R1");                                                 //0x97
        for (int i = 0; i < 8; i++) INSTRUCTIONS[opcode] = new Instruction(opcode++, "SUBB A,R" + i);                   //0x98 -> 0x9F
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "ORL C,/bit", 2);                                              //0xA0
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "AJMP addr11", 2);                                             //0xA1
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "MOV C,bit", 2);                                               //0xA2
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "INC DPTR");                                                   //0xA3
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "MUL AB");                                                     //0xA4
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "UNDEFINED");                                                  //0xA5
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "MOV @R0,direct", 2);                                          //0xA6
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "MOV @R1,direct", 2);                                          //0xA7
        for (int i = 0; i < 8; i++) INSTRUCTIONS[opcode] = new Instruction(opcode++, "MOV R" + i + ",direct", 2);       //0xA8 -> 0xAF
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "ANL C,/bit", 2);                                              //0xB0
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "ACALL addr11", 2);                                            //0xB1
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "CPL bit", 2);                                                 //0xB2
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "CPL C");                                                      //0xB3
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "CJNE A,#data,rel", 3);                                        //0xB4
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "CJNE A,direct,rel", 3);                                       //0xB5
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "CJNE @R0,#data,rel", 3);                                      //0xB6
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "CJNE @R1,#data,rel", 3);                                      //0xB7
        for (int i = 0; i < 8; i++) INSTRUCTIONS[opcode] = new Instruction(opcode++, "CJNE R" + i + ",#data,rel", 3);   //0xB8 -> 0xBF
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "PUSH direct", 2);                                             //0xC0
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "AJMP addr11", 2);                                             //0xC1
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "CPR bit", 2);                                                 //0xC2
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "CPR C");                                                      //0xC3
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "SWAP A");                                                     //0xC4
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "XCH A,direct", 2);                                            //0xC5
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "XCH A,@R0");                                                  //0xC6
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "XCH A,@R1");                                                  //0xC7
        for (int i = 0; i < 8; i++) INSTRUCTIONS[opcode] = new Instruction(opcode++, "XCH A,R" + i);                    //0xC8 -> 0xCF
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "POP direct", 2);                                              //0xD0
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "ACALL addr11", 2);                                            //0xD1
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "SETB bit", 2);                                                //0xD2
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "SETB C");                                                     //0xD3
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "DA");                                                         //0xD4
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "DJNZ direct,rel", 3);                                         //0xD5
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "XCHD A,@R0");                                                 //0xD6
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "XCHD A,@R1");                                                 //0xD7
        for (int i = 0; i < 8; i++) INSTRUCTIONS[opcode] = new Instruction(opcode++, "DJNZ R" + i + ",rel", 2);         //0xD8 -> 0xDF
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "MOVX A,@DPTR");                                               //0xE0
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "AJMP addr11", 2);                                             //0xE1
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "MOVX A,@R0");                                                 //0xE2
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "MOVX A,@R1");                                                 //0xE3
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "CLR A");                                                      //0xE4
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "MOV A,direct", 2);                                            //0xE5
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "MOV A,@R0");                                                  //0xE6
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "MOV A,@R1");                                                  //0xE7
        for (int i = 0; i < 8; i++) INSTRUCTIONS[opcode] = new Instruction(opcode++, "MOV A,R" + i, 1);                 //0xE8 -> 0xEF
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "MOVX @DPTR,A");                                               //0xF0
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "ACALL addr11", 2);                                            //0xF1
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "MOVX @R0,A");                                                 //0xF2
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "MOVX @R1,A");                                                 //0xF3
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "CPL A");                                                      //0xF4
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "MOV direct,A", 2);                                            //0xF5
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "MOV @R0,A");                                                  //0xF6
        INSTRUCTIONS[opcode] = new Instruction(opcode++, "MOV @R1,A");                                                  //0xF7
        for (int i = 0; i < 8; i++) INSTRUCTIONS[opcode] = new Instruction(opcode++, "MOV R" + i + ",A", 1);            //0xF8 -> 0xFF
    }

    public static final String ADDR11 = "addr11";
    public static final String ADDR16 = "addr16";
    public static final String DIRECT = "direct";
    public static final String REL    = "rel";
    public static final String BIT    = "bit";
    public static final String DATA   = "data";
    public static final String DATA16 = "data16";

    public final int     opcode;
    public final String  mnemonic;
    public final int     size;
    public final boolean isSimple;

    Instruction(int opcode, String mnemonic, int size)
    {
        this.opcode = opcode;
        this.mnemonic = mnemonic;
        this.size = size;

        isSimple = !(mnemonic.contains(ADDR11) || mnemonic.contains(ADDR16) || mnemonic.contains(DIRECT) || mnemonic.contains(REL) || mnemonic.contains(BIT) || mnemonic.contains(DATA) || mnemonic.contains(DATA16));
        if (isSimple)
        {
            SIMPLE_INSTRUCTIONS.put(Pattern.compile(mnemonic.replace(",", ", ?")), this);
        }
    }

    Instruction(int opcode, String mnemonic)
    {
        this(opcode, mnemonic, 1);
    }
}
