package com.lamfire.hydra;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.lamfire.hydra.exception.NotSupportedMethodException;
import com.lamfire.hydra.Session;

public class CycleSessionIterator implements Iterator<Session>{
	private SessionMgr sessionMgr;
	private final Queue<Session> queue = new ConcurrentLinkedQueue<Session>();
	
	public CycleSessionIterator(SessionMgr hydra){
		this.sessionMgr = hydra;
	}
	
	private synchronized Session nextSession(){
		if(queue.isEmpty()){
			Collection<Session> sessions = sessionMgr.getSessions();
			if(sessions == null || sessions.isEmpty()){
				return null;
			}
			queue.addAll(sessions);
		}
		return queue.poll();
	}
	
	public synchronized Session nextAvailableSession(){
		if(!hasNext()){
			return null;
		}
		Session session = nextSession();
		int count = 0;
        while(session == null || !session.isConnected()){
        	session = nextSession();
            if(count >= sessionMgr.getSessions().size()){
                return null;
            }
        }
		return session;
	}

	@Override
	public boolean hasNext() {
		Collection<Session> sessions = sessionMgr.getSessions();
		if(sessions == null){
			return false;
		}
		return !sessions.isEmpty();
	}

	@Override
	public Session next() {
		return nextSession();
	}

	@Override
	public void remove() {
		throw new NotSupportedMethodException();
	}
}
