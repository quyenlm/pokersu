package com.mrmq.poker.startup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.mrmq.poker.admin.impl.AdminServer;
import com.mrmq.poker.client.startup.PokerClientStartup;
import com.mrmq.poker.common.glossary.ServiceType;
import com.mrmq.poker.manager.PokerMananger;
import com.mrmq.poker.service.Server;
import com.mrmq.poker.service.Service;
import com.mrmq.poker.servlet.PokerServer;

public class PokerStartup {
	private static ExecutorService executorService = Executors.newFixedThreadPool(3);
	
	public static void main( String[] args ) throws Exception {
		int port = 8080;
		try {
			port = Integer.valueOf(System.getenv("PORT"));
			System.out.println("getenv PORT: " + port);
		} catch (Exception e) {
			System.out.println("cannot get getenv PORT, use default port: " + port);
		}
		System.out.println("starting server with port: " + port);
    	final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:poker-context.xml");
    	
//    	demo();
    	
    	PokerMananger.start(context);
    	
    	final Server pokerServer = (PokerServer) context.getBean("pokerServer");
    	pokerServer.setPort(port);
    	
    	final Server adminServer = (AdminServer) context.getBean("adminServer");
    	final Service adminService = PokerMananger.getService(ServiceType.ADMIN.getValue());
    	
    	Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				context.close();
			}
		}));
    	
    	try {
    		executorService.submit(adminService);
    		executorService.submit(pokerServer);
    		executorService.submit(adminServer);
    		PokerClientStartup.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	
	public static void demo() {
		List<String> list = new ArrayList<>(5);
		list.add(0, "0");
		list.add(1, "1");
		list.add(2, "3");
		list.add(2, "2");
		Iterator<String> it = list.iterator();
		while(it.hasNext())
			System.out.println(it.next());
	}
}