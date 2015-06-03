package com.example.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.netty.channel.Channel;
import org.red5.server.api.IAttributeStore;
import org.red5.server.api.so.IClientSharedObject;
import org.red5.server.api.so.ISharedObjectBase;
import org.red5.server.api.so.ISharedObjectListener;

import android.util.Log;

import com.flazr.rtmp.message.Command;
import com.flazr.rtmp.message.CommandAmf0;

public class UsersModule extends Module implements ISharedObjectListener{
	private final IClientSharedObject participantsSO;

	//private Map<String, Participant> participants = new ConcurrentHashMap<String, Participant>();
	private int participantCount = 0;
	private String joinServiceVersion;
	private Channel ch;
	private MainRtmpConnection mrc;
	public UsersModule(MainRtmpConnection handler, Channel channel) {
		super(handler, channel);
		this.ch=channel;
		this.mrc=handler;
		//joinServiceVersion = handler.getContext().getJoinService().getApplicationService().getVersion();
		
		participantsSO = handler.getSharedObject("ApplTSO", false);
		participantsSO.addSharedObjectListener(this);
		participantsSO.connect(channel);
		participantsSO.isConnected();
	}

	@Override
	public void onSharedObjectConnect(ISharedObjectBase so) {
		// TODO Auto-generated method stub
		//participantsSO=handler.getSharedObject("getConnectedClients", false);
		Object args=new String("Test123");
		/*Command command = new CommandAmf0("updateUser",null);
    	mrc.writeCommandExpectingResult(channel, command);
		Log.e("","onSharedObjectConnect");*/
		Map<String,Object> map=new HashMap<String, Object>();
		map.put("userName", "A B");
		map.put("message","A B Test123@hello");
		Command command1 = new CommandAmf0("sendMessage",null, map);
    	mrc.writeCommandExpectingResult(channel, command1);
		
	}

	@Override
	public void onSharedObjectDisconnect(ISharedObjectBase so) {
		// TODO Auto-generated method stub
		participantsSO.removeSharedObjectListener(this);
		Log.e("","onSharedObjectDisconnect");
	}

	@Override
	public void onSharedObjectUpdate(ISharedObjectBase so, String key,
			Object value) {
		// TODO Auto-generated method stub
		Log.e("","onSharedObjectUpdate1"+" "+key+" "+value);
		
	}

	@Override
	public void onSharedObjectUpdate(ISharedObjectBase so,
			IAttributeStore values) {
		// TODO Auto-generated method stub
		Log.e("","onSharedObjectUpdate2");
	}

	@Override
	public void onSharedObjectUpdate(ISharedObjectBase so,
			Map<String, Object> values) {
		// TODO Auto-generated method stub
		Log.e("","onSharedObjectUpdate3");
	}

	@Override
	public void onSharedObjectDelete(ISharedObjectBase so, String key) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSharedObjectClear(ISharedObjectBase so) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSharedObjectSend(ISharedObjectBase so, String method,
			List<?> params) {
		// TODO Auto-generated method stub
		List<String> arg=(List<String>)params;
		Log.e("","onSharedObjectSend"+" "+method+" "+arg.get(0));
		if(method=="getchatmsg"){
			Log.e("","New Messge Received");
		}
		else if(method=="userLeft"){
			Log.e("","User Left");
		}
		else if(method=="setUpdateuser"){
			Log.e("","User Joined");
		}
		
	}

	@Override
	public boolean onCommand(String resultFor, Command command) {
		// TODO Auto-generated method stub
		Log.e("","onSharedObjectCommand");
		return false;
	}
}
