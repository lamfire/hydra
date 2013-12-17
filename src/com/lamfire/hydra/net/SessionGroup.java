package com.lamfire.hydra.net;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

import com.lamfire.utils.Sets;

public class SessionGroup {
	private final Lock lock = new ReentrantLock();
	private final Map<Integer, Session> sessionMap = new ConcurrentHashMap<Integer, Session>();
	private final Map<Serializable, Integer> idMap = new HashMap<Serializable, Integer>();
	private final Map<Integer, Serializable> keyMap = new HashMap<Integer, Serializable>();

	private final ChannelFutureListener remover = new ChannelFutureListener() {
		public void operationComplete(ChannelFuture future) throws Exception {
			int sessionId = future.getChannel().getId();
			Serializable key = keyMap.get(sessionId);
			remove(key);
		}
	};

	public boolean add(Serializable key, Session s) {
		lock.lock();
		try {
			Channel channel = s.getChannel();
			int sessionId = s.getSessionId();

			if (!sessionMap.containsKey(sessionId)) {
				sessionMap.put(sessionId, s);
				channel.getCloseFuture().addListener(remover);
				idMap.put(key, sessionId);
				keyMap.put(sessionId, key);
				return true;
			}
		} finally {
			lock.unlock();
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
}
