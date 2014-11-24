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

/**
 * @author Dries007
 */
public class Instruction
{
    public static final Instruction[] INSTRUCTIONS = new Instruction[0xFF];
    static
    {
        int opcode = 0;
        INSTRUCTIONS[opcode++] = new Instruction("NOP");                                        //0x00
        INSTRUCTIONS[opcode++] = new Instruction("AJMP addr11", 2);                             //0x01
        INSTRUCTIONS[opcode++] = new Instruction("LJMP addr16", 3);                             //0x02
        INSTRUCTIONS[opcode++] = new Instruction("RR A");                                       //0x03
        INSTRUCTIONS[opcode++] = new Instruction("INC A");                                      //0x04
        INSTRUCTIONS[opcode++] = new Instruction("INC direct", 2);                              //0x05
        INSTRUCTIONS[opcode++] = new Instruction("INC @R0");                                    //0x06
        INSTRUCTIONS[opcode++] = new Instruction("INC @R1");                                    //0x07
        for (int i = 0; i < 8; i++) INSTRUCTIONS[opcode++] = new Instruction("INC R" + i);      //0x08 -> 0x0F
        INSTRUCTIONS[opcode++] = new Instruction("JBC bit, rel", 3);                            //0x10
        INSTRUCTIONS[opcode++] = new Instruction("ACALL addr11", 2);                            //0x11
        INSTRUCTIONS[opcode++] = new Instruction("LCALL addr16", 3);                            //0x12
        INSTRUCTIONS[opcode++] = new Instruction("RRC A");                                      //0x13
        INSTRUCTIONS[opcode++] = new Instruction("DEC A");                                      //0x14
        INSTRUCTIONS[opcode++] = new Instruction("DEC direct", 2);                              //0x15
        INSTRUCTIONS[opcode++] = new Instruction("DEC @R0");                                    //0x16
        INSTRUCTIONS[opcode++] = new Instruction("DEC @R1");                                    //0x17
        for (int i = 0; i < 8; i++) INSTRUCTIONS[opcode++] = new Instruction("DEC R" + i);      //0x18 -> 0x1F
        INSTRUCTIONS[opcode++] = new Instruction("JB bit, rel", 3);                             //0x20
        INSTRUCTIONS[opcode++] = new Instruction("AJMP addr11", 2);                             //0x21
        INSTRUCTIONS[opcode++] = new Instruction("RET");                                        //0x22
        INSTRUCTIONS[opcode++] = new Instruction("RL A");                                       //0x23
        INSTRUCTIONS[opcode++] = new Instruction("ADD A, #data", 2);                            //0x24
        INSTRUCTIONS[opcode++] = new Instruction("ADD A, addr16", 2);                           //0x25
        INSTRUCTIONS[opcode++] = new Instruction("ADD A, @R0");                                 //0x26
        INSTRUCTIONS[opcode++] = new Instruction("ADD A, @R1");                                 //0x27
        for (int i = 0; i < 8; i++) INSTRUCTIONS[opcode++] = new Instruction("ADD A, R" + i);   //0x28 -> 0x2F
        INSTRUCTIONS[opcode++] = new Instruction("JNB bit, rel", 3);                            //0x30
        INSTRUCTIONS[opcode++] = new Instruction("ACALL addr11", 2);                            //0x31
        INSTRUCTIONS[opcode++] = new Instruction("RETI");                                       //0x32
        INSTRUCTIONS[opcode++] = new Instruction("RLC A");                                      //0x33
        INSTRUCTIONS[opcode++] = new Instruction("ADDC A, #data", 2);                           //0x34
        INSTRUCTIONS[opcode++] = new Instruction("ADDC A, addr16", 2);                          //0x35
        INSTRUCTIONS[opcode++] = new Instruction("ADDC A, @R0");                                //0x36
        INSTRUCTIONS[opcode++] = new Instruction("ADDC A, @R1");                                //0x37
        for (int i = 0; i < 8; i++) INSTRUCTIONS[opcode++] = new Instruction("ADD A, R" + i);   //0x38 -> 0x3F
        INSTRUCTIONS[opcode++] = new Instruction("JC rel", 2);                                  //0x40
        INSTRUCTIONS[opcode++] = new Instruction("AJMP addr11", 2);                             //0x41
        INSTRUCTIONS[opcode++] = new Instruction("ORL direct,A", 2);                            //0x42
        INSTRUCTIONS[opcode++] = new Instruction("ORL direct,#data", 3);                        //0x43
        INSTRUCTIONS[opcode++] = new Instruction("ORL A, #data", 2);                            //0x44
        INSTRUCTIONS[opcode++] = new Instruction("ORL A, direct", 2);                           //0x45
        INSTRUCTIONS[opcode++] = new Instruction("ORL A, @R0");                                 //0x46
        INSTRUCTIONS[opcode++] = new Instruction("ORL A, @R1");                                 //0x47
        for (int i = 0; i < 8; i++) INSTRUCTIONS[opcode++] = new Instruction("ORL A, R" + i);   //0x48 -> 0x4F
        INSTRUCTIONS[opcode++] = new Instruction("JNC rel", 2);                                 //0x50
        INSTRUCTIONS[opcode++] = new Instruction("ACALL addr11", 2);                            //0x51
        INSTRUCTIONS[opcode++] = new Instruction("ANL direct,A", 2);                            //0x52
        INSTRUCTIONS[opcode++] = new Instruction("ANL direct,#data", 3);                        //0x53
        INSTRUCTIONS[opcode++] = new Instruction("ANL A, #data", 2);                            //0x54
        INSTRUCTIONS[opcode++] = new Instruction("ANL A, direct", 2);                           //0x55
        INSTRUCTIONS[opcode++] = new Instruction("ANL A, @R0");                                 //0x56
        INSTRUCTIONS[opcode++] = new Instruction("ANL A, @R1");                                 //0x57
        for (int i = 0; i < 8; i++) INSTRUCTIONS[opcode++] = new Instruction("ANL A, R" + i);   //0x58 -> 0x5F
        INSTRUCTIONS[opcode++] = new Instruction("JZ rel", 2);                                  //0x60
        INSTRUCTIONS[opcode++] = new Instruction("AJMP addr11", 2);                             //0x61
        INSTRUCTIONS[opcode++] = new Instruction("XRL direct,A", 2);                            //0x62
        INSTRUCTIONS[opcode++] = new Instruction("XRL direct,#data", 3);                        //0x63
        INSTRUCTIONS[opcode++] = new Instruction("XRL A, #data", 2);                            //0x64
        INSTRUCTIONS[opcode++] = new Instruction("XRL A, direct", 2);                           //0x65
        INSTRUCTIONS[opcode++] = new Instruction("XRL A, @R0");                                 //0x66
        INSTRUCTIONS[opcode++] = new Instruction("XRL A, @R1");                                 //0x67
        for (int i = 0; i < 8; i++) INSTRUCTIONS[opcode++] = new Instruction("XRL A, R" + i);   //0x68 -> 0x6F
        INSTRUCTIONS[opcode++] = new Instruction("JNZ rel", 2);                                 //0x70
        INSTRUCTIONS[opcode++] = new Instruction("ACALL addr11", 2);                            //0x71
        INSTRUCTIONS[opcode++] = new Instruction("ORL C,bit", 2);                               //0x72
        INSTRUCTIONS[opcode++] = new Instruction("JMP @A+DPTR", 1);                             //0x73
        INSTRUCTIONS[opcode++] = new Instruction("MOV A,#data", 2);                             //0x74
        INSTRUCTIONS[opcode++] = new Instruction("MOV direct,#data", 3);                        //0x75
        INSTRUCTIONS[opcode++] = new Instruction("MOV @R0,#data", 2);                           //0x76
        INSTRUCTIONS[opcode++] = new Instruction("MOV @R1,#data", 2);                           //0x77
        for (int i = 0; i < 8; i++) INSTRUCTIONS[opcode++] = new Instruction("MOV R" + i + ",#data");   //0x78 -> 0x7F
        INSTRUCTIONS[opcode++] = new Instruction("SJMP rel", 2);                                //0x80
        INSTRUCTIONS[opcode++] = new Instruction("AJMP addr11", 2);                             //0x81
        INSTRUCTIONS[opcode++] = new Instruction("ANL C,bit", 2);                               //0x82
//        INSTRUCTIONS[opcode++] = new Instruction("JMP @A+DPTR", 1);                             //0x83 TODO
//        INSTRUCTIONS[opcode++] = new Instruction("MOV A,#data", 2);                             //0x84
//        INSTRUCTIONS[opcode++] = new Instruction("MOV direct,#data", 3);                        //0x85
//        INSTRUCTIONS[opcode++] = new Instruction("MOV @R0,#data", 2);                           //0x86
//        INSTRUCTIONS[opcode++] = new Instruction("MOV @R1,#data", 2);                           //0x87
//        for (int i = 0; i < 8; i++) INSTRUCTIONS[opcode++] = new Instruction("MOV R" + i + ",#data");   //0x88 -> 0x8F
    }

    public final String mnemonic;
    public final int size;

    Instruction(String mnemonic, int size)
    {
        this.mnemonic = mnemonic;
        this.size = size;
    }

    Instruction(String mnemonic)
    {
        this.mnemonic = mnemonic;
        this.size = 1;
    }
}
