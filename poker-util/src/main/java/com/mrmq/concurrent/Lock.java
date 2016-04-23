package com.mrmq.concurrent;

import java.util.concurrent.TimeUnit;

public class Lock {
	private long timeToWait = 0L;
	private TimeUnit timeUnit;
	
	public Lock(long timeToWait, TimeUnit unit) {
		this.timeToWait = timeToWait;
		this.timeUnit = unit;
	}
	
	public void await() {
		synchronized (this) {
			try {
				this.wait(timeUnit.toMillis(timeToWait));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void wait(Long timeToWait, TimeUnit timeUnit) {
		Object object = new Object();
		synchronized (object) {
			try {
				object.wait(timeToWait);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}