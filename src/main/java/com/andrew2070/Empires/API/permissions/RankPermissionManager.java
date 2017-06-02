package com.andrew2070.Empires.API.permissions;



import net.minecraft.entity.player.EntityPlayer;

import java.util.UUID;
import com.andrew2070.Empires.entities.Permissions.PermissionLevel;
import com.andrew2070.Empires.API.commands.CommandsEMP;
import com.andrew2070.Empires.Datasource.EmpiresUniverse;
import com.andrew2070.Empires.entities.Empire.Citizen;
import com.andrew2070.Empires.entities.Empire.Empire;
import com.andrew2070.Empires.exceptions.EmpiresCommandException;
import com.andrew2070.Empires.utils.PlayerUtils;

public class RankPermissionManager implements IPermissionBridge {

    @Override
    public boolean hasPermission(UUID uuid, String permission) {
        if(permission.startsWith("Empires.cmd.outsider") || permission.equals("Empires.cmd"))
            return true;

        EntityPlayer player = PlayerUtils.getPlayerFromUUID(uuid);
        Citizen citizen = EmpiresUniverse.instance.getOrMakeCitizen(player);
        Empire empire = CommandsEMP.getEmpireFromCitizen(citizen);
        if(empire.citizensMap.get(citizen).permissionsContainer.hasPermission(permission) != PermissionLevel.ALLOWED) {
            throw new EmpiresCommandException("Empires.cmd.err.rankPerm");
        }
        return true;
    }
}