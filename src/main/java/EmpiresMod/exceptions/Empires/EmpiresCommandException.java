package EmpiresMod.exceptions.Empires;

import EmpiresMod.exceptions.Command.CommandException;

public class EmpiresCommandException extends CommandException {
	public EmpiresCommandException(String key, Object... args) {
		super(key, args);
	}

	public EmpiresCommandException(String key, Throwable cause, Object... args) {
		this(key, args);
		initCause(cause);
	}
}