package EmpiresMod.API.permissions;

import java.util.UUID;

import EmpiresMod.API.Commands.Command.CommandsEMP;
import EmpiresMod.Datasource.EmpiresUniverse;
import EmpiresMod.Utilities.PlayerUtils;
import EmpiresMod.entities.Empire.Citizen;
import EmpiresMod.entities.Empire.Empire;
import EmpiresMod.entities.Permissions.PermissionLevel;
import EmpiresMod.exceptions.Empires.EmpiresCommandException;
import net.minecraft.entity.player.EntityPlayer;

public class RankPermissionManager implements IPermissionBridge {

	@Override
	public boolean hasPermission(UUID uuid, String permission) {
		if (permission.startsWith("Empires.cmd.outsider") || permission.equals("Empires.cmd"))
			return true;

		EntityPlayer player = PlayerUtils.getPlayerFromUUID(uuid);
		Citizen citizen = EmpiresUniverse.instance.getOrMakeCitizen(player);
		Empire empire = CommandsEMP.getEmpireFromCitizen(citizen);
		if (empire.citizensMap.get(citizen).permissionsContainer.hasPermission(permission) != PermissionLevel.ALLOWED) {
			throw new EmpiresCommandException("Empires.cmd.err.rankPerm");
		}
		return true;
	}
}