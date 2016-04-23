package com.mrmq.poker.client.startup;

import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.mrmq.poker.client.impl.PokerClient;
import com.mrmq.poker.client.manager.PokerBotManager;
import com.mrmq.poker.client.manager.PokerClientManager;

public class PokerClientStartup {
	private static Logger log = LoggerFactory.getLogger(PokerClientStartup.class);
	
	public static void main( String[] args )  {
		start();
    }
	
	public static void start() {
		try {
			log.info("PokerClientManager starting...");
			
	    	final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:poker-client-context.xml");
	    	PokerClientManager.init(context);
	    	
	    	Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				public void run() {
					context.close();
				}
			}));
	    	
	    	PokerBotManager pokerBotManager = (PokerBotManager) context.getBean("pokerBotManager");
	    	Future<?> boManagerSubmit = PokerClientManager.getHandlerService().submit(pokerBotManager);
	    	boManagerSubmit.get();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			PokerClientManager.getHandlerService().shutdown();
			log.info("PokerClientManager stopped");
		}
	}
	
	private static void demo() {
		PokerClient pokerClient = new PokerClient(PokerClientManager.getConfigs().getPokerUrl(), "user1", "123456", 1);
		PokerClientManager.getHandlerService().submit(pokerClient);
	}
}