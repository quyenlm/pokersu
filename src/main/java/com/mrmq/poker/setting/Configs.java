package com.mrmq.poker.setting;

public class Configs {
	private String pokerProtoVersion;
	private String adminProtoVersion;
	
	private long serverHeartbeatInterval = 10000;
	
	public long getServerHeartbeatInterval() {
		return serverHeartbeatInterval;
	}
	public void setServerHeartbeatInterval(long serverHeartbeatInterval) {
		this.serverHeartbeatInterval = serverHeartbeatInterval;
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
		return "Configs [pokerProtoVersion=" + pokerProtoVersion + ", adminProtoVersion=" + adminProtoVersion
				+ ", serverHeartbeatInterval=" + serverHeartbeatInterval + "]";
	}
	
}
