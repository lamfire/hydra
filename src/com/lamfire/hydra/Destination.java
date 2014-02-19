package com.lamfire.hydra;

import java.nio.ByteBuffer;

import com.lamfire.logger.Logger;
import com.lamfire.hydra.net.Context;
import com.lamfire.hydra.net.Session;

/**
 * 消息调度器
 * @author lamfire
 *
 */
public abstract class Destination extends Hydra {
	static final Logger LOGGER = Logger.getLogger(Destination.class);
	private MessageBus bus;
	private String name;
	
	public static final Destination newBroadcastDestination(String host, int port){
		return new BroadcastDestination(host,port);
	}
	
	public static final Destination newPollerDestination(String host, int port){
		return new PollerDestination(host,port);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Destination(String host, int port) {
		super(host,port);
	}

    public Destination(String name,String host, int port) {
        super(host,port);
        this.name = name;
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
		bus.onReplyMessage(session, message);
        if(LOGGER.isDebugEnabled()){
		    LOGGER.debug("[Dispatcher] onMessageReceived:"+message);
        }
	}


	public abstract void forwardMessage(Message message);
	
}
