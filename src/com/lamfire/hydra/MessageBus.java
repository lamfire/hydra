package com.lamfire.hydra;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
    private Lock lock = new ReentrantLock();
	
	
	public MessageBus(Gateway gateway){
		this(gateway,1000000);
	}
	
	public MessageBus(Gateway gateway,int bufferSize){
		this.gateway = gateway;
		this.gateway.setBus(this);
		this.messageQueue = new FixedQueue<Message>(bufferSize);
		this.scheduler.submit(this);
	}
	
	public synchronized void addDestination(HydraDestination destination){
        try{
            lock.lock();
            destination.setMessageBus(this);
            this.destinations.put(destination.getName(), destination);
        }finally {
            lock.unlock();
        }
	}

    public synchronized void addDestination(String name,Destination destination){
        try{
            lock.lock();
            if(destination instanceof HydraDestination){
                addDestination(((HydraDestination)destination));
                return;
            }
            this.destinations.put(name, destination);
        }finally {
            lock.unlock();
        }
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
		if(s != null){
            onSendReplyMessage(s,message);
			return;
		}
        LOGGER.warn("request session was closed,ignore reply:" + new String(message.getBody()));
    }

    protected void onSendReplyMessage(Session replySession,Message message){
        replySession.send(message);
    }
	
	private boolean hasAlivedDestination(){
        try{
            lock.lock();
            for(Destination destination : this.getDestinations().values()){
                if(!(destination instanceof  HydraDestination)){
                     return true;
                }
                HydraDestination hydra = (HydraDestination) destination;
                if(hydra.hasConnections()){
                    return true;
                }
            }
            return false;
        }finally {
            lock.unlock();
        }
	}

    private synchronized void dispatchMessage(){
        try{
            if(hasAlivedDestination()){
                Message message = messageQueue.pop();
                onDispatch(getDestinations(),message);
            } else{
                LOGGER.warn("not found alive destination,waiting...");
                this.wait(5000);
            }
        }catch (Throwable t){
            LOGGER.warn(t.getMessage(),t);
        }
    }

	@Override
	public void run() {
		while(running){
            dispatchMessage();
		}
	}
	
	public void shutdown(){
		scheduler.shutdown();
	}
	
}
