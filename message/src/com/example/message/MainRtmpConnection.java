package com.example.message;

import java.util.ArrayList;
import java.util.Map;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.red5.server.so.SharedObjectMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.util.Log;

import com.flazr.rtmp.RtmpDecoder;
import com.flazr.rtmp.RtmpEncoder;
import com.flazr.rtmp.RtmpMessage;
import com.flazr.rtmp.client.ClientHandshakeHandler;
import com.flazr.rtmp.client.ClientOptions;
import com.flazr.rtmp.message.Command;
import com.flazr.rtmp.message.CommandAmf0;
import com.flazr.rtmp.message.Control;

public class MainRtmpConnection extends RtmpConnection {
	private static final Logger log = LoggerFactory.getLogger(MainRtmpConnection.class);
    private boolean connected = false;
    private Object[] args={"Test123","18.9750/72.8258","A B","","Female","info",false};
	public MainRtmpConnection(ClientOptions options, BigBlueButtonClient context) {
		super(options, context);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected ChannelPipelineFactory pipelineFactory() {
		// TODO Auto-generated method stub
		return new ChannelPipelineFactory() {
            @Override
            public ChannelPipeline getPipeline() throws Exception {
            final ChannelPipeline pipeline = Channels.pipeline();
            pipeline.addLast("handshaker", new ClientHandshakeHandler(options));
            pipeline.addLast("decoder", new RtmpDecoder());
            pipeline.addLast("encoder", new RtmpEncoder());
            pipeline.addLast("handler", MainRtmpConnection.this);
            return pipeline;
            }
		};
	}
	
	@Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
            
			// * https://github.com/bigbluebutton/bigbluebutton/blob/master/bigbluebutton-client/src/org/bigbluebutton/main/model/users/NetConnectionDelegate.as#L102
            // * _netConnection.connect(?);
			options.setArgs(args);                 
            Log.e("channel connection","success");
            writeCommandExpectingResult(e.getChannel(), Command.connect(options));
    }
	
	@Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            super.channelDisconnected(ctx, e);
            log.debug("Rtmp Channel Disconnected");
            
            connected = false;
    }
	public void doGetMyUserId(Channel channel) {
    	Command command = new CommandAmf0("getConnectedClients", null);
    	writeCommandExpectingResult(channel, command);
    }
	
	
	public boolean onGetMyUserId(String resultFor, Command command) {
    	if (resultFor.equals("getConnectedClients")) {
    		Object [] array=(Object [])command.getArg(0);
	    	for(Object var:array){
	    		Log.e("result",""+var.toString());
	    	}
    		
    		//context.setMyUserId((String) command.getArg(0));
	    	//connected = true;
	    	return true;
    	}
    	else
    		return false;
	}
	@SuppressWarnings("unchecked")
	public String connectGetCode(Command command) {
		Log.e("code",""+((Map<String, Object>) command.getArg(0)).get("code").toString());
    	return ((Map<String, Object>) command.getArg(0)).get("code").toString();
    }
	@Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent me) {
    final Channel channel = me.getChannel();
    final RtmpMessage message = (RtmpMessage) me.getMessage();
    
    Log.e("message",""+message.getHeader().getMessageType().toString());
    switch(message.getHeader().getMessageType()) {
            
    		case CONTROL:
            Control control = (Control) message;
            switch(control.getType()) {
                case PING_REQUEST:
                    final int time = control.getTime();
                    Control pong = Control.pingResponse(time);
                    channel.write(pong);
                    break;
            }
                    break;
            
            case COMMAND_AMF0:
            case COMMAND_AMF3:
            	Command command = (Command) message;                
	            String name = command.getName();
	            log.debug("server command: {}", name);
	            //Log.e("name",""+name);
	            if(name.equals("_result")) {
	                String resultFor = transactionToCommandMap.get(command.getTransactionId());
	                if (resultFor == null) {
	                	log.warn("result for method without tracked transaction");
	                	break;
	                }
	                log.info("result for method call: {}", resultFor);
	                Log.e("resultFor",""+resultFor);
	                if(resultFor.equals("connect")) {
	                	String code = connectGetCode(command);
	                	if (code.equals("NetConnection.Connect.Success"))
	                		{
	                			doGetMyUserId(channel);
	                			context.createUsersModule(this, channel);
	                		}
	                	else {
	                		log.error("method connect result in {}, quitting", code);
	                		log.debug("connect response: {}", command.toString());
	                		channel.close();
	                	}
	                	return;
	                } else if (onGetMyUserId(resultFor, command)) {
	                	
	                	
	                	break;
	                } 
	                //context.onCommand(resultFor, command);
                	break;
	            }
	            break;
                
            case SHARED_OBJECT_AMF0:
            case SHARED_OBJECT_AMF3:
                    onSharedObject(channel, (SharedObjectMessage) message);
            		Log.e("object", "shared");
                    break;
            default:
                	log.info("ignoring rtmp message: {}", message);
                    break;
    	}
    }
	
	public boolean isConnected() {
        return connected;
	}
}
