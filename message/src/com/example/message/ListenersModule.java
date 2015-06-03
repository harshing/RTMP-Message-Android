package com.example.message;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.netty.channel.Channel;
import org.red5.server.api.IAttributeStore;
import org.red5.server.api.so.IClientSharedObject;
import org.red5.server.api.so.ISharedObjectBase;
import org.red5.server.api.so.ISharedObjectListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flazr.rtmp.message.Command;
import com.flazr.rtmp.message.CommandAmf0;

public class ListenersModule extends Module implements ISharedObjectListener {
	
	private static final Logger log = LoggerFactory.getLogger(ListenersModule.class);
	private final IClientSharedObject voiceSO;
	
	private Map<Integer, Listener> listeners = new HashMap<Integer, Listener>();
	//private boolean roomMuted;

	public ListenersModule(MainRtmpConnection handler, Channel channel) {
		super(handler, channel);
		
		voiceSO = handler.getSharedObject("ApplTSO", false);
		voiceSO.addSharedObjectListener(this);
		voiceSO.connect(channel);
	}
	
	@Override
	public void onSharedObjectClear(ISharedObjectBase so) {
		log.debug("onSharedObjectClear");
		doGetCurrentUsers();
	}

	public void doGetCurrentUsers() {
    	Command cmd = new CommandAmf0("voice.getMeetMeUsers", null);
    	handler.writeCommandExpectingResult(channel, cmd);
	}

	/*
		<< [STRING _result]
		<< [NUMBER 5.0]
		<< [NULL null]
		<< [NUMBER 1.0]
		<< [BOOLEAN false]
		<< [BOOLEAN false]
		<< [STRING Felipe]
		<< [NUMBER 5.0]
		<< [BOOLEAN false]
		<< [MAP {talking=false, muted=false, name=Felipe, participant=5.0, locked=false}]	
		<< [MAP {5={talking=false, muted=false, name=Felipe, participant=5.0, locked=false}}]
		<< [MAP {count=1.0, participants={5={talking=false, muted=false, name=Felipe, participant=5.0, locked=false}}}]
		<< [1 COMMAND_AMF0 c3 #0 t0 (0) s144] name: _result, transactionId: 5, object: null, args: [{count=1.0, participants={5={talking=false, muted=false, name=Felipe, participant=5.0, locked=false}}}]
		server command: _result
		result for method call: voice.getMeetMeUsers
		
		<< [STRING _result]
		<< [NUMBER 5.0]
		<< [NULL null]
		<< [NUMBER 0.0]
		<< [MAP {count=0.0}]
		<< [1 COMMAND_AMF0 c3 #0 t0 (0) s44] name: _result, transactionId: 5, object: null, args: [{count=0.0}]
		server command: _result
		result for method call: voice.getMeetMeUsers
	 */
	@SuppressWarnings("unchecked")
	public boolean onGetCurrentUsers(String resultFor, Command command) {
		if (resultFor.equals("voice.getMeetMeUsers")) {
			
			
			listeners.clear();
			// the userId is different from the UsersModule
			Map<String, Object> currentUsers = (Map<String, Object>) command.getArg(0);
			int count = ((Double) currentUsers.get("count")).intValue();
			if (count > 0) {
				Map<String, Object> participants = (Map<String, Object>) currentUsers.get("participants");
				
				for (Map.Entry<String, Object> entry : participants.entrySet()) {
					@SuppressWarnings("unused")
					int userId = Integer.parseInt(entry.getKey());
					
					Listener listener = new Listener((Map<String, Object>) entry.getValue());
					onListenerJoined(listener);
				}
			}
			return true;
		}
		return false;
	}

	
	@Override
	public void onSharedObjectConnect(ISharedObjectBase so) {
		log.debug("onSharedObjectConnect");
	}

	@Override
	public void onSharedObjectDelete(ISharedObjectBase so, String key) {
		log.debug("onSharedObjectDelete");
	}

	@Override
	public void onSharedObjectDisconnect(ISharedObjectBase so) {
		log.debug("onSharedObjectDisconnect");
	}


	@Override
	public void onSharedObjectSend(ISharedObjectBase so, String method,
			List<?> params) {
		if (method.equals("userJoin")) {
				// meetMeUsersSO { SOEvent(SERVER_SEND_MESSAGE, userJoin, [5.0, Felipe, Felipe, false, false, false]) }
			Listener listener = new Listener(params);
			if (!listeners.containsKey(listener.getUserId())) {
				onListenerJoined(listener);
			} else {
				log.warn("The listener {} is already in the list", listener.getUserId());
			}
		} else if (method.equals("userLeft")) {
			// meetMeUsersSO { SOEvent(SERVER_SEND_MESSAGE, userLeft, [2.0]) }
			int userId = ((Double) params.get(0)).intValue();
			IListener listener = listeners.get(userId);
			
			if (listener != null) {
				//for (OnListenerLeftListener l : handler.getContext().getListenerLeftListeners())
					//l.onListenerLeft(listener);
				listeners.remove(userId);
			} else {
				log.warn("Can't find the listener {} on userLeft", userId);
			}
		}
		log.debug("onSharedObjectSend");
	}

	@Override
	public void onSharedObjectUpdate(ISharedObjectBase so, String key,
			Object value) {
		log.debug("onSharedObjectUpdate1");
	}

	@Override
	public void onSharedObjectUpdate(ISharedObjectBase so,
			IAttributeStore values) {
		log.debug("onSharedObjectUpdate2");
	}

	@Override
	public void onSharedObjectUpdate(ISharedObjectBase so,
			Map<String, Object> values) {
		log.debug("onSharedObjectUpdate3");
	}

	@Override
	public boolean onCommand(String resultFor, Command command) {
		if (onGetCurrentUsers(resultFor, command)) {
			return true;
		} else
			return false;
	}

	public Map<Integer, Listener> getListeners() {
		return listeners;
	}
	
	public void onListenerJoined(Listener p) {
		log.debug("New listener: " + p.toString());
		listeners.put(p.getUserId(), p);			
		//for (OnListenerJoinedListener l : handler.getContext().getListenerJoinedListeners())
			//l.onListenerJoined(p);
	}
	
}