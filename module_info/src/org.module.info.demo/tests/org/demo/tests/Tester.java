package org.demo.tests;

import module java.base;
import module java.logging;
import module jdk.jfr;

import module org.module.info.demo;
import module org.module.info.tester;

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
	private static Function<Logger, Consumer<Templet.Result>> reporter()
	{
		return logger -> result -> {
			switch (result) {
			case Templet.FalseAssertions assertions:
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
