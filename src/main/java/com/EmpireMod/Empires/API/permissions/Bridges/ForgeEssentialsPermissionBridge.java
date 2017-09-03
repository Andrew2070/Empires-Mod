package com.EmpireMod.Empires.API.permissions.Bridges;

import java.util.UUID;

import com.EmpireMod.Empires.API.permissions.IPermissionBridge;
import com.EmpireMod.Empires.API.permissions.PermissionManager;
import com.EmpireMod.Empires.Utilities.PlayerUtils;

import net.minecraft.entity.player.EntityPlayer;

public class ForgeEssentialsPermissionBridge implements IPermissionBridge {

	@Override
	public boolean hasPermission(UUID uuid, String permission) {
		EntityPlayer player = PlayerUtils.getPlayerFromUUID(uuid);
		return PermissionManager.checkPermission(player, permission);
	}
}