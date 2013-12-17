package com.lamfire.hydra.sample.reqres;

import java.util.concurrent.atomic.AtomicInteger;

import com.lamfire.hydra.Snake;
import com.lamfire.logger.Logger;
import com.lamfire.hydra.CycleSessionIterator;
import com.lamfire.hydra.Message;
import com.lamfire.hydra.MessageContext;
import com.lamfire.hydra.net.Context;
import com.lamfire.hydra.net.Session;

public class ReqresCli extends Snake {
	private static final Logger LOGGER = Logger.getLogger(ReqresCli.class);
	private  ResponseFutureQueue queue = new ResponseFutureQueue();
	private CycleSessionIterator it = new CycleSessionIterator(this);
	private AtomicInteger atimic = new AtomicInteger();
	
	public ReqresCli(String host, int port) {
		super(host, port);
	}

	static void usage(){
		System.out.println("com.lamfire.nkit.sample.ClientSample [host] [port] [connsize]");
	}
	
	public ResponseFuture sendMessage(byte[] bytes){
		int id = atimic.getAndIncrement();
		Session session = it.nextAvailableSession();
		ResponseFuture future = new ResponseFuture(id);
		queue.addFuture(future);
		session.send(new Message(id,bytes));
		return future;
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
		ReqresCli cli = new ReqresCli(host,port);
		cli.setKeepAlive(true);
		cli.connect();

		for(int i=0;i<10000000;i++){
			ResponseFuture future = cli.sendMessage(String.valueOf(i).getBytes());
			System.out.println(new String(future.getResponse()));
		}
	}

	@Override
	protected void handleMessage(MessageContext context, Message message) {
		ResponseFuture future = queue.remove(message.getId());
		future.onResponse(message);
	}

	@Override
	public void onExceptionCaught(Context context, Session session, Throwable throwable) {
		LOGGER.error(throwable.getMessage(),throwable);
	}

	
	
	
}
