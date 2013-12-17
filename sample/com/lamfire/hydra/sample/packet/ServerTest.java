package com.lamfire.hydra.sample.packet;

import java.nio.ByteBuffer;

import com.lamfire.hydra.net.Context;
import com.lamfire.hydra.net.MessageHandler;
import com.lamfire.hydra.net.Server;
import com.lamfire.hydra.net.Session;

public class ServerTest extends Server implements MessageHandler{

	public ServerTest(String bind, int port) {
		super(bind, port);
	}
	
	public static void main(String[] args) {
		ServerTest test= new ServerTest("127.0.0.1",1200);
		test.setMessageHandler(test);
		test.bind();
		
	}

	@Override
	public void onMessageReceived(Context context, Session session, ByteBuffer buffer) {
		session.send(buffer);
	}

}
