package org.demo.tests;

import module java.base;

import module org.module.info.demo;
import module org.module.info.tester;

import org.demo.ArithmeticOperation.*;

/** @hidden */
public final class ArithmeticOperationTests
{
	private ArithmeticOperationTests() { }

	/**
	 * @hidden
	 *
	 * @return an instance of {@code Testable}
	 */
	public static Testable provider()
	{
		return new SampleTests();
	}

	static final class SampleTests implements Testable
	{
		SampleTests() { }

		private static Function<LongBinaryOperator,
			BiFunction<Long, Long, Predicate<Templet>>>
							range_OpTester()
		{
			return op -> (result, zero) -> templet ->
							switch (templet) {
				case LongRangeTemplet templet_ -> templet_
					.stream.reduce(zero, op) == result;
				default -> throw new IllegalStateException();
			};
		}

		@Override
		public List<Templet.Result> collectResults()
		{
			return Arrays.asList(new LongRangeTemplet(() ->
					LongStream.rangeClosed(1L, 8L))
								.testEach(
				range_OpTester()
					.apply(ArithmeticOperation.op()
						.apply(new Plus()))
					.apply(36L, 0L),
				range_OpTester()
					.apply(ArithmeticOperation.op()
						.apply(new Minus()))
					.apply(0L, 36L),
				range_OpTester()
					.apply(ArithmeticOperation.op()
						.apply(new Times()))
					.apply(40_320L, 1L),
				range_OpTester()
					.apply(ArithmeticOperation.op()
						.apply(new Divide()))
					.apply(1L, 40_320L)),

						new LongRangeTemplet(() ->
					LongStream.of()).testEach(),

						new DivideByZeroTemplet()
					.testEach(dummy -> true),

						(new Templet() {}).testEach(
					dummy -> (0L == ArithmeticOperation.op()
				.apply(new Plus())
				.applyAsLong(Long.MAX_VALUE, 1L)),
					dummy -> (0L == ArithmeticOperation.op()
				.apply(new Minus())
				.applyAsLong(Long.MIN_VALUE, 1L)),
					dummy -> (0L == ArithmeticOperation.op()
				.apply(new Plus())
				.applyAsLong(Long.MIN_VALUE, 1L)),
					dummy -> (0L == ArithmeticOperation.op()
				.apply(new Minus())
				.applyAsLong(Long.MAX_VALUE, 1L))));
		}

		static final class LongRangeTemplet extends Templet
		{
			private final Supplier<LongStream> streamSupplier;
			LongStream stream;

			LongRangeTemplet(Supplier<LongStream> streamSupplier)
			{
				this.streamSupplier = streamSupplier;
			}

			@Override
			public void setUp()
			{
				stream = streamSupplier.get();
			}

			@Override
			public void tearDown() { stream = null; }
		}

		static final class DivideByZeroTemplet extends Templet
		{
			@Override
			public void setUp()
			{
				throw new RuntimeException("Oops");
			}

			@Override
			public void tearDown()
			{
				ArithmeticOperation.op()
					.apply(new Divide())
					.applyAsLong(0L, 0L);
			}
		}
	}
}
