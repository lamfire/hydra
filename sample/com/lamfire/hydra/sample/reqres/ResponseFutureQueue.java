package com.lamfire.hydra.sample.reqres;

import java.util.concurrent.ConcurrentHashMap;


import com.lamfire.logger.Logger;


public class ResponseFutureQueue extends ConcurrentHashMap<Integer, ResponseFuture>{
    static final Logger LOGGER = Logger.getLogger(ResponseFutureQueue.class);
	private static final long serialVersionUID = -568333872142979774L;
	
	public void addFuture(ResponseFuture future){
		this.put(future.getMessageId(), future);
	}
}
