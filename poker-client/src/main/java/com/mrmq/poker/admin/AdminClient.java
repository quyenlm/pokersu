package com.mrmq.poker.admin;

import java.net.URI;

import com.mrmq.poker.client.AbstractClient;
import com.mrmq.poker.client.ClientHandler;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

public final class AdminClient extends AbstractClient {
	
	public AdminClient() {
		super();
	}
	
	public AdminClient(String url, String loginId, String pass) {
		super(url, loginId, pass);
	}

	@Override
	protected ClientHandler getHandler(URI uri) {
		final AdminClientHandler handler =
                new AdminClientHandler(
                        WebSocketClientHandshakerFactory.newHandshaker(
                        		uri, WebSocketVersion.V13, null, false, new DefaultHttpHeaders()));
        handler.setLoginId(loginId);
        handler.setPass(pass);
        
        return handler;
	}
	
	@Override
	protected void onFinished() {
		synchronized (this) {
			notify();
		}
	}
}