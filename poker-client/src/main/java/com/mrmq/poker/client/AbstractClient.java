package com.mrmq.poker.client;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

/**
 * This is an example of a WebSocket client.
 * <p>
 * In order to run this example you need a compatible WebSocket server.
 * Therefore you can either start the WebSocket server from the examples
 * by running {@link io.netty.example.http.websocketx.server.WebSocketServer}
 * or connect to an existing WebSocket server such as
 * <a href="http://www.websocket.org/echo.html">ws://echo.websocket.org</a>.
 * <p>
 * The client will attempt to connect to the URI passed to it as the first argument.
 * You don't have to specify any arguments if you want to connect to the example WebSocket server,
 * as this is the default.
 */
public abstract class AbstractClient implements Runnable {
	protected Logger log = LoggerFactory.getLogger(this.getClass());
	
	protected String url;
	protected String loginId;
	protected String pass;
	private EventLoopGroup eventLoopGroup;
	
	public AbstractClient() {
	}
	
	public AbstractClient(String url, String loginId, String pass) {
		this.url = url;
		this.loginId = loginId;
		this.pass = pass;
	}
	
	protected abstract ClientHandler getHandler(URI uri);
	protected abstract void onFinished();
	
	@Override
	public void run() {
		try {
			start(url, loginId, pass);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			onFinished();
		}
	}
	
    public void start(final String url, final String loginId, final String pass) throws Exception {
        URI uri = new URI(url);
        String scheme = uri.getScheme() == null? "ws" : uri.getScheme();
        final String host = uri.getHost() == null? "127.0.0.1" : uri.getHost();
        final int port;
        if (uri.getPort() == -1) {
            if ("ws".equalsIgnoreCase(scheme)) {
                port = 80;
            } else if ("wss".equalsIgnoreCase(scheme)) {
                port = 443;
            } else {
                port = -1;
            }
        } else {
            port = uri.getPort();
        }

        if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
        	log.error("Only WS(S) is supported.");
            return;
        }

        final boolean ssl = "wss".equalsIgnoreCase(scheme);
        final SslContext sslCtx;
        if (ssl) {
            sslCtx = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            sslCtx = null;
        }

        eventLoopGroup = new NioEventLoopGroup();
        try {
            // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
            // If you change it to V00, ping is not supported and remember to change
            // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
            
        	final ClientHandler handler = getHandler(uri);
        	
            Bootstrap b = new Bootstrap();
            b.group(eventLoopGroup)
             .channel(NioSocketChannel.class)
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel(SocketChannel ch) {
                     ChannelPipeline p = ch.pipeline();
                     if (sslCtx != null) {
                         p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                     }
                     p.addLast(
                             new HttpClientCodec(),
                             new HttpObjectAggregator(8192),
                             handler);
                 }
             });

            b.connect(uri.getHost(), port).sync();
            handler.handshakeFuture().sync().channel().closeFuture().sync();
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }
    
    public void stop() {
    	log.info("AbstractClient stopping...");
    	try {
    		eventLoopGroup.shutdownGracefully();
    	} catch (Exception e) {
    		log.error(e.getMessage(), e);
    	}
    	log.info("AbstractClient stopped");
    }
    
    public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getLoginId() {
		return loginId;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}
}