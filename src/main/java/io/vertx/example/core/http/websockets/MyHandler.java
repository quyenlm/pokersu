package io.vertx.example.core.http.websockets;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;

public class MyHandler implements Handler<Buffer> {
	@Override
	public void handle(Buffer event) {
		System.out.println("event: " + event);
		
	}
}

