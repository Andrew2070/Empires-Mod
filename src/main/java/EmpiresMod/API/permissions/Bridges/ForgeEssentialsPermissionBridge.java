package EmpiresMod.API.permissions.Bridges;

import java.util.UUID;

import EmpiresMod.API.permissions.IPermissionBridge;
import EmpiresMod.API.permissions.PermissionManager;
import EmpiresMod.Utilities.PlayerUtils;
import net.minecraft.entity.player.EntityPlayer;

public class ForgeEssentialsPermissionBridge implements IPermissionBridge {

	@Override
	public boolean hasPermission(UUID uuid, String permission) {
		EntityPlayer player = PlayerUtils.getPlayerFromUUID(uuid);
		return PermissionManager.checkPermission(player, permission);
	}
}