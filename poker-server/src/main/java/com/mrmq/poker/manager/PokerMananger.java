package com.mrmq.poker.manager;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.protobuf.ByteString;
import com.mrmq.poker.business.impl.PokerBusiness;
import com.mrmq.poker.common.glossary.ServiceType;
import com.mrmq.poker.common.proto.PokerModelProto.Hall;
import com.mrmq.poker.common.proto.Rpc.RpcMessage;
import com.mrmq.poker.common.proto.Rpc.RpcMessage.Result;
import com.mrmq.poker.db.entity.PkGame;
import com.mrmq.poker.db.entity.PkGame.PkGameType;
import com.mrmq.poker.db.entity.PkUser;
import com.mrmq.poker.game.poker.PokerTable;
import com.mrmq.poker.handler.impl.AbstractHandler;
import com.mrmq.poker.memory.Memory;
import com.mrmq.poker.service.Service;
import com.mrmq.poker.service.Session;
import com.mrmq.poker.service.impl.Heartbeat;
import com.mrmq.poker.service.impl.PokerSession;
import com.mrmq.poker.setting.Configs;
import com.mrmq.poker.utils.Helper;

import io.netty.channel.Channel;

public class PokerMananger {
	private static Logger log = LoggerFactory.getLogger(AbstractHandler.class);
	
	private static ExecutorService syncerService;
	private static ExecutorService handlerService;
	private static ExecutorService executorGameService;
	
	private static AtomicLong sessionIdGennerator = new AtomicLong(1L);
	private static AtomicLong roomIdGennerator = new AtomicLong(1L);
	private static AtomicLong tableIdGennerator = new AtomicLong(1L);
	
	private static Map<String, Service> services = null;
	private static Map<String, Hall> halls = null;
	private static Map<String, PokerTable> tables = null;
	private static ConcurrentMap<String, PkUser> mapUsers = new ConcurrentHashMap<>();
	//Key = <GAMETYPE, <MAXPLAYER + MINAMOUNT, GAME RULE>>
	private static ConcurrentMap<String, ConcurrentHashMap<String, PkGame>> mapGames = new ConcurrentHashMap<String, ConcurrentHashMap<String, PkGame>>();
	
	private static PokerBusiness pokerBusiness;
	private static Properties errMsgs;

	private static Configs configs;
	
	static {
		halls = new ConcurrentHashMap<String, Hall>();
		tables = new ConcurrentHashMap<String, PokerTable>();
		
		//Sync data
		final ThreadFactory syncFactory = new ThreadFactoryBuilder()
				.setNameFormat("Syncer-%d").setDaemon(true).build();
		syncerService = Executors.newFixedThreadPool(10, syncFactory);
		
		//Handle request
		final ThreadFactory handleFactory = new ThreadFactoryBuilder()
				.setNameFormat("Handler-%d").setDaemon(true).build();
		handlerService = Executors.newFixedThreadPool(10, handleFactory);
		
		//Handle game table
		final ThreadFactory threadFactory = new ThreadFactoryBuilder()
				.setNameFormat("Table-%d").setDaemon(true).build();
		executorGameService = Executors.newFixedThreadPool(10, threadFactory);
	}
	
	public static void start(ApplicationContext appContext) throws Exception {
		configs = (Configs) appContext.getBean("configs");
		errMsgs = (Properties) appContext.getBean("errMsgs");
		
		//sync
		PokerMananger.setPokerBusiness((PokerBusiness) appContext.getBean("pokerBusiness"));
		PokerMananger.getPokerBusiness().start(PokerMananger.getSyncerService());
		
		//heart beat
		Heartbeat heartbeat = (Heartbeat) appContext.getBean("heartbeatService");
		if(heartbeat != null)
			PokerMananger.getSyncerService().submit(heartbeat);
		
		loadData(appContext);
	}
	
	private static void loadData(ApplicationContext appContext) {
		//Load Users
		List<PkUser> lstUser = getPokerBusiness().getUserManager().loadAlls();
		log.info("Loaded Users, size: {}", lstUser.size());
		for (PkUser pkUser : lstUser) {
			mapUsers.put(pkUser.getLogin(), pkUser);
		}
		
		//Load GameType
		List<PkGame> lstPkGame = getPokerBusiness().getGameManager().loadAlls();
		log.info("Loaded Games, size: {}", lstPkGame.size());
		
		for (PkGame pkGame : lstPkGame) {
			ConcurrentHashMap<String, PkGame> mapRule = mapGames.get(pkGame.getGameType().trim());
			if(mapRule == null) {
				mapRule = new ConcurrentHashMap<String, PkGame>();
				mapGames.put(pkGame.getGameType().trim(), mapRule);
			}
			mapRule.put(pkGame.getRuleKey(), pkGame);
		}
	}
	
