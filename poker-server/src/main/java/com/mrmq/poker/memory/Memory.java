package com.mrmq.poker.memory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.mrmq.poker.service.Session;

public class Memory {
	//key = loginId/IP
	private static ConcurrentMap<String, Session> hmSession = new ConcurrentHashMap<String, Session>();
	//Key = loginId
	private static ConcurrentMap<String, Session> hmSessionByUser = new ConcurrentHashMap<String, Session>();
	
	private static ConcurrentMap<String, Session> hmAdminSession = new ConcurrentHashMap<String, Session>();

	public static ConcurrentMap<String, Session> getHmSessionByUser() {
		return hmSessionByUser;
	}
	
	public static ConcurrentMap<String, Session> getHmSession() {
		return hmSession;
	}

	public static ConcurrentMap<String, Session> getAdminSession() {
		return hmAdminSession;
	}
}