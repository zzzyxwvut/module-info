/**
 * Defines demo related support.
 *
 * Note that the {@code Testable} service is not exported.
 *
 * @uses org.demo.internal.Testable
 */
module org.module.info.demo
{
	/* For IDE'd peace of mind. */
	requires static jdk.jfr;
	requires static java.logging;
	requires transitive org.module.info.tester;

	exports org.demo;

	uses org.demo.internal.Testable;
}
