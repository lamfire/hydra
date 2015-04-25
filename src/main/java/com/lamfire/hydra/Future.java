package com.lamfire.hydra;

import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.ChannelFuture;


public class Future {

	private ChannelFuture channelFuture;
	private Session session;
	
	public Future(Session session,ChannelFuture channelFuture){
		this.session = session;
		this.channelFuture = channelFuture;
	}
	
	public void addFutureListener(FutureListener listener){
		listener.setSession(session);
		channelFuture.addListener(listener);
	}
	
	public void removeFutureListener(FutureListener listener){
		channelFuture.removeListener(listener);
	}

	public Session getSession() {
		return this.session;
	}


	public boolean isDone() {
		return channelFuture.isDone();
	}


	public boolean isCancelled() {
		return channelFuture.isCancelled();
	}


	public boolean isSuccess() {
		return channelFuture.isCancelled();
	}


	public Throwable getCause() {
		return channelFuture.getCause();
	}


	public boolean cancel() {
		return channelFuture.cancel();
	}
	
	public void sync() throws InterruptedException{
		channelFuture.sync();
	}
	
	public void syncUninterruptibly(){
		channelFuture.syncUninterruptibly();
	}

	public ChannelFuture await() throws InterruptedException {
		return channelFuture.await();
	}

	public ChannelFuture awaitUninterruptibly() {
		return channelFuture.awaitUninterruptibly();
	}

	public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
		return channelFuture.await(timeout, unit);
	}

	public boolean await(long timeoutMillis) throws InterruptedException {
		return channelFuture.await(timeoutMillis);
	}

	public boolean awaitUninterruptibly(long timeout, TimeUnit unit) {
		return channelFuture.awaitUninterruptibly(timeout, unit);
	}

	public boolean awaitUninterruptibly(long timeoutMillis) {
		return channelFuture.awaitUninterruptibly(timeoutMillis);
	}
}
