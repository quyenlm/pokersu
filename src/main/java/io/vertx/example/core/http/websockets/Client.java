package io.vertx.example.core.http.websockets;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.example.util.Runner;

/*
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class Client extends AbstractVerticle {
//	String host = "localhost";
	String host = "gettingstart.herokuapp.com";
	String uri = "";
	int port = 51295;
	
	// Convenience method so you can run it in your IDE
	public static void main(String[] args) {
		Runner.runExample(Client.class);
	}

	@Override
	public void start() throws Exception {
		if (vertx == null) {
			System.out.println("init vertx context...");
			vertx = Vertx.vertx();
		}
		
		HttpClient client = vertx.createHttpClient();

		client.websocket(port, host, uri,
				websocket -> {
					websocket.handler(data -> {
						System.out.println("Received data " + data.toString("ISO-8859-1"));
						client.close();
					});
					websocket.writeBinaryMessage(Buffer.buffer("Hello world"));
				});
	}
}
