package com.mrmq.poker.client;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInboundHandler;

public interface ClientHandler extends ChannelInboundHandler {
	public ChannelFuture handshakeFuture();
}
