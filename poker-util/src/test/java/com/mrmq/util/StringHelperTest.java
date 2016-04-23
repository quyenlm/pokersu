package com.mrmq.util;

import com.mrmq.util.StringHelper;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class StringHelperTest extends TestCase {
	public StringHelperTest(String testName) {
		super(testName);
	}

	public static Test suite() {
		return new TestSuite(StringHelperTest.class);
	}

	public void testEmpty() {
		assertTrue(StringHelper.isEmpty(""));
	}
	
	public void testNull() {
		assertTrue(StringHelper.isEmpty(null));
	}
	
	public void testNoNull() {
		assertFalse(StringHelper.isEmpty("null"));
	}
}
