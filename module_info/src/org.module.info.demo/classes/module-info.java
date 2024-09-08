/** Defines demo related support. */
module org.module.info.demo
{
	requires static jdk.jfr;
	requires static java.logging;
	requires transitive org.module.info.tester;

	exports org.demo;
}
