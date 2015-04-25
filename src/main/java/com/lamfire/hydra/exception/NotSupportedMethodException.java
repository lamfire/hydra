package com.lamfire.hydra.exception;

public class NotSupportedMethodException extends RuntimeException {

	private static final long serialVersionUID = 5659852383725177288L;

	public NotSupportedMethodException() {
	}

	public NotSupportedMethodException(String message) {
		super(message);
	}

	public NotSupportedMethodException(Throwable e) {
		super(e);
	}

	public NotSupportedMethodException(String message, Throwable e) {
		super(message, e);
	}
}
