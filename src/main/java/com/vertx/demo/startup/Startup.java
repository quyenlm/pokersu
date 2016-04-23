package com.vertx.demo.startup;

import io.vertx.example.util.Runner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.pokersu.server.PokerServer;
import com.pokersu.server.Server;

public class Startup {
	private static ExecutorService executorService = Executors.newFixedThreadPool(3);
	
	public static void main( String[] args ) throws Exception {
		int port = 8080;
		try {
			port = Integer.valueOf(System.getenv("PORT"));
			System.out.println("getenv PORT: " + port);
		} catch (Exception e) {
			System.out.println("cannot get getenv PORT, use default port: " + port);
		}
		
//		Runner.runExample(Server.class);
    	
    	try {
    		final Server pokerServer = new PokerServer();
    		pokerServer.setPort(port);
    		executorService.submit(pokerServer);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
