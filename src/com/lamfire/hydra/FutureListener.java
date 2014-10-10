package com.lamfire.hydra;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;


public abstract class FutureListener implements ChannelFutureListener{

	private Session session;
	
	public FutureListener(){}

	protected void setSession(Session session){
		this.session = session;
	}
	
	public void operationComplete(ChannelFuture arg) throws Exception {
		onComplate(session);
	}
	
	public abstract void onComplate(Session session);

}
