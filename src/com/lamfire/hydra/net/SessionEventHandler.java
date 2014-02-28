package com.lamfire.hydra.net;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.lamfire.logger.Logger;
import com.lamfire.utils.Maps;
import com.lamfire.utils.ThreadFactory;

abstract class SessionEventHandler extends SimpleChannelUpstreamHandler implements Context{
	private static final Logger LOGGER = Logger.getLogger(SessionEventHandler.class);
	private final Map<Integer,Session> sessions = Maps.newHashMap();//所有的SESSION
	private ExecutorService service = null;
	
	private SessionEventListener sessionEventListener;
	private MessageHandler messageHandler;

	public void setSessionEventListener(SessionEventListener listener){
		this.sessionEventListener = listener;
	}

    public void setExecutorService(ExecutorService service){
        this.service = service;
    }
	
	public void setMessageHandler(MessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}

	public Session getSession(int sessionId){
		return sessions.get(sessionId);
	}
	
	public Session getSession(Channel channel){
		return getSession(channel.getId());
	}
	
	public Collection<Session> getSessions(){
		return sessions.values();
	}

	public synchronized void shutdown() {
		sessions.clear();
        if(service != null){
            service.shutdown();
            service = null;
        }
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		if(e.getMessage() == null){
			//ctx.sendUpstream(e);
			return;
		}
		if ((e.getMessage() instanceof HeatbeatType)) {//为心跳
			Session session = sessions.get(e.getChannel().getId());
			HeatbeatType heatbeat = (HeatbeatType)e.getMessage();
			
			if(LOGGER.isDebugEnabled())
			LOGGER.debug("[HEATBEAT]:recieved heatbeat "+heatbeat+" from " + session);
			
			
			if(heatbeat == HeatbeatType.Request){
				session.sendHeatbeatReply();
				if(LOGGER.isDebugEnabled())
				LOGGER.debug("[HEATBEAT]:sending heatbeat reply to " + session);
			}
			SessionUtils.onHeatbeat(this, sessionEventListener, session,(HeatbeatType)e.getMessage());
			return;
		}
		if (!(e.getMessage() instanceof ByteBuffer)) {
			//ctx.sendUpstream(e);
			return;
		}
		
		
		ByteBuffer buffer = (ByteBuffer)e.getMessage();
		Session session = sessions.get(e.getChannel().getId());
		
		//sync execute ?
		//SessionUtils.onMessageReceived(this,messageHandler, session, buffer);
		
		//
		Task task = new Task(this,session,buffer);
        if(service != null){
		    service.submit(task);
        }   else{
            task.run();
        }
	}
	

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		super.channelConnected(ctx, e);
		Session session = sessions.get(e.getChannel().getId());
		SessionUtils.onConnected(this,sessionEventListener, session);
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("channelConnected:"+session);
		}
			
	}



	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		super.channelDisconnected(ctx, e);
		Session session = sessions.get(e.getChannel().getId());
		SessionUtils.onDisconnected(this,sessionEventListener, session);
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("channelDisconnected:"+session);
		}
	}



	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		super.channelOpen(ctx, e);
		Session session = new SessionImpl(e.getChannel());
		sessions.put(session.getSessionId(), session);
		SessionUtils.onOpen(this,sessionEventListener, session);
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("channelOpen:"+session);
		}
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		super.channelClosed(ctx, e);
		Session session = sessions.remove(e.getChannel().getId());
        if(session == null){
            return;
        }
        SessionClosedListener listener = session.getSessionClosedListener();
        if(listener != null){
            listener.onClosed(session);
        }
		SessionUtils.onClosed(this,sessionEventListener, session);
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("channelClosed:"+session);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		Session session = sessions.get(e.getChannel().getId());
		SessionUtils.onExceptionCaught(this,sessionEventListener, session,e.getCause());
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("exceptionCaught:"+session,e.getCause());
		}
	}
	
	class Task implements Runnable{
		Context context; 
		Session session; 
		ByteBuffer buffer;

		public Task(Context context, Session session, ByteBuffer buffer) {
			this.context = context;
			this.session = session;
			this.buffer = buffer;
		}

		@Override
		public void run() {
			SessionUtils.onMessageReceived(context,messageHandler, session, buffer);
		}
		
	}
}
