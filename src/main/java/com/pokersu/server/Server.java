package com.pokersu.server;

import java.util.concurrent.TimeUnit;

public interface Server extends Runnable {
	public int getPort();

	public void setPort(int port);

	public boolean isSsl();

	public void setSsl(boolean ssl);
	
	public void start();
	
	public boolean stop(long timeOut, TimeUnit unit);
}
