package com.mrmq.poker.business.wraper;

import com.mrmq.poker.db.DbAction;

public interface Wrapper<T> {
	public long getEventId();
	public DbAction getAction();
	public T getObject();
}
