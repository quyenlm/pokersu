package com.nextstep.poker.server.websockets;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;

public class MyHandler implements Handler<Buffer> {
	@Override
	public void handle(Buffer event) {
		System.out.println("event: " + event);
		
	}
}

