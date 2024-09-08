package org.demo;

import java.util.function.Function;
import java.util.function.LongBinaryOperator;

/** Basic arithmetic operations. */
public sealed interface ArithmeticOperation
{
	/**
	 * Returns a functional interface that takes some arithmetic operation
	 * and returns a functional interface that takes a binary operator
	 * over long values {@code x} and {@code y} and returns a long value
	 * that is the result of applying the arithmetic operation to both
	 * long values.
	 *
	 * @return a curried function
	 * @throws ArithmeticException if {@code y} is zero for operation
	 *	{@link Divide}, or if the result overflows a long for any
	 *	operation
	 */
	static Function<ArithmeticOperation, LongBinaryOperator> op()
	{
		return ao -> (x, y) -> switch (ao) {
			case Plus dummy -> Math.addExact(x, y);
			case Minus dummy -> Math.subtractExact(x, y);
			case Times dummy -> Math.multiplyExact(x, y);
			case Divide dummy -> Math.divideExact(x, y);
		};
	}

	/** An {@link ArithmeticOperation} implementation for addition. */
	record Plus() implements ArithmeticOperation { }

	/** An {@link ArithmeticOperation} implementation for subtraction. */
	record Minus() implements ArithmeticOperation { }

	/** An {@link ArithmeticOperation} implementation for multiplication. */
	record Times() implements ArithmeticOperation { }

	/** An {@link ArithmeticOperation} implementation for division. */
	record Divide() implements ArithmeticOperation { }
}
