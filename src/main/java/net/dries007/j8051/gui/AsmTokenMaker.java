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

package net.dries007.j8051.gui;

import net.dries007.j8051.compiler.Instruction;
import net.dries007.j8051.util.Helper;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMaker;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;

import javax.swing.text.Segment;

/**
 * @author Dries007
 */
public class AsmTokenMaker extends AbstractTokenMaker
{
    private int currentTokenStart;
    private int currentTokenType;
    private int startTokenType;

    @Override
    public TokenMap getWordsToHighlight()
    {
        TokenMap tokenMap = new TokenMap(true);

        tokenMap.put("#include", Token.PREPROCESSOR);
        tokenMap.put("#define", Token.PREPROCESSOR);
        tokenMap.put("#undefine", Token.PREPROCESSOR);
        tokenMap.put("#ifdef", Token.PREPROCESSOR);
        tokenMap.put("#ifndef", Token.PREPROCESSOR);
        tokenMap.put("#else", Token.PREPROCESSOR);
        tokenMap.put("#endif", Token.PREPROCESSOR);

        for (Instruction.Type type : Instruction.Type.values())
        {
            tokenMap.put(type.name().toLowerCase(), Token.RESERVED_WORD);
        }

        tokenMap.put("equ", Token.FUNCTION);
        tokenMap.put("data", Token.FUNCTION);
        tokenMap.put("bit", Token.FUNCTION);
        tokenMap.put("db", Token.FUNCTION);
        tokenMap.put("dw", Token.FUNCTION);
        tokenMap.put("ds", Token.FUNCTION);

        tokenMap.put("low", Token.FUNCTION);
        tokenMap.put("high", Token.FUNCTION);
        tokenMap.put("org", Token.FUNCTION);
        tokenMap.put("end", Token.FUNCTION);

        return tokenMap;
    }

    @Override
    public String[] getLineCommentStartAndEnd(int languageIndex) {
        return new String[] { ";", null };
    }

    @Override
    public boolean getMarkOccurrencesOfTokenType(int type) {
        return type==Token.IDENTIFIER || type==Token.VARIABLE;
    }

    @Override
    public void addToken(Segment segment, int start, int end, int tokenType, int startOffset)
    {
        // This assumes all keywords, etc. were parsed as "identifiers."
        if (tokenType == Token.IDENTIFIER)
        {
            int value = wordsToHighlight.get(segment, start, end);
            if (value != -1)
            {
                tokenType = value;
            }
        }
        super.addToken(segment, start, end, tokenType, startOffset);
    }

