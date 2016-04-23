package com.mrmq.poker.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mrmq.poker.client.manager.PokerClientManager;
import com.mrmq.poker.common.proto.Rpc.RpcMessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;

public abstract class AbstractClientHandler extends SimpleChannelInboundHandler<Object> implements ClientHandler {
	private static Logger log = LoggerFactory.getLogger(AbstractClientHandler.class);
	
	protected final WebSocketClientHandshaker handshaker;
	protected ChannelPromise handshakeFuture;

	protected String loginId;
	protected String pass;
	
    public AbstractClientHandler(WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
    }

    protected abstract void handleRpcMessage(ChannelHandlerContext ctx, RpcMessage msg) throws Exception;
	
	protected void login(ChannelHandlerContext ctx) {
		RpcMessage request = PokerClientManager.createLoginRequest(loginId, pass);
		request(ctx, request);
	}
	
	protected void request(ChannelHandlerContext ctx, RpcMessage rpc) {
		BinaryWebSocketFrame frame = new BinaryWebSocketFrame();
    	frame.content().writeBytes(rpc.toByteArray());
    	ctx.writeAndFlush(frame);
	}
	
	public void messageReceived(ChannelHandlerContext ctx, Object msg) {
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(ch, (FullHttpResponse) msg);
            log.info("Connected to server");
            handshakeFuture.setSuccess();
            
            login(ctx);
            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException(
                    "Unexpected FullHttpResponse (getStatus=" + response.getStatus() +
                            ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }

        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            log.info("WebSocket Client received message: " + textFrame.text());
        } else if (frame instanceof PongWebSocketFrame) {
        	log.info("WebSocket Client received pong");
        } else if (frame instanceof CloseWebSocketFrame) {
        	log.info("WebSocket Client received closing");
            ch.close();
        } else if (frame instanceof BinaryWebSocketFrame) {
			RpcMessage rpc;
			try {
				ByteBuf buffer = frame.content();
				
				byte[] bytes = new byte[buffer.readableBytes()];
				buffer.readBytes(bytes);
				
				rpc = RpcMessage.parseFrom(bytes);
				handleRpcMessage(ctx, rpc);
			} catch (InvalidProtocolBufferException e) {
				log.error(e.getMessage(), e);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
        }
    }
	
    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
    	log.info("WebSocket Client disconnected!");
    }

    @Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
    	messageReceived(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
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
