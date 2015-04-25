package com.lamfire.hydra;

import java.nio.ByteBuffer;

import com.lamfire.hydra.Message;
import com.lamfire.logger.Logger;
import com.lamfire.hydra.Context;
import com.lamfire.hydra.Session;

/**
 * 蛇
 * 当网络通信建立起来后，负责创建消息或者消费消息
 * @author lamfire
 *
 */
public abstract class Snake extends Hydra {
	static final Logger LOGGER = Logger.getLogger(Snake.class);
		
	public Snake(String host, int port){
		super(host,port);
	}

	public void onMessageReceived(Context context, Session session, Message message) {
        MessageContext mc = new MessageContext();
        mc.setContext(context);
        mc.setSession(session);
        mc.setMessage(message);
        mc.setNodes(message.getLinks());
        handleMessage(mc, message);
	}
	
	protected abstract void handleMessage(final MessageContext context,final Message message);

	@Override
	public void shutdown() {
		super.shutdown();
	}

}
