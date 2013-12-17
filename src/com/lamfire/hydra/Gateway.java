package com.lamfire.hydra;

import java.nio.ByteBuffer;

import com.lamfire.logger.Logger;
import com.lamfire.hydra.net.Context;
import com.lamfire.hydra.net.Session;

/**
 * 消息路由
 * @author admin
 *
 */
public class Gateway extends Hydra {
	static final Logger LOGGER = Logger.getLogger(Gateway.class);
	private MessageBus bus;
	

	public Gateway(String host, int port) {
		super(host,port);
		
	}

	void setBus(MessageBus bus){
		this.bus = bus;
	}
	

	@Override
	public void onMessageReceived(Context context, Session session, ByteBuffer buffer) {
		if(bus == null){
			LOGGER.warn("The 'Bus' not found,received message was be igrone.");
			return;
		}
		Message message = new Message();
		message.decode(buffer);
		bus.onRouterMessage(session, message);
		
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("[Gateway] onMessageReceived:"+message);
		}
	}

}
