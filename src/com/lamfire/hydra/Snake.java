package com.lamfire.hydra;

import java.nio.ByteBuffer;

import com.lamfire.logger.Logger;
import com.lamfire.hydra.net.Context;
import com.lamfire.hydra.net.Session;

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

	public void onMessageReceived(Context context, Session session, ByteBuffer buffer) {
		Task task = new Task(context, session, buffer);
		//线程池执行?
		task.run();
	}
	
	protected abstract void handleMessage(final MessageContext context,final Message message);

	@Override
	public void shutdown() {
		super.shutdown();
	}
	
	class Task implements Runnable{
		Context context; 
		Session session; 
		ByteBuffer buffer;

		public Task(Context context, Session session, ByteBuffer buffer) {
			super();
			this.context = context;
			this.session = session;
			this.buffer = buffer;
		}

		@Override
		public void run() {
			Message message = new Message();
			message.decode(buffer);
			MessageContext mc = new MessageContext();
			mc.setContext(this.context);
			mc.setSession(this.session);
			mc.setMessage(message);
			mc.setNodes(message.getLinks());
			handleMessage(mc, message);
		}
		
	}
}
