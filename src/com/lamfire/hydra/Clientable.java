package com.lamfire.hydra;

public interface Clientable {
	public Session connect();
	public void shutdown() ;
}
