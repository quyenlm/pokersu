package com.mrmq.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.Strings;


/**
 * 
 */
public final class IdGenerator {
	private final AtomicLong sequence = new AtomicLong(1);

	public long next() {
		String date = getDate();
		String seq = String.valueOf(this.sequence.getAndIncrement());
		return Long.parseLong(date + Strings.padStart(seq, 9, '0'));
	}
	
	private String getDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		return sdf.format(new Date(System.currentTimeMillis()));
	}
}
