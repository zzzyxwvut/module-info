package org.tester;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/** Derived instances of this type admit access to testing facilities. */
public abstract class Templet
{
	/** Constructs a new {@code Templet} object. */
	protected Templet() { }

	/**
	 * This life-cycle method will be invoked <i>before</i>
	 * {@link Predicate#test(Object)} can be invoked on some predicate
	 * object passed to {@link #testEach(Predicate[])}.
	 *
	 * @implSpec
	 * This implementation does nothing.
	 */
	public void setUp() { }

	/**
	 * This life-cycle method will be invoked <i>after</i>
	 * {@link Predicate#test(Object)} can be invoked on some predicate
	 * object passed to {@link #testEach(Predicate[])}.
	 *
	 * @implSpec
	 * This implementation does nothing.
	 */
	public void tearDown() { }

	/**
	 * {@return the total numbers of succeeded and failed invocations of
	 * {@link Predicate#test(Object)} made on each passed predicate object
	 * with every {@code true} value contributing to success and every
	 * {@code false} value contributing to failure} For each passed
	 * predicate object in turn, {@link #setUp()} will be first invoked,
	 * then {@code this} will be passed to {@code Predicate#test(Object)}
	 * and the latter will be invoked on its passed implementation, and,
	 * finally, {@link #tearDown()} will be invoked.
	 *
	 * @param methods arbitrary predicate objects
	 * @see Result
	 */
	@SafeVarargs
	public final Result testEach(Predicate<? super Templet>... methods)
	{
		throw new UnsupportedOperationException("TODO");
	}

	/**
	 * This interface declares methods that summarise test results.
	 * Caught runtime exceptions may be queried by downcasting failed
	 * instances of this type to {@link FalseAssertions}.
	 */
	public sealed interface Result
	{
		/** {@return the total number of failed tests} */
		int failed();

		/** {@return the total number of passed tests} */
		int passed();
	}

	record Success(int failed, int passed) implements Result { }
	record Failure(Map<Integer, Optional<RuntimeException>> falses,
								int failed,
								int passed)
					implements FalseAssertions, Result { }

	/** This interface exposes false assertions. */
	public interface FalseAssertions
	{
		/**
		 * {@return the ordinal map of collected false assertions that
		 * may carry caught runtime exceptions}
		 */
		Map<Integer, Optional<RuntimeException>> falses();
	}
}
