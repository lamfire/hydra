package com.lamfire.hydra;

import java.util.Iterator;

import com.lamfire.hydra.Message;
import com.lamfire.hydra.Session;

/**
 * 广播调度器
 * @author lamfire
 *
 */
public class BroadcastDestination extends HydraDestination {

	public BroadcastDestination(String host, int port) {
		super(host, port);
	}

	@Override
	public void forwardMessage(Message message) {
		Iterator<Session> it = getSessions().iterator();
		while(it.hasNext()){
			Session session = it.next();
			session.send(message);
		}
	}

}
