package com.mrmq.poker.client.setting;

public class Configs {
	private String pokerUrl;
	private String adminUrl;
	private String pokerProtoVersion;
	private String adminProtoVersion;
	
	private long serverHeartbeatInterval = 10000;
	private int threadPoolSize = 100;
	
	private int adminLoadUserTime = 3;
	private int adminLoadUserTimeout = 15000; // Millisecond
	
	public String getPokerUrl() {
		return pokerUrl;
	}
	public void setPokerUrl(String pokerUrl) {
		this.pokerUrl = pokerUrl;
	}
	public String getAdminUrl() {
		return adminUrl;
	}
	public void setAdminUrl(String adminUrl) {
		this.adminUrl = adminUrl;
	}
	public long getServerHeartbeatInterval() {
		return serverHeartbeatInterval;
	}
	public void setServerHeartbeatInterval(long serverHeartbeatInterval) {
		this.serverHeartbeatInterval = serverHeartbeatInterval;
	}
	public int getThreadPoolSize() {
		return threadPoolSize;
	}
	public void setThreadPoolSize(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
	}
	public int getAdminLoadUserTime() {
		return adminLoadUserTime;
	}
	public void setAdminLoadUserTime(int adminLoadUserTime) {
		this.adminLoadUserTime = adminLoadUserTime;
	}
	public int getAdminLoadUserTimeout() {
		return adminLoadUserTimeout;
	}
	public void setAdminLoadUserTimeout(int adminLoadUserTimeout) {
		this.adminLoadUserTimeout = adminLoadUserTimeout;
	}
	
	public String getPokerProtoVersion() {
		return pokerProtoVersion;
	}
	public void setPokerProtoVersion(String pokerProtoVersion) {
		this.pokerProtoVersion = pokerProtoVersion;
	}
	public String getAdminProtoVersion() {
		return adminProtoVersion;
	}
	public void setAdminProtoVersion(String adminProtoVersion) {
		this.adminProtoVersion = adminProtoVersion;
	}
	@Override
	public String toString() {
		return "Configs [pokerUrl=" + pokerUrl + ", adminUrl=" + adminUrl + ", pokerProtoVersion=" + pokerProtoVersion
				+ ", adminProtoVersion=" + adminProtoVersion + ", serverHeartbeatInterval=" + serverHeartbeatInterval
				+ ", threadPoolSize=" + threadPoolSize + ", adminLoadUserTime=" + adminLoadUserTime
				+ ", adminLoadUserTimeout=" + adminLoadUserTimeout + "]";
	}
	
}
