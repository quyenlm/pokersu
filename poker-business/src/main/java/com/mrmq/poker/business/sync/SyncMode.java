package com.mrmq.poker.business.sync;

public enum SyncMode {
	BATCH(1), //
	EVENT(2); //
	
	private int value = 1;
	
	SyncMode(int value) {
		this.value = value;
	}
	
	public int getNumber() {
		return value;
	}
}