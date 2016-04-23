package com.pokersu.server;


public interface Service extends Runnable {
	
	public void onEvent(Object event);
	public void stop();
}