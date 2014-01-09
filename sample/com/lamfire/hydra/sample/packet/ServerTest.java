package com.lamfire.hydra.sample.packet;

import java.nio.ByteBuffer;

import com.lamfire.hydra.net.*;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;

public class ServerTest extends Server implements MessageHandler,SessionEventListener{
    @Override
    public void onClosed(Context context, Session session) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onConnected(Context context, Session session) {
        group.add(session.getSessionId(),session);
    }

    @Override
    public void onDisconnected(Context context, Session session) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onOpen(Context context, Session session) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onHeatbeat(Context context, Session session, HeatbeatType heatbeat) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onExceptionCaught(Context context, Session session, Throwable throwable) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private SessionGroup group = new SessionGroup();

	public ServerTest(String bind, int port) {
		super(bind, port);
	}
	
	public static void main(String[] args) {
		ServerTest test= new ServerTest("127.0.0.1",1200);
		test.setMessageHandler(test);
        test.setSessionEventListener(test);
		test.bind();
		
	}

	@Override
	public void onMessageReceived(Context context, Session session, ByteBuffer buffer) {
		session.send(buffer);
	}

}
