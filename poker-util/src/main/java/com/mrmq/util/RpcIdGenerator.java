package com.mrmq.util;

import java.text.SimpleDateFormat;

import com.mrmq.concurrent.PaddedAtomicLong;

/**
 * 
 */
public final class RpcIdGenerator {
	//
	private static final int APP_ID_BITS = 8;
	private static final int SEQUENCE_BITS = 12;
	private static final int TIMESTAMP_BITS = 43;
	private static final long APP_ID_MASK = (1L << APP_ID_BITS) - 1L;
	private static final long SEQUENCE_MASK = (1L << SEQUENCE_BITS) - 1L;
	private static final long TIMESTAMP_MASK = (1L << TIMESTAMP_BITS) - 1L;
	private static final long TIMESTAMP_OFFSET = parse("2000-01-01 00:00:00.000");
	
	//
	private final short appId;
	private final PaddedAtomicLong sequence = new PaddedAtomicLong(1);

	/**
	 * 
	 */
	public RpcIdGenerator() {
		this((short)0);
	}
	
	public RpcIdGenerator(short appId) {
		this.appId = appId;
	}

	/**
	 * 
	 */
	public long next() {
		final long id = this.appId & APP_ID_MASK;
		final long seq = this.sequence.getAndIncrement() & SEQUENCE_MASK;
		final long now = (System.currentTimeMillis() - TIMESTAMP_OFFSET) & TIMESTAMP_MASK;
		return (now << (SEQUENCE_BITS + APP_ID_BITS)) | (seq << APP_ID_BITS) | id;
	}
	
	/**
	 * 
	 */
	public short getAppId() {
		return appId;
	}
	
	public static short getAppId(long id) {
		return (short)(id & APP_ID_MASK);
	}
	
	public static long getSequence(long id) {
		return (id >> APP_ID_BITS) & SEQUENCE_MASK;
	}
	
	public static long getTimestamp(long id) {
		return (id >> (SEQUENCE_BITS + APP_ID_BITS)) + TIMESTAMP_OFFSET;
	}
	
	/**
	 * 
	 */
	private static final long parse(String timestamp) {
		try {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(timestamp).getTime();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
