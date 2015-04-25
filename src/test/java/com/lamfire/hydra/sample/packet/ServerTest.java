package com.lamfire.hydra.sample.packet;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.lamfire.code.PUID;
import com.lamfire.hydra.*;


public class ServerTest extends Server implements MessageHandler,SessionEventListener{
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private Runnable sessionGroupPrinter = new Runnable() {
        @Override
        public void run() {
            System.out.println("[SessionGroup Size] : "+group.size());
        }
    } ;

    @Override
    public void onClosed(Context context, Session session) {
        System.out.println("onClosed : "+session);
    }

    @Override
    public void onConnected(Context context, Session session) {
        System.out.println("onConnected : "+session);
        group.add(PUID.puidAsString(),session);
        session.setSessionClosedListener(new SessionClosedListener() {
            @Override
            public void onClosed(Session session) {
                System.out.println("SessionClosedListener : "+session);
            }
        });
    }

    @Override
    public void onDisconnected(Context context, Session session) {
        System.out.println("onDisconnected : "+session);
    }

    @Override
    public void onOpen(Context context, Session session) {
        System.out.println("onOpen : "+session);
    }

    @Override
    public void onHeatbeat(Context context, Session session, HeatbeatType heatbeat) {
        System.out.println("onHeatbeat : "+session);
    }

    @Override
    public void onExceptionCaught(Context context, Session session, Throwable throwable) {
        System.out.println("onExceptionCaught : "+session);
    }

    private SessionGroup group = new SessionGroup();

	public ServerTest(String bind, int port) {
		super(bind, port);
        scheduler.scheduleWithFixedDelay(sessionGroupPrinter,1,1, TimeUnit.SECONDS);
	}
	
	public static void main(String[] args) {
		ServerTest test= new ServerTest("127.0.0.1",1200);
		test.setMessageHandler(test);
        test.setSessionEventListener(test);
		test.bind();
		
	}

	@Override
	public void onMessageReceived(Context context, Session session, Message message) {
		session.send(message);
	}

}
