package com.lamfire.hydra;

import java.util.LinkedList;
import java.util.List;
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
	private final List<Destination> destinations = new LinkedList<Destination>();
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
		scheduler.submit(this);
	}
	
	public void addDestination(Destination destination){
		destination.setBus(this);
		this.destinations.add(destination);
	}

	public Gateway getGateway() {
		return gateway;
	}

	public List<Destination> getDestinations() {
		return destinations;
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
	
	protected abstract void onDispatch(List<Destination> destinations,Message message);

	/**
	 * 消息路由器收到消息回应
	 */
	void onDealerMessage(Session session, Message message) {
		int sid = message.popLink();
		Session s = gateway.getSession(sid);
		if(s != null){
			s.send(message);
		}
	}
	
	private boolean hasAlivedDestination(){
		for(Destination destination : this.destinations){
			if(destination.hasConnections()){
				return true;
			}
		}
		return false;
	}

	@Override
	public void run() {
		while(running){
			if(hasAlivedDestination()){
				Message message = messageQueue.pop();
				onDispatch(getDestinations(),message);
			}
		}
	}
	
	public void shutdown(){
		scheduler.shutdown();
	}
	
}