    @Override
    public Token getTokenList(Segment text, int initialTokenType, int startOffset)
    {
        resetTokenList();

        boolean prepocessor = false;

        char[] array = text.array;
        int offset = text.offset;
        int count = text.count;
        int end = offset + count;

        // Token starting offsets are always of the form:
        // 'startOffset + (currentTokenStart-offset)', but since startOffset and
        // offset are constant, tokens' starting positions become:
        // 'newStartOffset+currentTokenStart'.
        int newStartOffset = startOffset - offset;

        currentTokenStart = offset;
        currentTokenType  = startTokenType;

        for (int i=offset; i<end; i++) {

            char c = array[i];

            switch (currentTokenType) {

                //case Token.SEPARATOR:
                case Token.NULL:

                    currentTokenStart = i;   // Starting a new token here.

                    switch (c) {

                        case ' ':
                        case '\t':
                            currentTokenType = Token.WHITESPACE;
                            break;

                        case '\'':
                            if (!prepocessor) currentTokenType = Token.LITERAL_CHAR;
                            break;

                        case '"':
                            if (!prepocessor) currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
                            break;

                        case ';':
                            if (!prepocessor) currentTokenType = Token.COMMENT_EOL;
                            break;

                        case '#':
                            prepocessor = true;
                            currentTokenType = Token.PREPROCESSOR;
                            break;

                        default:
                            if (RSyntaxUtilities.isDigit(c)) {
                                currentTokenType = Token.LITERAL_NUMBER_HEXADECIMAL;
                                break;
                            }
                            else if (RSyntaxUtilities.isLetter(c) || c=='/' || c=='_') {
                                currentTokenType = Token.IDENTIFIER;
                                break;
                            }

                            // Anything not currently handled - mark as an identifier
                            currentTokenType = Token.IDENTIFIER;
                            break;

                    } // End of switch (c).

                    break;

                case Token.SEPARATOR:
                case Token.WHITESPACE:

                    switch (c) {

                        case ' ':
                        case '\t':
                            break;   // Still whitespace.

                        case '\'':
                            if (!prepocessor)
                            {
                                addToken(text, currentTokenStart,i-1, Token.WHITESPACE, newStartOffset+currentTokenStart);
                                currentTokenStart = i;
                                currentTokenType = Token.LITERAL_CHAR;
                            }
                            break;

                        case '"':
                            if (!prepocessor)
                            {
                                addToken(text, currentTokenStart,i-1, Token.WHITESPACE, newStartOffset+currentTokenStart);
                                currentTokenStart = i;
                                currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
                            }
                            break;

                        case ';':
                            if (!prepocessor)
                            {
                                addToken(text, currentTokenStart, i - 1, Token.WHITESPACE, newStartOffset + currentTokenStart);
                                currentTokenStart = i;
                                currentTokenType = Token.COMMENT_EOL;
                            }
                            break;

                        default:   // Add the whitespace token and start anew.

                            addToken(text, currentTokenStart,i-1, Token.WHITESPACE, newStartOffset+currentTokenStart);
                            currentTokenStart = i;

                            if (RSyntaxUtilities.isHexCharacter(c) || c == '#') {
                                currentTokenType = Token.LITERAL_NUMBER_HEXADECIMAL;
                                break;
                            }
                            else if (RSyntaxUtilities.isLetter(c) || c=='/' || c=='_') {
                                currentTokenType = Token.IDENTIFIER;
                                break;
                            }

                            // Anything not currently handled - mark as identifier
                            currentTokenType = Token.IDENTIFIER;

                    } // End of switch (c).

                    break;

                default: // Should never happen
                case Token.IDENTIFIER:

                    switch (c) {

//                        case ',':
//                            addToken(text, currentTokenStart,i-1, Token.IDENTIFIER, newStartOffset+currentTokenStart);
//                            currentTokenStart = i;
//                            currentTokenType = Token.SEPARATOR;
//                            break;

                        case ':':
                        case ' ':
                        case '\t':
                            addToken(text, currentTokenStart,i-1, Token.IDENTIFIER, newStartOffset+currentTokenStart);
                            currentTokenStart = i;
                            currentTokenType = Token.WHITESPACE;
                            break;

                        case '"':
                            addToken(text, currentTokenStart,i-1, Token.IDENTIFIER, newStartOffset+currentTokenStart);
                            currentTokenStart = i;
                            currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
                            break;

                        default:
                            if (RSyntaxUtilities.isLetterOrDigit(c) || c=='/' || c=='_') {
                                break;   // Still an identifier of some type.
                            }
                            // Otherwise, we're still an identifier (?).

                    } // End of switch (c).

                    break;

                case Token.LITERAL_NUMBER_HEXADECIMAL:

                    switch (c) {

                        case ' ':
                        case '\t':
                            addToken(text, currentTokenStart,i-1, Token.LITERAL_NUMBER_HEXADECIMAL, newStartOffset+currentTokenStart);
                            currentTokenStart = i;
                            currentTokenType = Token.WHITESPACE;
                            break;

                        case '"':
                            addToken(text, currentTokenStart,i-1, Token.LITERAL_NUMBER_HEXADECIMAL, newStartOffset+currentTokenStart);
                            currentTokenStart = i;
                            currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
                            break;

                        default:

                            if (RSyntaxUtilities.isHexCharacter(c)) {
                                break;   // Still a literal number.
                            }
                            if (Helper.isNumberAppendix(c)) {
                                break;
                            }

                            // Otherwise, remember this was a number and start over.
                            addToken(text, currentTokenStart,i-1, Token.LITERAL_NUMBER_HEXADECIMAL, newStartOffset+currentTokenStart);
                            i--;
                            currentTokenType = Token.NULL;

                    } // End of switch (c).

                    break;

                case Token.COMMENT_EOL:
                    i = end - 1;
                    addToken(text, currentTokenStart,i, currentTokenType, newStartOffset+currentTokenStart);
                    // We need to set token type to null so at the bottom we don't add one more token.
                    currentTokenType = Token.NULL;
                    break;

                case Token.PREPROCESSOR:
                    i = end - 1;
                    addToken(text, currentTokenStart,i, currentTokenType, newStartOffset+currentTokenStart);
                    // We need to set token type to null so at the bottom we don't add one more token.
                    currentTokenType = Token.NULL;
                    break;

                case Token.LITERAL_STRING_DOUBLE_QUOTE:
                    if (c=='"') {
                        addToken(text, currentTokenStart,i, Token.LITERAL_STRING_DOUBLE_QUOTE, newStartOffset+currentTokenStart);
                        currentTokenType = Token.NULL;
                    }
                    break;

            } // End of switch (currentTokenType).

        } // End of for (int i=offset; i<end; i++).

        switch (currentTokenType) {

            // Remember what token type to begin the next line with.
            case Token.LITERAL_STRING_DOUBLE_QUOTE:
                addToken(text, currentTokenStart,end-1, currentTokenType, newStartOffset+currentTokenStart);
                break;

            // Do nothing if everything was okay.
            case Token.NULL:
                addNullToken();
                break;

            // All other token types don't continue to the next line...
            default:
                addToken(text, currentTokenStart,end-1, currentTokenType, newStartOffset+currentTokenStart);
                addNullToken();

        }

        // Return the first token in our linked list.
        return firstToken;
    }
}
