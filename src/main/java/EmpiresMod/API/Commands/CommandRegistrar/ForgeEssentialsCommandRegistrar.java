package EmpiresMod.API.Commands.CommandRegistrar;

import net.minecraft.command.ICommand;

/**
 * ForgeEssentials command registrar.
 */
public class ForgeEssentialsCommandRegistrar extends VanillaCommandRegistrar {
	@Override
	public void registerCommand(ICommand cmd, String permNode, boolean defaultPerm) {
		// PermissionManager.registerPermission(permNode,
		// PermissionLevel.fromBoolean(defaultPerm));
		super.registerCommand(cmd, permNode, defaultPerm);
	}
}