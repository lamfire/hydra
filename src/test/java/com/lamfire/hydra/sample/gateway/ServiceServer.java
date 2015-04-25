package com.lamfire.hydra.sample.gateway;

import com.lamfire.hydra.*;
import com.lamfire.hydra.Message;
import com.lamfire.logger.Logger;
import com.lamfire.hydra.Snake;

public class ServiceServer {
	private static final Logger LOGGER = Logger.getLogger(ServiceServer.class);
	
	public static void startup(String host,int port) {
		MessageMapper mapper = new MessageMapper();
		mapper.registAction(1001, new Action(){
			@Override
			public void execute(MessageContext context, Message message) {
				LOGGER.debug(message.toString());
				context.send(message.getId(),message.getBody());
			}
			
		});
		Snake executor = new IdentitySnake(mapper,host, port);
        executor.setAutoConnectRetry(true);
        executor.setKeepAlive(true);
		executor.connect();
	}
	
	static void usage(){
		LOGGER.info("com.lamfire.nkit.sample.ServiceServer [host] [port]");
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
