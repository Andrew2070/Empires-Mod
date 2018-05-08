package EmpiresMod.exceptions.Format;

import EmpiresMod.Localization.LocalizationManager;
import net.minecraft.util.IChatComponent;

public abstract class FormattedException extends RuntimeException {

	public final IChatComponent message;

	public FormattedException(String localizationKey, Object... args) {
		message = LocalizationManager.get(localizationKey, args);
	}
}