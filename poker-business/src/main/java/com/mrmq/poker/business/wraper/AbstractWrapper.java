package com.mrmq.poker.business.wraper;

import com.mrmq.poker.db.DbAction;

public abstract class AbstractWrapper<T> implements Wrapper<T> {
	private long eventId;
	private DbAction action;
	private T value;
	
	public AbstractWrapper(DbAction action, T value, long eventId) {
		this.action = action;
		this.value = value;
		this.eventId = eventId;
	}
	
	public long getEventId() {
		return eventId;
	}
	
	public DbAction getAction() {
		return action;
	}
	
	public T getObject() {
		return value;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " [eventId=" + eventId + ", action=" + action + ", value=" + value + "]";
	}
}
