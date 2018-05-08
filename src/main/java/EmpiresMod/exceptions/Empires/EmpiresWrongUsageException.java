package EmpiresMod.exceptions.Empires;

import EmpiresMod.Empires;
import net.minecraft.command.WrongUsageException;

public class EmpiresWrongUsageException extends WrongUsageException {
	public EmpiresWrongUsageException(String key, Object... args) {
		super(Empires.instance.LOCAL.getLocalization(key, args).getUnformattedText());
	}
}