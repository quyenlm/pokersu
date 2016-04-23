package com.mrmq.poker.client.impl;

import java.net.URI;

import com.mrmq.poker.client.AbstractClient;
import com.mrmq.poker.client.ClientHandler;
import com.mrmq.poker.client.manager.PokerBotManager;
import com.mrmq.poker.common.proto.PokerModelProto.Table;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;


public final class PokerClient extends AbstractClient {
	private Integer position;
	private Table table = null;
	
	public PokerClient(String url, String loginId, String pass, Integer position) {
		super(url, loginId, pass);
		this.position = position;
	}

	@Override
	protected ClientHandler getHandler(URI uri) {
		final PokerClientHandler handler =
                new PokerClientHandler(
                        WebSocketClientHandshakerFactory.newHandshaker(
                        		uri, WebSocketVersion.V13, null, false, new DefaultHttpHeaders()));
        handler.setLoginId(loginId);
        handler.setPass(pass);
        handler.setPosition(position);
        handler.setTable(table);
        
        return handler;
	}

	@Override
	protected void onFinished() {
		PokerBotManager.onUserFree(loginId);
	}
	
	public Table getTable() {
		return table;
	}

	public void setTable(Table table) {
		this.table = table;
	}
}