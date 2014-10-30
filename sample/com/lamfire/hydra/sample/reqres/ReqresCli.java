package com.lamfire.hydra.sample.reqres;

import com.lamfire.hydra.exception.HydraException;
import com.lamfire.hydra.reply.ReplySnake;
import com.lamfire.logger.Logger;
import com.lamfire.utils.Threads;

public class ReqresCli {
	private static final Logger LOGGER = Logger.getLogger(ReqresCli.class);


	static void usage(){
		System.out.println(ReqresCli.class.getName() +" [host] [port] [connsize]");
	}

    static void send(ReplySnake snake,byte[] bytes){
        try{
            byte[] result = snake.send(bytes);
            System.out.println(new String(result));
        }catch (HydraException e){
            e.printStackTrace();
            Threads.sleep(5000);
        }
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
		
		LOGGER.info("[startup]:"+host + ":" +port  +" - "+ size);
		ReplySnake snake = new ReplySnake(host,port);
        snake.setKeepAlive(true);
        snake.setKeepaliveConnsWithClient(1);
        snake.setAutoConnectRetry(true);
        snake.setReadTimeoutMills(5000);
        snake.connect();

		for(int i=0;i<10000000;i++){
			send(snake,String.valueOf(i).getBytes());
		}
	}

	
}
