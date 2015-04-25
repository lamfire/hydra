package com.lamfire.hydra;

import java.util.Iterator;

import com.lamfire.hydra.Message;
import com.lamfire.logger.Logger;
import com.lamfire.hydra.Session;

/**
 * 顺序轮循调度器
 * @author lamfire
 *
 */
public class PollerDestination extends HydraDestination {
	private static final Logger LOGGER = Logger.getLogger(PollerDestination.class);
	public PollerDestination(String host, int port) {
		super(host, port);
	}

	private Iterator<Session> it;
	
	public void forwardMessage(Message message) {
		if (it == null || !it.hasNext()) {
			it = getSessions().iterator();
		}
		
		if(!it.hasNext()){
			LOGGER.error("Not found available 'Destination', from " + getName());
			return;
		}
		
		try {
			Session s = it.next();
			if(s.isConnected()){
				s.send(message);
			}
		} catch (Exception e) {
			it = null;
		}
	}

}