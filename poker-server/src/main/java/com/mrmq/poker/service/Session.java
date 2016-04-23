package com.mrmq.poker.service;

import com.google.protobuf.ByteString;
import com.mrmq.poker.common.bean.Client;
import com.mrmq.poker.common.proto.Rpc.RpcMessage;
import com.mrmq.poker.db.entity.PkUser;

public interface Session extends Client {
	public String id();
	
	public boolean isAuthenticated();
	public boolean isAlive();
	public void setAuthenticated(boolean authenticated);
	
	public PkUser getUser();
	public void setUser(PkUser player);
	
	public boolean send(RpcMessage msg);
	
	public boolean release();
}