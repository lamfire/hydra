package com.lamfire.hydra;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.lamfire.logger.Logger;
import com.lamfire.hydra.net.Session;
import com.lamfire.hydra.utils.FixedQueue;
import com.lamfire.utils.ThreadFactory;

/**
 * 消息总线
 * @author lamfire
 *
 */
public abstract class MessageBus implements Runnable{
	static final Logger LOGGER = Logger.getLogger(MessageBus.class);
	private Gateway gateway;
	private final Map<String,Destination> destinations = new HashMap<String,Destination>();
	private final FixedQueue<Message> messageQueue;
	private final ExecutorService scheduler = Executors.newFixedThreadPool(1, new ThreadFactory("MessageBus#Cache"));
	private boolean running = true;
	
	
	public MessageBus(Gateway gateway){
		this(gateway,1000000);
	}
	
	public MessageBus(Gateway gateway,int bufferSize){
		this.gateway = gateway;
		this.gateway.setBus(this);
		this.messageQueue = new FixedQueue<Message>(bufferSize);
		this.scheduler.submit(this);
	}
	
	public void addDestination(HydraDestination destination){
		destination.setBus(this);
		this.destinations.put(destination.getName(), destination);
	}

    public void addDestination(String name,Destination destination){
        this.destinations.put(name, destination);
    }

	public Gateway getGateway() {
		return gateway;
	}

	public Map<String,? extends Destination> getDestinations() {
		return Collections.unmodifiableMap(destinations);
	}
	
	/**
	 * 消息接收器收到消息请求
	 */
	void onRouterMessage(Session session, Message message) {
		try{
			message.addLink(session.getSessionId());
			messageQueue.push(message);
		}catch (Exception e) {
			LOGGER.error("[ERROR]:" + e.getMessage());
		}
	}
	
	protected abstract void onDispatch(Map<String,? extends Destination> destinations,Message message);

	/**
	 * 消息路由器收到消息回应
	 */
	void onReplyMessage(Session session, Message message) {
		int sid = message.popLink();
		Session s = gateway.getSession(sid);
		if(s == null){
			LOGGER.warn("request session was closed,ignore reply:" + new String(message.getBody()));
		}
        s.send(message);

	}
	
	private boolean hasAlivedDestination(){
		for(Destination destination : this.destinations.values()){
            if(!(destination instanceof  HydraDestination)){
                 return true;
            }
            HydraDestination hydra = (HydraDestination) destination;
			if(hydra.hasConnections()){
				return true;
			}
		}
		return false;
	}

	@Override
	public void run() {
		while(running){
            try{
                if(hasAlivedDestination()){
                    Message message = messageQueue.pop();
                    onDispatch(getDestinations(),message);
                }
            }catch (Throwable t){
                LOGGER.warn(t.getMessage(),t);
            }
		}
	}
	
	public void shutdown(){
		scheduler.shutdown();
	}
	
}
