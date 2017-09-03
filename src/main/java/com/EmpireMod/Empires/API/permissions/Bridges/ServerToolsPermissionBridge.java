package com.EmpireMod.Empires.API.permissions.Bridges;

import java.lang.reflect.Method;
import java.util.UUID;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.EmpireMod.Empires.Empires;
import com.EmpireMod.Empires.API.permissions.IPermissionBridge;

public class ServerToolsPermissionBridge implements IPermissionBridge {

	private Method serverToolsManagerMethod;

	public ServerToolsPermissionBridge() {
		try {
			Class<?> serverToolsManagerClass = Class.forName("info.servertools.permission.PermissionManager");
			serverToolsManagerMethod = serverToolsManagerClass.getMethod("checkPerm", String.class, UUID.class);
		} catch (Exception e) {
			throw new RuntimeException("Failed to find ServerTools-PERMISSION class!", e);
		}
	}

	@Override
	public boolean hasPermission(UUID uuid, String permission) {
		boolean result;

		// MyPermissions.instance.LOG.error("Testing permission: " +
		// permission);
		try {
			result = (Boolean) serverToolsManagerMethod.invoke(null, permission, uuid);
		} catch (Exception ex) {
			Empires.instance.LOG.error("Error ocurred when trying to check permission!");
			Empires.instance.LOG.error(ExceptionUtils.getStackTrace(ex));
			return false;
		}

		// Check for mods that don't implement node.* entries
		if (!result) {
			String lastNode = "";
			String[] nodes = permission.split("\\.");
			for (int i = 0; i < nodes.length - 1; i++) {
				lastNode = lastNode + nodes[i] + ".";
				// MyPermissions.instance.LOG.error("Testing permission: " +
				// lastNode + "*");
				try {
					if ((Boolean) serverToolsManagerMethod.invoke(null, lastNode + "*", uuid)) {
						result = true;
					}
				} catch (Exception ex) {
					Empires.instance.LOG.error("Error ocurred when trying to check permission!");
					Empires.instance.LOG.error(ExceptionUtils.getStackTrace(ex));
					return false;
				}
			}
		}

		return result;
	}
}