package com.mrmq.poker.client.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mrmq.poker.admin.AdminClient;
import com.mrmq.poker.client.impl.PokerClient;
import com.mrmq.poker.common.proto.AdminModelProto.User;
import com.mrmq.poker.common.proto.PokerModelProto.Table;

public class PokerBotManager extends Thread {
	private static Logger log = LoggerFactory.getLogger(PokerBotManager.class);
	
	private static CountDownLatch counter = new CountDownLatch(1);
	private AdminClient adminClient;
	
	private static ConcurrentMap<String, Table> curTables = new ConcurrentHashMap<String, Table>();
	private static BlockingQueue<User> freeUsers = new LinkedBlockingQueue<User>();
	private static ConcurrentMap<String, User> allUsers = new ConcurrentHashMap<String, User>();
	
	public PokerBotManager() {
	}
	
	@Override
	public void run() {
		try {
			for(int i = 1; i <= PokerClientManager.getConfigs().getAdminLoadUserTime(); i++) {
				log.info("Get infomartion of player, time: {}", i);
				
				loadUser();
				counter.await(PokerClientManager.getConfigs().getAdminLoadUserTimeout(), TimeUnit.MILLISECONDS);
				
				log.info("There are {} players to play", allUsers.size());
				
				if(allUsers.size() < 1)
					adminClient.stop();
				else
					break;
			}
			
			if(allUsers.size() < 1) {
				log.info("PokerBotManager must be stop");
				return;
			}
			
			freeUsers.addAll(allUsers.values());
			
			//Add player
			demo();
			
			synchronized (adminClient) {
				adminClient.wait();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private static void demo() {
		PokerClient pokerClient = new PokerClient(PokerClientManager.getConfigs().getPokerUrl(), "user1", "123456", 1);
		PokerClientManager.getHandlerService().submit(pokerClient);
	}
	
	private Future<?> loadUser() {
		 return PokerClientManager.getHandlerService().submit(adminClient);
	}
	
	public static synchronized List<User> findFreeUser(int count) {
		List<User> lstUser = new ArrayList<>();
		
		User tempUser = null;
		int findTime = 0;
		
		for(int i = 0; i < count; i++) {
			while(findTime++ < 5) {
				try {
					tempUser = freeUsers.poll(1000, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
				}
				if(tempUser != null) {
					lstUser.add(tempUser);
					break;
				}
			}
		}
		
		return lstUser;
	}

	public static synchronized void onUserFree(String loginId) {
		if(allUsers.containsKey(loginId))
			freeUsers.add(allUsers.get(loginId));
	}
	
	public static CountDownLatch getCounter() {
		return counter;
	}

	public static void setCounter(CountDownLatch counter) {
		PokerBotManager.counter = counter;
	}

	public AdminClient getAdminClient() {
		return adminClient;
	}

	public void setAdminClient(AdminClient adminClient) {
		this.adminClient = adminClient;
	}

	public static BlockingQueue<User> getFreePlayers() {
		return freeUsers;
	}

	public static ConcurrentMap<String, Table> getCurTables() {
		return curTables;
	}

	public static void setCurTables(ConcurrentMap<String, Table> curTables) {
		PokerBotManager.curTables = curTables;
	}

	public static ConcurrentMap<String, User> getAllUsers() {
		return allUsers;
	}
}