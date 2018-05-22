package EmpiresMod.API.permissions;

import EmpiresMod.Empires;
import EmpiresMod.API.permissions.Bridges.BukkitPermissionBridge;
import EmpiresMod.API.permissions.Bridges.ForgeEssentialsPermissionBridge;
import EmpiresMod.API.permissions.Bridges.EmpiresBridge;
import EmpiresMod.API.permissions.Bridges.ServerToolsPermissionBridge;
import EmpiresMod.Configuration.Config;
import EmpiresMod.Utilities.ClassUtils;
import EmpiresMod.exceptions.Permission.PermissionException;
import cpw.mods.fml.common.Loader;

public class PermissionProxy {
	public static final String PERM_SYSTEM_BUKKIT = "$Bukkit";
	public static final String PERM_SYSTEM_FORGE_ESSENTIALS = "$ForgeEssentials";
	public static final String PERM_SYSTEM_EMPIRES = "$Empires";
	public static final String PERM_SYSTEM_SERVER_TOOLS = "$ServerTools";

	private static IPermissionBridge permissionManager;

	public static IPermissionBridge getPermissionManager() {
		if (permissionManager == null) {
			init();
		}
		return permissionManager;
	}

	public static void init() {
		if (Config.instance.permissionSystem.get().equals(PERM_SYSTEM_BUKKIT)) {
			if (!ClassUtils.isBukkitLoaded()) {
				throw new PermissionException("Failed to find Bukkit permission system.");
			}
			permissionManager = new BukkitPermissionBridge();
			Empires.instance.LOG.info("Successfully linked to Bukkit's permission system");
		} else if (Config.instance.permissionSystem.get().equals(PERM_SYSTEM_FORGE_ESSENTIALS)) {
			if (!Loader.isModLoaded("ForgeEssentials")) {
				throw new PermissionException("Failed to find ForgeEssentials permission system.");
			}
			permissionManager = new ForgeEssentialsPermissionBridge();
			Empires.instance.LOG.info("Successfully linked to ForgeEssentials' permission system");
		} else if (Config.instance.permissionSystem.get().equals(PERM_SYSTEM_SERVER_TOOLS)) {
			if (!Loader.isModLoaded("ServerTools-PERMISSION")) {
				throw new PermissionException("Failed to find ServerTools' permission system.");
			}
			permissionManager = new ServerToolsPermissionBridge();
			Empires.instance.LOG.info("Successfully linked to ServerTools' permission system");
		} else {
			permissionManager = new EmpiresBridge();
			((EmpiresBridge) permissionManager).loadConfigs();
			Empires.instance.LOG.info("Currently using built-in permission system.");
			Empires.instance.LOG.info("This is not fully functional and only works for mods that use this API.");
			Empires.instance.LOG.info("If you have Bukkit or ForgeEssentials installed please use that instead.");
		}
	}
}