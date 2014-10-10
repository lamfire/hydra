package com.lamfire.hydra;

public class SessionException extends RuntimeException{

	private static final long serialVersionUID = -2779164453456L;


	public SessionException(){}
	
	public SessionException(String message){
		super(message);
	}
	
	public SessionException(Throwable e){
		super(e);
	}
	
	public SessionException(String message,Throwable e){
		super(message,e);
	}
	
}
