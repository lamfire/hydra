package com.lamfire.hydra.sample.gateway;

import java.lang.management.ManagementFactory;

import com.lamfire.hydra.*;
import com.lamfire.hydra.Message;
import com.lamfire.logger.Logger;
import com.lamfire.hydra.Snake;
import com.lamfire.hydra.Session;

public class Client {
	private static final Logger LOGGER = Logger.getLogger(Client.class);
	
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
        executor.setAutoConnectRetry(true);
        executor.setKeepAlive(true);
		//connect
        executor.connect();
		for(int i=0;i<size;i++){
			send(executor, 1001, ManagementFactory.getRuntimeMXBean().getName().getBytes());
			//}
		}
	}

    static void send(Snake snake,int id,byte[] bytes){
        try{
            Session session = snake.awaitAvailableSession();
            session.send(new Message(id,bytes));
        }catch (Exception e){
            e.printStackTrace();
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