	public static ExecutorService getExecutorService() {
		return handlerService;
	}

	public static void setExecutorService(ExecutorService executorService) {
		PokerMananger.handlerService = executorService;
	}
	
	public static Session getOrSetSession(Channel channel) {
		String ip = Helper.getChannelIp(channel);
		Session session = Memory.getHmSession().get(ip);
		
		if(session == null) {
			session = new PokerSession(genSessionId(), channel);
			Memory.getHmSession().put(ip, session);
		}
		
		return session;
	}
	
	public static Session removeSession(Channel channel) {
		String ip = Helper.getChannelIp(channel);
		return Memory.getHmSession().remove(ip);
	}
	
	public static Session removeSessionByUser(String loginId) {
		return Memory.getHmSessionByUser().remove(loginId);
	}
	
	public static Session putSessionByUser(String loginId, Session session) {
		return Memory.getHmSessionByUser().put(loginId, session);
	}
	
	public static Session getSessionByUser(String loginId) {
		return Memory.getHmSessionByUser().get(loginId);
	}
	
	public static ConcurrentMap<String, Session> getSessions() {
		return Memory.getHmSession();
	}
	
	public static String genSessionId() {
		return String.valueOf(sessionIdGennerator.getAndIncrement());
	}

	public static String genRoomId() {
		return String.valueOf(roomIdGennerator.getAndIncrement());
	}
	
	public static String genTableId() {
		return String.valueOf(tableIdGennerator.getAndIncrement());
	}
	
	public static PkUser getUser(String loginId) {
		PkUser user = mapUsers.get(loginId);
		if(user == null)
			user = getPokerBusiness().getUserManager().loadPkUser(loginId);
		return user;
	}
	
	public static PkUser putUser(PkUser user) {
		return mapUsers.put(user.getLogin(), user);
	}
	
	public static Map<String, PkUser> getUsers() {
		return mapUsers;
	}
	
	public static Service getService(String serviceCode) {
		return services.get(serviceCode);
	}

	public static Map<String, Service> getServices() {
		return services;
	}

	public static void setServices(Map<String, Service> services) {
		PokerMananger.services = services;
	}

	public static ConcurrentHashMap<String, PkGame> getGameRules(PkGameType type) {
		return mapGames.get(type.getValue());
	}
	
	public static Map<String, PokerTable> getTables() {
		return tables;
	}
	
	public static PokerTable getTable(String id) {
		return tables.get(id);
	}
	
	public static PokerTable removeTable(String id) {
		return tables.remove(id);
	}
	
	public static boolean putTable(PokerTable table) {
		if(!tables.containsKey(table.getTableId())) {
			
			tables.put(table.getId(), table);
			executorGameService.execute(table);
			
			return true;
		}
		return false;
	}

	public static Map<String, Hall> getHalls() {
		return halls;
	}

	public static String getMsg(String msgCode) {
		if(errMsgs.containsKey(msgCode))
			return errMsgs.getProperty(msgCode);
		return msgCode;
	}

	public static Configs getConfigs() {
		return configs;
	}
	
	public static RpcMessage.Builder createRpcMessage(long id, String clazz, ByteString data) {
		RpcMessage.Builder rpc = createRpcMessage();
    	
    	rpc.setPayloadClass(clazz);
    	rpc.setPayloadData(data);
    	rpc.setResult(Result.SUCCESS);
    	
    	return rpc;
	}
	
	private static AtomicLong requestIdGennerator = new AtomicLong(1L);
	
	public static RpcMessage.Builder createRpcMessage() {
		RpcMessage.Builder rpc = RpcMessage.newBuilder();
    	rpc.setId(requestIdGennerator.getAndIncrement());
    	rpc.setVersion(configs.getPokerProtoVersion());
    	rpc.setService(ServiceType.POKER.getValue());
    	
    	rpc.setResult(Result.SUCCESS);
    	
    	return rpc;
	}
	
	public static PokerBusiness getPokerBusiness() {
		return pokerBusiness;
	}

	public static void setPokerBusiness(PokerBusiness pokerBusiness) {
		PokerMananger.pokerBusiness = pokerBusiness;
	}

	public static ExecutorService getSyncerService() {
		return syncerService;
	}

	public static void onEvent(Object event, ServiceType serviceType) {
		Service service = PokerMananger.getService(serviceType.getValue());
		if(service != null)
			service.onEvent(event);
	}
}