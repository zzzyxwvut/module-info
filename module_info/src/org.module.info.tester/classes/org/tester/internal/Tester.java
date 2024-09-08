package org.tester.internal;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import org.tester.Templet.Result;
import org.tester.Templet;

/** This class arranges for the invocation of test methods. */
public final class Tester
{
	private Tester() { }

	/**
	 * {@return the total numbers of succeeded and failed invocations of
	 * {@link Predicate#test(Object)} made on each passed predicate object
	 * with every {@code true} value contributing to success and every
	 * {@code false} value contributing to failure} For each passed
	 * predicate object in turn, {@link Templet#setUp()} will be first
	 * invoked, then {@code this} will be passed to
	 * {@code Predicate#test(Object)} and the latter will be invoked on
	 * its passed implementation, and, finally, {@link Templet#tearDown()}
	 * will be invoked.
	 *
	 * @param success an abstraction describing succeeded results
	 * @param failure an abstraction describing failed results
	 * @param templet an instance of testing context
	 * @param methods arbitrary predicate objects
	 * @see Result
	 */
	@SafeVarargs
	public static Result testEach(
			BiFunction<Integer, Integer, Result> success,
			Function<Map<Integer, Optional<RuntimeException>>,
				BiFunction<Integer, Integer, Result>> failure,
			Templet templet,
			Predicate<? super Templet>... methods)
	{
		Objects.requireNonNull(success, "success");
		Objects.requireNonNull(failure, "failure");
		Objects.requireNonNull(templet, "templet");
		Objects.requireNonNull(methods, "methods");
		int ordinal = 0, failed = 0, passed = 0;
		final Map<Integer, Optional<RuntimeException>> falses =
							new LinkedHashMap<>();

		for (Predicate<? super Templet> method : methods) {
			try {
				templet.setUp();

				if (method.test(templet)) {
					++passed;
				} else {
					++failed;
					falses.put(ordinal, Optional.empty());
				}
			} catch (final RuntimeException e) {
				++failed;
				falses.put(ordinal, Optional.of(e));
			} finally {
				try {
					templet.tearDown();
				} catch (final RuntimeException e) {
					/* Unconditionally bump it up. */
					++failed;
					final Optional<RuntimeException> x =
							falses.get(ordinal);

					if (x != null && x.isPresent())
						e.addSuppressed(x.get());

					falses.put(ordinal, Optional.of(e));
				}
			}

			++ordinal;
		}

		return (failed != 0)
			? failure.apply(Collections.unmodifiableMap(falses))
				.apply(failed, passed)
			: success.apply(failed, passed);
	}
}
