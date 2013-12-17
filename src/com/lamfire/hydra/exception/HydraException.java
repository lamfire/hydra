package com.lamfire.hydra.exception;

public class HydraException extends RuntimeException {

	private static final long serialVersionUID = 56598523345343L;

	public HydraException() {
	}

	public HydraException(String message) {
		super(message);
	}

	public HydraException(Throwable e) {
		super(e.getMessage(),e);
	}

	public HydraException(String message, Throwable e) {
		super(message, e);
	}
}
