package org.demo.internal;

import java.util.List;

import org.tester.Templet.Result;
import org.tester.Templet;

/**
 * A service whose provider implementations are expected to collect a list of
 * test results where each result is made by {@link Templet#testEach}.
 */
public interface Testable
{
	/** {@return a list of collected test results} */
	List<Result> collectResults();
}
