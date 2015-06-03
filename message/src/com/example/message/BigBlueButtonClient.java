package com.example.message;

import org.jboss.netty.channel.Channel;

import com.flazr.rtmp.client.ClientOptions;

public class BigBlueButtonClient {
	
	private UsersModule usersModule = null;
	private MainRtmpConnection mainConnection=null;
	private String myUserId;
	public boolean connectBigBlueButton(ClientOptions opt) {
		
		mainConnection = new MainRtmpConnection(opt, this);
		return mainConnection.connect();
	}
	public void setMyUserId(String myUserId) {
		this.myUserId = myUserId;
	}
	public void createUsersModule(MainRtmpConnection handler,
			Channel channel) {
		usersModule = new UsersModule(handler, channel);
	}

}
