package com.EmpireMod.Empires.exceptions.Permission;

public class PermissionException extends RuntimeException {

	public PermissionException(String message) {
		super(message);
	}

	public PermissionException(String message, Throwable cause) {
		super(message);
		initCause(cause);
	}
}