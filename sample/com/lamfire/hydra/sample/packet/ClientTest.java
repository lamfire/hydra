package com.lamfire.hydra.sample.packet;


import com.lamfire.hydra.*;

public class ClientTest extends Client implements MessageHandler {

	public ClientTest(String bind, int port) {
		super(bind, port);
	}
	
	public static void main(String[] args) {
		ClientTest test= new ClientTest("127.0.0.1",1200);
		test.setMessageHandler(test);
		test.connect();

        Session s = test.awaitAvailableSession();
		byte[] bytes = "heelo lamfire".getBytes();

        Message message = new Message(0,bytes);
		s.send(message);
		
	}

	@Override
	public void onMessageReceived(Context context, Session session, Message message) {
		System.out.println(new String(message.getBody()));
		session.send(message);
	}

}
