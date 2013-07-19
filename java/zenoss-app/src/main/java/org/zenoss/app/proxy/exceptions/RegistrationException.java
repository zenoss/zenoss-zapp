package org.zenoss.app.proxy.exceptions;

public class RegistrationException extends RuntimeException {
	private static final long serialVersionUID = 6779603009112566225L;

	public RegistrationException(String message) {
		super(message);
	}
	
	public RegistrationException(Throwable e) {
		super(e);
	}
	
	public RegistrationException(String message, Throwable cause) {
		super(message, cause);
	}

}
