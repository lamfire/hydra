package com.lamfire.hydra;

import java.util.Map;

import com.lamfire.hydra.Message;
import com.lamfire.logger.Logger;

/**
 * 缺省消息总线
 * @author lamfire
 *
 */
public class DefaultMessageBus extends MessageBus{
	static final Logger LOGGER = Logger.getLogger(DefaultMessageBus.class);
	
	public DefaultMessageBus(Gateway gateway) {
		super(gateway);
	}


	public DefaultMessageBus(Gateway gateway, int bufferSize) {
		super(gateway, bufferSize);
	}

	protected void onDispatch(Map<String,? extends Destination> destinations,Message message){
		for(Destination destination : destinations.values()){
			destination.forwardMessage(message);
		}
	}

}
