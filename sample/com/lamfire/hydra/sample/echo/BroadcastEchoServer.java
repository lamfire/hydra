package com.lamfire.hydra.sample.echo;

import com.lamfire.hydra.*;
import com.lamfire.logger.Logger;
import com.lamfire.hydra.Snake;
import com.lamfire.hydra.net.Session;

public class BroadcastEchoServer {
	private static final Logger LOGGER = Logger.getLogger(BroadcastEchoServer.class);
	private static Snake executor;
	public static void startup(String host,int port) {
		MessageMapper mapper = new MessageMapper();
		mapper.registAction(1001, new Action(){
			@Override
			public void execute(MessageContext context, Message message) {
				LOGGER.debug(message.toString());
				broadcast(message);
			}
			
			private void broadcast(Message message){
				for(Session s:executor.getSessions()){
					s.send(message);
				}
			}
			
		});
		executor = new IdentitySnake(mapper,host, port);
        executor.setWorkerThreads(16);
		executor.bind();
	}
	
	
	
	static void usage(){
		LOGGER.info("com.lamfire.hydra.sample.ServiceServer [host] [port]");
	}
	
	public static void main(String[] args) {
		String host = "127.0.0.1";
		int port = 8001;
		
		if(args.length > 0 && "?".equals(args[0])){
			usage();
			return;
		}
		
		if(args.length == 2){
			host = args[0];
			port = Integer.valueOf(args[1]);
		}
		
		LOGGER.info("[startup]:"+host + ":" +port);
		startup(host,port);
	}
}
