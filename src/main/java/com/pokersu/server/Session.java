package com.pokersu.server;


public interface Session  {
	public String id();
	
	public boolean isAuthenticated();
	public boolean isAlive();
	public void setAuthenticated(boolean authenticated);
	
	
	
	public boolean release();
}