package com.example.message;

import org.jboss.netty.channel.Channel;

import com.flazr.rtmp.message.Command;

public abstract class Module {
	protected final MainRtmpConnection handler;
	protected final Channel channel;
	
	public Module(final MainRtmpConnection handler, final Channel channel) {
		this.handler = handler;
		this.channel = channel;
	}
	
	abstract public boolean onCommand(String resultFor, Command command);
}