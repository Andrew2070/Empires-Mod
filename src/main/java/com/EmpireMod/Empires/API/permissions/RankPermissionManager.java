package com.EmpireMod.Empires.API.permissions;



import net.minecraft.entity.player.EntityPlayer;

import java.util.UUID;
import com.EmpireMod.Empires.entities.Permissions.PermissionLevel;
import com.EmpireMod.Empires.API.commands.CommandsEMP;
import com.EmpireMod.Empires.Datasource.EmpiresUniverse;
import com.EmpireMod.Empires.entities.Empire.Citizen;
import com.EmpireMod.Empires.entities.Empire.Empire;
import com.EmpireMod.Empires.exceptions.EmpiresCommandException;
import com.EmpireMod.Empires.utils.PlayerUtils;

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