package com.mrmq.concurrent;

import java.util.concurrent.atomic.AtomicLong;

/**
 * An AtomicLong with heuristic padding to lessen cache effects of this heavily CAS'ed location.
 * 
 */
public final class PaddedAtomicLong extends AtomicLong {
	//
	private static final long serialVersionUID = -4671483038892198626L;
	
	//
	public volatile long p1, p2, p3, p4, p5, p6 = 7L;

	/**
	 * Default constructor
	 */
	public PaddedAtomicLong() {
	}

	public PaddedAtomicLong(final long initialValue) {
		super(initialValue);
	}

	/**
	 * 
	 */
	public long sumPaddingToPreventOptimisation() {
		return p1 + p2 + p3 + p4 + p5 + p6;
	}
}
