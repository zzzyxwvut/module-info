package org.demo.internal;

import module java.base;

import module org.module.info.tester;

/**
 * A service whose provider implementations are expected to collect a list of
 * test results where each result is made by {@link Templet#testEach}.
 */
public interface Testable
{
	/** {@return a list of collected test results} */
	List<Templet.Result> collectResults();
}
