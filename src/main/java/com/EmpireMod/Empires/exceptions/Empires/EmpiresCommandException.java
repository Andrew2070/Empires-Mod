package com.EmpireMod.Empires.exceptions.Empires;

import com.EmpireMod.Empires.exceptions.Command.CommandException;

public class EmpiresCommandException extends CommandException {
	public EmpiresCommandException(String key, Object... args) {
		super(key, args);
	}

	public EmpiresCommandException(String key, Throwable cause, Object... args) {
		this(key, args);
		initCause(cause);
	}
}