package com.mrmq.poker.business.sync;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mrmq.poker.business.wraper.AbstractWrapper;
import com.mrmq.poker.db.UnsupportActionException;
import com.mrmq.poker.db.manager.DbManager;

public class AbstractSyncer<T extends AbstractWrapper<V>, V> implements Syncer<T> {
	protected Logger log = LoggerFactory.getLogger(this.getClass());
	
	private SyncMode syncMode = SyncMode.EVENT;
	private int batchSize = 1; 
	private long batchTime = 10000; //miliseconds
	
	protected AtomicBoolean isAlive = new AtomicBoolean(false);
	private BlockingQueue<T> queue = new LinkedBlockingQueue<T>();
	private DbManager<V> dbManager;
	
	public void put(T value) throws InterruptedException {
		this.queue.put(value);
	}
	
	public void run() {
		log.info("{} started", this.getClass());
		isAlive.set(true);
		
		while(isAlive.get() || queue.size() > 0) {
			try {
				if(SyncMode.EVENT == syncMode)
					handeEvent();
				else {
					Thread.sleep(batchTime);
					handeBatch();
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}			
		}
		
		log.warn("{} stopped", this.getClass());
	}

	private void handeEvent() throws Exception {
		T value;
		
		value = queue.poll(1000, TimeUnit.MILLISECONDS);
		
		if(value != null) {
			try {
				switch (value.getAction()) {
				case INSERT:
					dbManager.insert(value.getObject());
					log.info("Inserted {}", value);
					break;
					
				case UPDATE:
					dbManager.update(value.getObject());
					log.info("Updated {}", value);
					break;
					
				case DELETE:
					dbManager.delete(value.getObject());
					log.info("Deleted {}", value);
					break;
				default:
					throw new UnsupportActionException("Unsupport action " + value.getAction() + " with object: " + value.getObject());
				}
			} catch (SQLException e) {
				log.warn(String.format("Cannot sync {}. Enqueue item for sync later!", value), e);
				put(value);
			} catch (UnsupportActionException e) {
				log.error("Cannot sync {}", value);
				throw e;
			} catch (Exception e) {
				log.error("Cannot sync {}", value);
				throw e;
			}
		}
	}
	
	private void handeBatch() throws Exception {		
		log.info("[start] hande Batch, {}", getClass().getName());
		List<T> values = new ArrayList<T>();
		
		//Move all element to temp list
		queue.drainTo(values);
		
		List<V> lstInserts = new ArrayList<V>();
		List<V> lstUpdates = new ArrayList<V>();
		List<V> lstDeletes = new ArrayList<V>();
		
		if(values.size() > 0) {
			T value;
			Iterator<T> it = values.iterator();
			
			//Filt
			while(it.hasNext()) {
				value = it.next();
				
				switch (value.getAction()) {
					case INSERT:
						lstInserts.add(value.getObject());
						dbManager.insert(value.getObject());
						log.info("Inserted {}", value);
						break;
						
					case UPDATE:
						lstUpdates.add(value.getObject());
						dbManager.update(value.getObject());
						log.info("Updated {}", value);
						break;
						
					case DELETE:
						lstDeletes.add(value.getObject());
						dbManager.delete(value.getObject());
						log.info("Deleted {}", value);
						break;
					default:
						log.error("Unsupport action " + value.getAction() + " with object: " + value.getObject());
				}
			}
			
			//Insert batch
			if(lstInserts.size() > 0) {
				log.info("[start] Insert {} items to DB", lstInserts.size());
				dbManager.insertBatch(lstInserts);
				log.info("[end] Insert {} items to DB", lstInserts.size());
			}
			
			//Update batch
			if(lstUpdates.size() > 0) {
				log.info("[start] Update {} items to DB", lstUpdates.size());
				dbManager.updateBatch(lstUpdates);
				log.info("[end] Update {} items to DB", lstUpdates.size());
			}
			
			//Delete batch
			if(lstDeletes.size() > 0) {
				log.info("[start] Delete {} items to DB", lstDeletes.size());
				dbManager.deleteBatch(lstDeletes);
				log.info("[end] Delete {} items to DB", lstDeletes.size());
			}
		}
		
		log.info("[end] hande Batch, {}", getClass().getName());
	}
	
	@Override
	public void stop(long timeout, TimeUnit unit) {
		this.isAlive.set(false);
	}

	@Override
	public boolean isAlive() {
		return isAlive.get();
	}
	
	public DbManager<V> getDbManager() {
		return dbManager;
	}

	public void setDbManager(DbManager<V> dbManager) {
		this.dbManager = dbManager;
	}

	public SyncMode getSyncMode() {
		return syncMode;
	}

	public void setSyncMode(SyncMode syncMode) {
		this.syncMode = syncMode;
	}
	
	public int getBatchSize() {
		return batchSize;
	}
	
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public long getBatchTime() {
		return batchTime;
	}
	
	public void setBatchTime(long batchTime) {
		this.batchTime = batchTime;
	}
}