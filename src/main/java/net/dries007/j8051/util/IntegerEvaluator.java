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

import com.fathzer.soft.javaluator.AbstractEvaluator;
import com.fathzer.soft.javaluator.Function;
import com.fathzer.soft.javaluator.Operator;
import com.fathzer.soft.javaluator.Parameters;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static com.fathzer.soft.javaluator.BracketPair.PARENTHESES;

/**
 * <a href="http://en.wikipedia.org/wiki/Order_of_operations">Following the C preference</a>
 *
 * @author Dries007
 */
public class IntegerEvaluator extends AbstractEvaluator<Integer>
{
    public static final Operator LOGICAL_OR  = new Operator("||", 2, Operator.Associativity.LEFT, 1);
    public static final Operator LOGICAL_AND = new Operator("&&", 2, Operator.Associativity.LEFT, 2);
    public static final Operator BITWISE_OR  = new Operator("|", 2, Operator.Associativity.LEFT, 3);
    public static final Operator BITWISE_XOR = new Operator("^", 2, Operator.Associativity.LEFT, 4);
    public static final Operator BITWISE_AND = new Operator("&", 2, Operator.Associativity.LEFT, 5);

    public static final Operator EQUAL     = new Operator("==", 2, Operator.Associativity.LEFT, 6);
    public static final Operator NOT_EQUAL = new Operator("!=", 2, Operator.Associativity.LEFT, 6);

    public static final Operator LESS_THAN             = new Operator("<", 2, Operator.Associativity.LEFT, 7);
    public static final Operator LESS_THAN_OR_EQUAL    = new Operator("<=", 2, Operator.Associativity.LEFT, 7);
    public static final Operator GREATER_THAN          = new Operator(">", 2, Operator.Associativity.LEFT, 7);
    public static final Operator GREATER_THAN_OR_EQUAL = new Operator(">=", 2, Operator.Associativity.LEFT, 7);

    public static final Operator SHIFT_LEFT  = new Operator("<<", 2, Operator.Associativity.LEFT, 8);
    public static final Operator SHIFT_RIGHT = new Operator(">>", 2, Operator.Associativity.LEFT, 8);

    public static final Operator MINUS = new Operator("-", 2, Operator.Associativity.LEFT, 9);
    public static final Operator PLUS  = new Operator("+", 2, Operator.Associativity.LEFT, 9);

    public static final Operator MULTIPLY = new Operator("*", 2, Operator.Associativity.LEFT, 10);
    public static final Operator DIVIDE   = new Operator("/", 2, Operator.Associativity.LEFT, 10);
    public static final Operator MODULO   = new Operator("%", 2, Operator.Associativity.LEFT, 10);

    public static final Operator LOGICAL_NOT = new Operator("!", 1, Operator.Associativity.RIGHT, 11);
    public static final Operator NEGATE      = new Operator("-", 1, Operator.Associativity.RIGHT, 11);
    public static final Operator COMPLEMENT  = new Operator("~", 1, Operator.Associativity.RIGHT, 11);

    // Used for bit assignments
    public static final Operator DOT  = new Operator(".", 2, Operator.Associativity.LEFT, 12);

    public static final List<Operator> OPERATORS = Arrays.asList(LOGICAL_OR, LOGICAL_AND, BITWISE_OR, BITWISE_XOR, BITWISE_AND, EQUAL, NOT_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL, SHIFT_LEFT, SHIFT_RIGHT, MINUS, PLUS, MULTIPLY, DIVIDE, MODULO, LOGICAL_NOT, NEGATE, COMPLEMENT, DOT);

    public static final Function LOW  = new Function("low", 1);
    public static final Function HIGH = new Function("high", 1);

    public static final List<Function> FUNCTIONS = Arrays.asList(LOW, HIGH);

    private static final Parameters PARAMETERS;

    static
    {
        // Create the evaluator's parameters
        PARAMETERS = new Parameters();
        PARAMETERS.addFunctions(FUNCTIONS);
        PARAMETERS.addOperators(OPERATORS);
        PARAMETERS.addFunctionBracket(PARENTHESES);
        PARAMETERS.addExpressionBracket(PARENTHESES);
    }

    public static final IntegerEvaluator EVALUATOR = new IntegerEvaluator();

    public IntegerEvaluator()
    {
        super(PARAMETERS);
    }

    @Override
    protected Integer evaluate(Operator operator, Iterator<Integer> operands, Object evaluationContext)
    {
        if (LOGICAL_OR == operator) return (operands.next() == 0 && operands.next() == 0) ? 0 : 1;
        if (LOGICAL_AND == operator) return (operands.next() != 0 && operands.next() != 0) ? 1 : 0;
        if (BITWISE_OR == operator) return operands.next() | operands.next();
        if (BITWISE_XOR == operator) return operands.next() ^ operands.next();
        if (BITWISE_AND == operator) return operands.next() & operands.next();
        if (EQUAL == operator) return operands.next().equals(operands.next()) ? 1 : 0;
        if (NOT_EQUAL == operator) return operands.next().equals(operands.next()) ? 0 : 1;
        if (LESS_THAN == operator) return (operands.next() < operands.next()) ? 1 : 0;
        if (LESS_THAN_OR_EQUAL == operator) return (operands.next() <= operands.next()) ? 1 : 0;
        if (GREATER_THAN == operator) return (operands.next() > operands.next()) ? 1 : 0;
        if (GREATER_THAN_OR_EQUAL == operator) return (operands.next() >= operands.next()) ? 1 : 0;
        if (SHIFT_LEFT == operator) return operands.next() << operands.next();
        if (SHIFT_RIGHT == operator) return operands.next() >> operands.next();
        if (MINUS == operator) return operands.next() - operands.next();
        if (PLUS == operator) return operands.next() + operands.next();
        if (MULTIPLY == operator) return operands.next() * operands.next();
        if (DIVIDE == operator) return operands.next() / operands.next();
        if (MULTIPLY == operator) return operands.next() * operands.next();
        if (MODULO == operator) return operands.next() % operands.next();
        if (LOGICAL_NOT == operator) return operands.next() == 0 ? 1 : 0;
        if (NEGATE == operator) return -operands.next();
        if (COMPLEMENT == operator) return ~operands.next();
        if (DOT == operator)
        {
            Integer regiser = operands.next();
            Integer bit = operands.next();
            if (regiser >= 0x20 && regiser < 0x30)
            {
                regiser -= 0x20;
                return (regiser / 2) + regiser % 2 == 0 ? bit : 8 + bit;
            }
            if (regiser >= 0x80 && regiser < 0xFF && regiser % 8 == 0)
            {
                return regiser + regiser - 0x80 % 16 == 0 ? bit : 8 + bit;
            }
            throw new NumberFormatException("Not bit addressable: " + regiser);
        }

        return super.evaluate(operator, operands, evaluationContext);
    }

    @Override
    protected Integer evaluate(Function function, Iterator<Integer> arguments, Object evaluationContext)
    {
        if (LOW == function) return arguments.next() & 0xFF;
        if (HIGH == function) return arguments.next() >> 8;
        return super.evaluate(function, arguments, evaluationContext);
    }

    @Override
    protected Integer toValue(String literal, Object evaluationContext)
    {
        if (evaluationContext != null)
        {
            //noinspection unchecked
            HashMap<String, Integer> map = (HashMap<String, Integer>) evaluationContext;
            if (map.containsKey(literal)) return map.get(literal);
        }
        return Helper.parseToInt(literal);
    }
}
