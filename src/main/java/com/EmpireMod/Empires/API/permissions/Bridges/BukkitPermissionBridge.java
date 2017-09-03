package com.EmpireMod.Empires.API.permissions.Bridges;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.EmpireMod.Empires.API.permissions.IPermissionBridge;

public class BukkitPermissionBridge implements IPermissionBridge {

	@Override
	public boolean hasPermission(UUID uuid, String permission) {
		OfflinePlayer player = Bukkit.getPlayer(uuid);
		if (player == null || player.getPlayer() == null) {
			// MyPermissions.instance.LOG.error("Failed to get player with the
			// UUID: " + uuid.toString());
			return false;
		}

		// MyPermissions.instance.LOG.error("Testing permission: " +
		// permission);
		boolean result = player.getPlayer().hasPermission(permission);

		// Check for mods that don't implement node.* entries
		if (!result) {
			String lastNode = "";
			String[] nodes = permission.split("\\.");
			for (int i = 0; i < nodes.length - 1; i++) {
				lastNode = lastNode + nodes[i] + ".";
				// MyPermissions.instance.LOG.error("Testing permission: " +
				// lastNode + "*");
				if (player.getPlayer().hasPermission(lastNode + "*")) {
					result = true;
				}
			}
		}

		return result;
	}
}