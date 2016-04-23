package com.mrmq.poker.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mrmq.concurrent.Lock;
import com.mrmq.poker.common.proto.Heartbeat.HeartbeatMessage;
import com.mrmq.poker.common.proto.Rpc.RpcMessage;
import com.mrmq.poker.common.proto.Rpc.RpcMessage.Result;
import com.mrmq.poker.manager.PokerMananger;
import com.mrmq.poker.service.Publisher;
import com.mrmq.poker.service.Session;
import com.mrmq.util.DateTimeHelper;

public class Heartbeat implements Publisher, Runnable {
	private static final SimpleDateFormat  serverSdf = new SimpleDateFormat(DateTimeHelper.DATE_PATTERN_YYYY_MM_DD_HH_MM_SS);
	
	private static Logger log = LoggerFactory.getLogger(Heartbeat.class);
	private boolean isAlive = true;
	private long heartbeatInterval = 0;
	
	@Override
	public void run() {
		if(heartbeatInterval > 0) {
			log.info("[start] Heart beat...");
			while(isAlive) {
				try {
					Lock.wait(heartbeatInterval, TimeUnit.MILLISECONDS);
					publish();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
			log.info("[end] Heart beat...");
		} else
			log.info("Config heartbeat interval = 0. Do not start Heart beat service!");
	}
	
	public void publish() {
		//Create heartbeat message
		RpcMessage.Builder rpcBuilder = PokerMananger.createRpcMessage();
		rpcBuilder.setResult(Result.SUCCESS);
		
		final ConcurrentMap<String, Session> sessions = PokerMananger.getSessions();
		
		HeartbeatMessage.Builder heartbeatBuilder = HeartbeatMessage.newBuilder();
		heartbeatBuilder.setOnlineUsers(sessions.keySet().size());
		heartbeatBuilder.setServerTime(serverSdf.format(new Date(System.currentTimeMillis())));
		HeartbeatMessage heartBeat = heartbeatBuilder.build();
		log.info("Heartbeat: {}", heartBeat);
		
		rpcBuilder.setPayloadClass(HeartbeatMessage.getDescriptor().getName());
		rpcBuilder.setPayloadData(heartBeat.toByteString());
		
		RpcMessage rpc = rpcBuilder.build();
		
		//publish to clients
		Iterator<String> it = sessions.keySet().iterator();
		Session session = null;
		String key = null;
		while(it.hasNext()) {
			try {
				key = it.next();
				session = sessions.get(key);
				if(session != null && session.isAlive()) {
					session.send(rpc);
				} else {
					//Remove this session from mem
					it.remove();
				}
			} catch (Exception e) {
				log.warn("Cannot send heart beat to " + session, e);
			}
		}
	}

	public long getHeartbeatInterval() {
		return heartbeatInterval;
	}

	public void setHeartbeatInterval(long heartbeatInterval) {
		this.heartbeatInterval = heartbeatInterval;
	}
}