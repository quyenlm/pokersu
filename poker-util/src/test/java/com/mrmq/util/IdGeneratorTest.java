package com.mrmq.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author quyen.le.manh
 *
 */
public class IdGeneratorTest extends TestCase {
	public IdGeneratorTest(String testName) {
		super(testName);
	}

	public static Test suite() {
		return new TestSuite(IdGenerator.class);
	}

	public void testGenId() {
		IdGenerator idGenerator = new IdGenerator();
		long value = idGenerator.next();
		
		System.out.println("fdfd" + value);
		
		assertTrue(StringHelper.isEmpty(""));
	}
}

