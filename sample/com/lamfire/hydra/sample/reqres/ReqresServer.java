package com.lamfire.hydra.sample.reqres;

import com.lamfire.hydra.Snake;
import com.lamfire.logger.Logger;
import com.lamfire.hydra.Message;
import com.lamfire.hydra.MessageContext;

public class ReqresServer extends Snake {
	private static final Logger LOGGER = Logger.getLogger(ReqresServer.class);
	
	public ReqresServer(String host, int port) {
		super(host, port);
	}

	static void usage(){
		LOGGER.info("com.lamfire.nkit.sample.ServiceServer [host] [port]");
	}
	
	public static void main(String[] args) {
		String host = "127.0.0.1";
		int port = 8000;
		
		if(args.length > 0 && "?".equals(args[0])){
			usage();
			return;
		}
		
		if(args.length == 2){
			host = args[0];
			port = Integer.valueOf(args[1]);
		}
		
		LOGGER.info("[startup]:"+host + ":" +port);
		ReqresServer server = new ReqresServer(host,port);
		server.setKeepAlive(false);
		server.bind();
	}

	@Override
	protected void handleMessage(MessageContext context, Message message) {
		int messageId = message.getId();
		LOGGER.info("[REVIEVE] message:"+messageId);
		context.send(messageId, message.getBody());
	}
}
