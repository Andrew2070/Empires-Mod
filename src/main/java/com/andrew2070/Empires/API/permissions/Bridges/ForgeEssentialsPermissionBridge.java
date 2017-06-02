package com.andrew2070.Empires.API.permissions.Bridges;

import com.andrew2070.Empires.utils.PlayerUtils;
import net.minecraft.entity.player.EntityPlayer;
import com.andrew2070.Empires.API.permissions.PermissionManager;

import java.util.UUID;

import com.andrew2070.Empires.API.permissions.IPermissionBridge;

public class ForgeEssentialsPermissionBridge implements IPermissionBridge {

    @Override
    public boolean hasPermission(UUID uuid, String permission) {
        EntityPlayer player = PlayerUtils.getPlayerFromUUID(uuid);
        return PermissionManager.checkPermission(player, permission);
    }
}