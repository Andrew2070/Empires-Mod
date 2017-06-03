package com.EmpireMod.Empires.API.permissions.Bridges;

import com.EmpireMod.Empires.utils.PlayerUtils;
import net.minecraft.entity.player.EntityPlayer;
import com.EmpireMod.Empires.API.permissions.PermissionManager;

import java.util.UUID;

import com.EmpireMod.Empires.API.permissions.IPermissionBridge;

public class ForgeEssentialsPermissionBridge implements IPermissionBridge {

    @Override
    public boolean hasPermission(UUID uuid, String permission) {
        EntityPlayer player = PlayerUtils.getPlayerFromUUID(uuid);
        return PermissionManager.checkPermission(player, permission);
    }
}