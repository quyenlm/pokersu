package com.mrmq.poker.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;
import com.mrmq.poker.common.proto.Heartbeat.HeartbeatMessage;
import com.mrmq.poker.common.proto.PokerServiceProto.EndGameEvent;
import com.mrmq.poker.common.proto.Rpc.RpcMessage;
import com.mrmq.poker.db.entity.PkUser;
import com.mrmq.poker.manager.PokerMananger;
import com.mrmq.poker.service.Session;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

public class PokerSession implements Session {
	private static Logger log = LoggerFactory.getLogger(PokerSession.class);
	
	private String id;
	private boolean authenticated;
	private Channel channel;
	private PkUser user;
	
	public PokerSession(String id, Channel channel) {
		this.id = id;
		this.channel = channel;
	}
	
	public String id() {
		return id;
	}

	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}
	
	public boolean isAuthenticated() {
		return authenticated;
	}

	@Override
	public boolean isAlive() {
		return channel != null && channel.isActive();
	}
	
	@Override
	public PkUser getUser() {
		return user;
	}

	@Override
	public void setUser(PkUser player) {
		this.user = player;
	}
	
	@Override
	public boolean send(RpcMessage msg) {		
		if(channel != null && channel.isActive()) {
			if(!HeartbeatMessage.getDescriptor().getName().equals(msg.getPayloadClass()))
				log.info("Response to player: {}, msg:\n{}", (user != null ? user.getLogin() : toString()), msg);
			
			BinaryWebSocketFrame frame = new BinaryWebSocketFrame();
	    	frame.content().writeBytes(msg.toByteArray());
	    	
			ChannelFuture future = channel.writeAndFlush(frame);
			return future.isDone();
		} else {
			log.warn("Channel not connected. Can not response to player: {}, msg:\n{}", user, msg);
			return false;
		}
	}

	@Override
	public boolean send(String payloadClass, ByteString payloadData) {
		//Create rpc
		RpcMessage.Builder rpc = PokerMananger.createRpcMessage();
    	rpc.setPayloadClass(payloadClass);
    	rpc.setPayloadData(payloadData);
    	return send(rpc.build());
	}
	
	@Override
	public boolean release() {
		authenticated = false;
		if(user != null)
			PokerMananger.removeSessionByUser(user.getLogin());
		return false;
	}

	@Override
	public String toString() {
		return "PokerSession [id=" + id + ", authenticated=" + authenticated + "]";
	}
	
}