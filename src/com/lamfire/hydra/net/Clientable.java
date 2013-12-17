package com.lamfire.hydra.net;

public interface Clientable {
	public Session connect();
	public void shutdown() ;
}
