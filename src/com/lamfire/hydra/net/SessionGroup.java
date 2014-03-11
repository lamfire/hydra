package com.lamfire.hydra.net;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.lamfire.hydra.packet.Packet;
import com.lamfire.logger.Logger;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

import com.lamfire.utils.Sets;

public class SessionGroup implements Iterable<Session>{
    private static final Logger LOGGER = Logger.getLogger(SessionGroup.class);
	private final Lock lock = new ReentrantLock();
	private final ConcurrentHashMap<Serializable, Session> group = new ConcurrentHashMap<Serializable, Session>();
    private Iterator<Entry<Serializable, Session>> groupIterator = null;

	private final ChannelFutureListener remover = new ChannelFutureListener() {
		public void operationComplete(ChannelFuture future) throws Exception {
			int sessionId = future.getChannel().getId();
			Serializable key = getKeyBySessionId(sessionId);
            if(key == null){
			    return;
            }
            Session session = remove(key);
            if(LOGGER.isDebugEnabled()){
                LOGGER.debug("The session["+session+"] was closed,removed it,group size was " + group.size());
            }
		}
	};

    public Session getPollingNextSession(){
        lock.lock();
        try{
            if(group.isEmpty()){
                return null;
            }
            if(this.groupIterator == null || (!this.groupIterator.hasNext()) ){
                this.groupIterator = this.group.entrySet().iterator();
            }
            return this.groupIterator.next().getValue();
        }catch (Throwable t){
            this.groupIterator = null;
            if(!group.isEmpty()){
                return group.elements().nextElement();
            }
        } finally {
            lock.unlock();
        }
        return null;
    }

    private Session getSessionBySessionId(int sessionId){
        lock.lock();
        try{
            for(Session s : group.values()){
                if(s.getSessionId() == sessionId){
                    return s;
                }
            }
        }finally {
            lock.unlock();
        }
        return null;
    }

    private Serializable getKeyBySessionId(int sessionId){
        lock.lock();
        try{
            for(Entry<Serializable,Session> e : group.entrySet()){
                Session s = e.getValue();
                if(s.getSessionId() == sessionId){
                    return e.getKey();
                }
            }
        }finally {
            lock.unlock();
        }
        return null;
    }

	public boolean add(Serializable key, Session session) {
		lock.lock();
		try {
            //exists?
            if(group.containsKey(key)){
                remove(key);
            }

			if (!group.values().contains(session)) {
				group.put(key, session);
				bindRemoveListener(session);
                if(LOGGER.isDebugEnabled()){
                    LOGGER.debug("The session["+session+"] added,group size was " + group.size());
                }
				return true;
			}
		} finally {
			lock.unlock();
		}

		return false;
	}

    private void send(Session session , byte[] bytes){
         if(session.isConnected() && session.isSendable()){
             session.send(bytes);
         }
    }

    public void broadcast(byte[] bytes){
        for(Session session :  sessions()){
            send(session,bytes);
        }
    }

    public void broadcast(Packet<?> packet){
        for(Session session :  sessions()){
            send(session,packet.encode().array());
        }
    }

    public void close() {
		lock.lock();
		try {
			for (Session session : sessions()) {
                unbindRemoveListener(session);
                session.close();
			}
            group.clear();
		} finally {
			lock.unlock();
		}
	}

	public boolean isEmpty() {
		return group.isEmpty();
	}

	public Session remove(Serializable key) {
		lock.lock();
		try {
			Session s = group.remove(key);
			if (s != null) {
                unbindRemoveListener(s);
				return s;
			}
		} finally {
			lock.unlock();
		}
        return null;
	}

    private void bindRemoveListener(Session session){
        session.getChannel().getCloseFuture().addListener(remover);
    }

    private void unbindRemoveListener(Session session){
        session.getChannel().getCloseFuture().removeListener(remover);
    }

	public int remove(Collection<Serializable> keys) {
		lock.lock();
		try {
			int count = 0;
			for (Serializable key : keys) {
				Session s = remove(key);
				if (s != null) {
					count++;
				}
			}
			return count;
		} finally {
			lock.unlock();
		}
	}

	public Session get(Serializable key) {
		return group.get(key);
	}

	public int size() {
		return group.size();
	}

	public Collection<Session> sessions() {
		return Sets.newCopyOnWriteArraySet(group.values());
	}

	public Collection<Serializable> keys() {
		return group.keySet();
	}

	public boolean exists(Serializable key) {
		return group.containsKey(key);
	}

    @Override
    public Iterator<Session> iterator() {
        return group.values().iterator();
    }
}
