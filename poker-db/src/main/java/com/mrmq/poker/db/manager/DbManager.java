package com.mrmq.poker.db.manager;

import java.util.List;

public interface DbManager<T> {
	public List<T> loadAlls() throws Exception;
	
	public void insert(T instance) throws Exception;
	public T update(T instance) throws Exception;
	public void delete(T instance) throws Exception;
	
	public void insertBatch(List<T> instances) throws Exception;
	public void updateBatch(List<T> instances) throws Exception;
	public void deleteBatch(List<T> instances) throws Exception;
}