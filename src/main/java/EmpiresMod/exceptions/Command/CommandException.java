package EmpiresMod.exceptions.Command;

import EmpiresMod.exceptions.Format.FormattedException;

public class CommandException extends FormattedException {

	public CommandException(String localizationKey, Object... args) {
		super(localizationKey, args);
	}
}