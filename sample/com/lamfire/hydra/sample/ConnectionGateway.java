package com.lamfire.hydra.sample;

import com.lamfire.hydra.*;
import com.lamfire.hydra.HydraDestination;
import com.lamfire.utils.StringUtils;

public class ConnectionGateway {

	static void usage(){
		System.out.println("com.lamfire.nkit.sample.ConnectionGateway [host] [port] [bind|connect]");
	}
	
	public static void main(String[] args) {
		String host = "0.0.0.0";
		int port = 8000;
		String action = "bind";
		
		if(args.length > 0 && "?".equals(args[0])){
			usage();
			return;
		}
		
		if(args.length == 3){
			host = args[0];
			port = Integer.valueOf(args[1]);
			action = (args[2]);
		}
		
		Gateway gateway = new Gateway(host,port);
		gateway.setHearbeatIntervalTime(30);
		gateway.setMaxWaitWithHeartbeat(3);
		
		if(StringUtils.equalsIgnoreCase(action, "connect")){
			gateway.connect();
		}else{
			gateway.bind();
		}
		
		HydraDestination dest = new PollerDestination("0.0.0.0", 8001);
		dest.bind();

		MessageBus bus = new DefaultMessageBus(gateway);
		bus.addDestination(dest);
	}
}
