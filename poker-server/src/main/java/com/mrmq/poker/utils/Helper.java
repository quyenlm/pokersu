package com.mrmq.poker.utils;

import io.netty.channel.Channel;

public class Helper {
	public static String getChannelIp(Channel channel) {
		String ip = channel.remoteAddress().toString();
		return ip;
	}
}