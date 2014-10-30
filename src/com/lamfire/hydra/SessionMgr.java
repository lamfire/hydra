package com.lamfire.hydra;

import java.util.*;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.lamfire.logger.Logger;
import com.lamfire.utils.Maps;

abstract class SessionMgr extends SimpleChannelUpstreamHandler implements Context{
	private static final Logger LOGGER = Logger.getLogger(SessionMgr.class);
	private final Map<Integer,Session> sessions = Maps.newHashMap();//所有已连接上的SESSION
    private final Object SESSION_WAIT_LOCK = 1;
    private final CycleSessionIterator cycleSessionIterator;
	private SessionEventListener sessionEventListener;
	private MessageHandler messageHandler;

    public SessionMgr(){
        cycleSessionIterator = new CycleSessionIterator(this);
    }

	public void setSessionEventListener(SessionEventListener listener){
		this.sessionEventListener = listener;
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
        return Collections.unmodifiableCollection(sessions.values());
	}

    public Session awaitAvailableSession(){
        Session session = null;
        synchronized (SESSION_WAIT_LOCK){
            while(session == null){
                session = cycleSessionIterator.nextAvailableSession();
                if(session == null){
                    try {
                        SESSION_WAIT_LOCK.wait();
                    } catch (InterruptedException e) {
                        LOGGER.warn(e.getMessage(),e);
                    }
                }
            }
        }
        return session;
    }

    private Session makeSession(Channel channel){
        Session session = new SessionImpl(channel);
        synchronized (SESSION_WAIT_LOCK){
            sessions.put(session.getSessionId(), session);
            SESSION_WAIT_LOCK.notifyAll();
        }
        return session;
    }

    protected synchronized void closeAllSessions(){
        if(sessions.isEmpty()){
            return;
        }
        List<Session> list = new ArrayList<Session>(sessions.values());
        for(Session s : list){
            s.close();
        }
        sessions.clear();
    }

	public synchronized void shutdown() {
        closeAllSessions();
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
		if (!(e.getMessage() instanceof Message)) {
			//ctx.sendUpstream(e);
			return;
		}
		
		
		Message message = (Message)e.getMessage();
		Session session = sessions.get(e.getChannel().getId());
		
		//sync execute ?
        if(session != null){
		    SessionUtils.onMessageReceived(this,messageHandler, session, message);
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
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        super.channelConnected(ctx, e);
        Session session = makeSession(ctx.getChannel());
        SessionUtils.onConnected(this,sessionEventListener, session);
        if(LOGGER.isDebugEnabled()){
            LOGGER.debug("channelConnected:"+session);
        }

    }

	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		super.channelOpen(ctx, e);
		Session session = new SessionImpl(e.getChannel());
		//sessions.put(session.getSessionId(), session);
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
}
