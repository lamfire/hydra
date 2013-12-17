package com.lamfire.hydra.sample;

import java.lang.management.ManagementFactory;

import com.lamfire.hydra.*;
import com.lamfire.logger.Logger;
import com.lamfire.hydra.Snake;
import com.lamfire.hydra.net.Session;

public class ClientSample {
	private static final Logger LOGGER = Logger.getLogger(ClientSample.class);
	
	public static void startup(String host,int port,int size){	
		MessageMapper mapper = new MessageMapper();
		mapper.registAction(1001, new Action() {
			@Override
			public void execute(MessageContext context, Message message) {
				LOGGER.info(context.getSessionId()  +" - " +message.toString());
				context.send(message.getId(),message.getBody());
			}
		});
		
		final Snake executor = new IdentitySnake(mapper,host, port);
		executor.setKeepaliveConnsWithClient(size);
		//connect
		for(int i=0;i<size;i++){
			Session session = executor.connect();
			//for(int n=0;n<100;n++){
			session.send(new Message(1001,ManagementFactory.getRuntimeMXBean().getName().getBytes()));
			//}
		}
	}
	
	static void usage(){
		System.out.println("com.lamfire.nkit.sample.ClientSample [host] [port] [connsize]");
	}
	
	public static void main(String[] args) throws Exception {
		String host = "127.0.0.1";
		int port = 8000;
		int size = 1;
		
		if(args.length > 0 && "?".equals(args[0])){
			usage();
			return;
		}
		
		if(args.length == 3){
			host = args[0];
			port = Integer.valueOf(args[1]);
			size = Integer.valueOf(args[2]);
		}
		
		System.out.println("[startup]:"+host + ":" +port  +" - "+ size);
		startup(host,port,size);
	}
}
