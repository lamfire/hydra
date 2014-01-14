package com.lamfire.hydra.net;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.lamfire.logger.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

import com.lamfire.utils.Sets;

public class SessionGroup implements Iterable<Session>{
    private static final Logger LOGGER = Logger.getLogger(SessionGroup.class);
	private final Lock lock = new ReentrantLock();
	private final Map<Integer, Session> sessionMap = new ConcurrentHashMap<Integer, Session>();
	private final Map<Serializable, Integer> idMap = new HashMap<Serializable, Integer>();
	private final Map<Integer, Serializable> keyMap = new HashMap<Integer, Serializable>();
    private Iterator<Session> iterator;

	private final ChannelFutureListener remover = new ChannelFutureListener() {
		public void operationComplete(ChannelFuture future) throws Exception {
			int sessionId = future.getChannel().getId();
			Serializable key = keyMap.get(sessionId);
			remove(key);
            iterator = iterator();
            if(LOGGER.isDebugEnabled()){
                LOGGER.debug("The session["+sessionId+"] was closed,removed it,group size was " + sessionMap.size());
            }
		}
	};

	public boolean add(Serializable key, Session s) {
		lock.lock();
		try {
			Channel channel = s.getChannel();
			int sessionId = s.getSessionId();

            //exists?
            if(keyMap.containsKey(key)){
                remove(key);
            }

            idMap.put(key, sessionId);
            keyMap.put(sessionId, key);

			if (!sessionMap.containsKey(sessionId)) {
				sessionMap.put(sessionId, s);
				channel.getCloseFuture().addListener(remover);
                if(LOGGER.isDebugEnabled()){
                    LOGGER.debug("The session["+sessionId+"] added,group size was " + sessionMap.size());
                }
				return true;
			}
		} finally {
			lock.unlock();
            iterator = iterator();
		}

		return false;
	}

	public Serializable getKeyBySessionId(Integer sessionId) {
		return keyMap.get(sessionId);
	}

	public void close() {
		lock.lock();
		try {
			Set<Entry<Integer, Session>> sessions = Sets.newCopyOnWriteArraySet(sessionMap.entrySet());
			for (Map.Entry<Integer, Session> entry : sessions) {
				Session s = entry.getValue();
				s.close();
			}
			sessionMap.clear();
			idMap.clear();
			keyMap.clear();
		} finally {
			lock.unlock();
		}
	}

	public boolean isEmpty() {
		return sessionMap.isEmpty();
	}

	public Session remove(Serializable key) {
		lock.lock();
		try {
			Integer sessionId = idMap.remove(key);
			if (sessionId == null) {
				return null;
			}
			keyMap.remove(sessionId);
			Session s = sessionMap.remove(sessionId);
			if (s == null) {
				return null;
			}
			s.getChannel().getCloseFuture().removeListener(remover);
			return s;
		} finally {
			lock.unlock();
            iterator = iterator();
		}
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
		Integer sessionId = idMap.get(key);
		if (sessionId == null)
			return null;
		return sessionMap.get(sessionId);
	}
	
	public Session getBySessionId(Integer sessionId){
		Serializable key = keyMap.get(sessionId);
		if(key != null){
			return sessionMap.get(key);
		}
		return null;
	}

	public int size() {
		return sessionMap.size();
	}

	public Collection<Session> sessions() {
		return sessionMap.values();
	}

	public Collection<Serializable> keys() {
		return idMap.keySet();
	}

	public boolean existsKey(Serializable key) {
		return idMap.containsKey(key);
	}

	public boolean existsSession(int sessionId) {
		return keyMap.containsKey(sessionId);
	}

    @Override
    public Iterator<Session> iterator() {
        return sessionMap.values().iterator();
    }


    public synchronized Session next(){
        lock.lock();
        try {
            try{
                if(this.iterator.hasNext()){
                    return this.iterator.next();
                }
                this.iterator = iterator();
                if(!this.iterator.hasNext()){
                    return null;
                }
            }catch (Throwable e){
                this.iterator = iterator();
                if(!this.iterator.hasNext()){
                    return null;
                }
            }
            return next();
        } finally {
            lock.unlock();
        }
    }
}
