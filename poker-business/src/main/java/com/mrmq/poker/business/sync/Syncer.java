package com.mrmq.poker.business.sync;

import java.util.concurrent.TimeUnit;

public interface Syncer<T> extends Runnable {
	public void put(T value) throws InterruptedException;
	public void stop(long timeout, TimeUnit unit);
	public boolean isAlive();
}