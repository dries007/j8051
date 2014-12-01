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

package net.dries007.j8051.util;

import net.dries007.j8051.util.exceptions.NotBitAddressableException;

/**
 * @author Dries007
 */
public class Helper
{
    private Helper()
    {
    }

    public static int parseToInt(String string)
    {
        char last = string.charAt(string.length() - 1);
        if (last == 'b' || last == 'B') return Integer.parseInt(string.substring(0, string.length() - 1), 2);
        if (last == 'h' || last == 'H') return Integer.parseInt(string.substring(0, string.length() - 1), 16);
        if (last == 'o' || last == 'O') return Integer.parseInt(string.substring(0, string.length() - 1), 8);
        if (string.startsWith("0x")) return Integer.parseInt(string.substring(2), 16);
        return Integer.parseInt(string);
    }

    public static String toHexString(int[] data)
    {
        if (data == null) return "null";
        StringBuilder stringBuilder = new StringBuilder(data.length * 5);
        stringBuilder.append('[');
        for (int i = 0; i < data.length; i++)
        {
            stringBuilder.append(String.format("0x%02X", data[i]));
            if (i + 1 < data.length) stringBuilder.append(", ");
        }
        return stringBuilder.append(']').toString();
    }

    public static int getBitAddress(final int regiser, final int bit)
    {
        if (bit < 0 || bit > 7) throw new NumberFormatException("Bit invalid: " + bit);
        if (regiser >= 0x20 && regiser < 0x30)
        {
            int result = regiser - 0x20;
            return 16 * (result / 2) + (regiser % 2 == 0 ? bit : 8 + bit);
        }
        if (regiser >= 0x80 && regiser < 0xFF && regiser % 8 == 0)
        {
            return regiser + bit;
        }
        throw new NotBitAddressableException(String.format("Bit Not Addressable: 0x%02X.%d", regiser, bit));
    }

    public static String capitalize(String name)
    {
        if (name.length() == 0) return name;
        return Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase();
    }

    public static boolean isNumberAppendix(char c)
    {
        return c == 'h' || c == 'H' || c == 'b' || c == 'B' || c == 'o' || c == 'O';
    }
}
