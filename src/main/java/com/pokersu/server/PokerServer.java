package com.pokersu.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

public final class PokerServer implements Server {
	private boolean ssl = false;
	private int port = 8888;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	
	@Override
	public void run() {
		start();
	}
	
	public void start() {
		try {
			// Configure SSL.
			final SslContext sslCtx;
			if (isSsl()) {
				SelfSignedCertificate ssc;

				ssc = new SelfSignedCertificate();

				sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
			} else {
				sslCtx = null;
			}

			bossGroup = new NioEventLoopGroup(1);
			workerGroup = new NioEventLoopGroup();
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.INFO)).childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							ChannelPipeline p = ch.pipeline();
							if (sslCtx != null) {
								p.addLast(sslCtx.newHandler(ch.alloc()));
							}
							p.addLast("decoder", new HttpRequestDecoder());
							p.addLast("encoder", new HttpResponseEncoder()); 
							p.addLast("handler", new PokerServerHandler());
						}
					});

			// Bind and start to accept incoming connections.
			b.bind(getPort()).sync().channel().closeFuture().sync();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	public boolean stop(long timeOut, TimeUnit unit) {
		return false;
	}
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isSsl() {
		return ssl;
	}

	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	
}