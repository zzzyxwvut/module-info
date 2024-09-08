package org.demo.tests;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import java.util.logging.Logger;
import java.util.logging.LogManager;

import jdk.jfr.consumer.EventStream;
import jdk.jfr.consumer.RecordedStackTrace;
import jdk.jfr.consumer.RecordingStream;

import org.tester.Templet.FalseAssertions;
import org.tester.Templet.Result;

import org.demo.internal.Testable;

final class Tester
{
	static {
		LoggingSupport.updateConfiguration(Tester.class,
						"logging.properties");
	}

	private Tester() { }

	private static Function<Stream<RuntimeException>, Stream<String>>
							limitedMapper()
	{
		return exceptions -> exceptions
			.flatMap(exception -> Arrays.stream(
					Optional.ofNullable(exception
								.getCause())
						.orElse(exception)
						.getStackTrace())
				.limit(4L)
				.map(StackTraceElement::toString));
	}

	@SuppressWarnings("fallthrough")
	private static Function<Logger, Consumer<Result>> reporter()
	{
		return logger -> result -> {
			switch (result) {
			case FalseAssertions assertions:
				logger.info(assertions.falses()
					.values()
					.stream()
					.flatMap(limitedMapper()
						.compose(Optional::stream))
					.collect(Collectors.joining(
						System.lineSeparator(),
						System.lineSeparator(),
						System.lineSeparator())));
				/* FALL THROUGH. */
			default:
				logger.info(result.toString());
			}
		};
	}

	private static Optional<Testable> lookUpAnyTestableProvider()
	{
		return ServiceLoader.load(ModuleLayer.boot(), Testable.class)
			.findFirst();
	}

	private static void dumpStackForSampleTests(RecordedStackTrace stackTrace)
	{
		LoggingSupport.getLogger(Tester.class)
			.orElseThrow(() -> new IllegalStateException(
							"No logger"))
			.info((stackTrace == null)
				? "[No stack trace]"
				: stackTrace.getFrames().toString());
	}

	public static void main(String[] args) throws InterruptedException
	{
		try (EventStream stream = new RecordingStream()) {
			stream.onEvent("jdk.ClassLoad", event -> {
				if ("org.demo.tests.ArithmeticOperationTests$SampleTests"
						.equals(event.getClass(
								"loadedClass")
							.getName()))
					dumpStackForSampleTests(event
							.getStackTrace());
			});
			stream.startAsync();
			lookUpAnyTestableProvider()
				.map(Testable::collectResults)
				.orElseThrow(() -> new IllegalStateException(
								"No provider"))
				.stream()
				.forEachOrdered(reporter().apply(
					LoggingSupport.getLogger(Tester.class)
						.orElseThrow(() ->
							new IllegalStateException(
								"No logger"))));
			stream.awaitTermination(Duration.ofSeconds(2L));
		}
	}

	static final class LoggingSupport
	{
		static void updateConfiguration(Class<?> klass,
							String resource)
		{
			try (InputStream is = klass.getResourceAsStream(
								resource)) {
				if (is == null)
					throw new IllegalArgumentException(
						String.format(
							"Unavailable resource: '%s'",
								resource));

				/*
				 * Resolve all conflicting properties in
				 * favour of new non-null values, else retain
				 * old non-null values.
				 */
				LogManager.getLogManager()
					.updateConfiguration(is, property ->
							(oldValue, newValue) ->
						(oldValue == null &&
							newValue == null)
						/* Discard the property. */
						? null
						: (newValue == null)
							? oldValue
							: newValue);
			} catch (final IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		static Optional<Logger> getLogger(Class<?> klass)
		{
			return Optional.ofNullable(
					Logger.getLogger(klass.getName()));
		}
	}
}
