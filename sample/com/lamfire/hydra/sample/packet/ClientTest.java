package com.lamfire.hydra.sample.packet;

import java.nio.ByteBuffer;

import com.lamfire.hydra.net.Client;
import com.lamfire.hydra.net.Context;
import com.lamfire.hydra.net.MessageHandler;
import com.lamfire.hydra.net.Session;

public class ClientTest extends Client implements MessageHandler{

	public ClientTest(String bind, int port) {
		super(bind, port);
	}
	
	public static void main(String[] args) {
		ClientTest test= new ClientTest("127.0.0.1",1200);
		test.setMessageHandler(test);
		Session s = test.connect();
		byte[] bytes = "heelo lamfire".getBytes();
		ByteBuffer buffer = ByteBuffer.allocate(4 + bytes.length);
		buffer.putInt(bytes.length);
		buffer.put(bytes);
		s.send(buffer);
		
	}

	@Override
	public void onMessageReceived(Context context, Session session, ByteBuffer buffer) {
		buffer.flip();
		int len =  buffer.getInt();
		byte[] bytes = new byte[len];
		buffer.get(bytes);
		System.out.println(new String(bytes));
		session.send(buffer);
	}

}
