package io.vertx.example.core.http.websockets;

import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.impl.ServerWebSocketImpl;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.net.impl.ConnectionBase;

public class ServerWebSocketImpl1 extends ServerWebSocketImpl {
	
	public ServerWebSocketImpl1(VertxInternal vertx, String uri, String path,
			String query, MultiMap headers, ConnectionBase conn,
			boolean supportsContinuation, Runnable connectRunnable,
			int maxWebSocketFrameSize) {
		super(vertx, uri, path, query, headers, conn, supportsContinuation,
				connectRunnable, maxWebSocketFrameSize);
	}

	@Override
	public ServerWebSocket handler(Handler<Buffer> handler) {
		return super.handler(handler);
	}
}
