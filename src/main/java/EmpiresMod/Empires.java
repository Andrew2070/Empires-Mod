package EmpiresMod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;

import EmpiresMod.API.Chat.BukkitChatCompat;
import EmpiresMod.API.Chat.EmpireChatChannel;
import EmpiresMod.API.Chat.ForgeChatHandler;
import EmpiresMod.API.Commands.Command.CommandManager;
import EmpiresMod.API.Commands.Command.CommandsEMP;
import EmpiresMod.API.JSON.Configuration.FlagsConfig;
import EmpiresMod.API.JSON.Configuration.JsonConfig;
import EmpiresMod.API.JSON.Configuration.RanksConfig;
import EmpiresMod.API.JSON.Configuration.RelationshipsConfig;
import EmpiresMod.API.JSON.Configuration.WildPermsConfig;
import EmpiresMod.API.permissions.PermissionManager;
import EmpiresMod.API.permissions.PermissionProxy;
import EmpiresMod.API.permissions.RankPermissionManager;
import EmpiresMod.API.permissions.Bridges.EmpiresBridge;
import EmpiresMod.Configuration.Config;
import EmpiresMod.Datasource.DatasourceCrashCallable;
import EmpiresMod.Datasource.EmpiresDatasource;
import EmpiresMod.Handlers.EmpiresLoadingCallback;
import EmpiresMod.Handlers.ExtraEventsHandler;
import EmpiresMod.Handlers.PlayerTracker;
import EmpiresMod.Handlers.SafemodeHandler;
import EmpiresMod.Handlers.Ticker;
import EmpiresMod.Handlers.VisualsHandler;
import EmpiresMod.Localization.Localization;
import EmpiresMod.Localization.LocalizationManager;
import EmpiresMod.Proxies.EconomyProxy;
import EmpiresMod.Utilities.ClassUtils;
import EmpiresMod.Utilities.StringUtils;
import EmpiresMod.commands.Admin.CommandsAdmin;
import EmpiresMod.commands.Neutral.CommandsNeutral;
import EmpiresMod.commands.Officer.CommandsOfficer;
import EmpiresMod.commands.Permission.PermCommands;
import EmpiresMod.commands.Recruit.CommandsRecruit;
import EmpiresMod.entities.Managers.SignManager;
import EmpiresMod.entities.Managers.ToolManager;
import EmpiresMod.entities.Signs.SellSign;
import EmpiresMod.exceptions.Configuration.ConfigException;
import EmpiresMod.protection.ProtectionHandlers;
import EmpiresMod.protection.ProtectionManager;
import EmpiresMod.protection.JSON.ProtectionParser;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = Constants.MODID, name = Constants.MODNAME, version = Constants.VERSION, dependencies = Constants.DEPENDENCIES, acceptableRemoteVersions = "*")

public class Empires {
	// @Instance
	@Mod.Instance(Constants.MODID)

	public static Empires instance;
	public Localization LOCAL;
	public Logger LOG;
	public EmpiresDatasource datasource;
	private final List<JsonConfig> jsonConfigs = new ArrayList<JsonConfig>();

	@EventHandler
	public void preInit(FMLPreInitializationEvent ev) {
		// ITEMS & BLOCK INIT + REGISTERING STUFF
		MinecraftServer server = MinecraftServer.getServer();
		LOG = ev.getModLog();

		Constants.CONFIG_FOLDER = ev.getModConfigurationDirectory().getPath() + "/Empires Mod/";
		Constants.DATABASE_FOLDER = ev.getModConfigurationDirectory().getParent() + "/databases/";
		// Load Configs
		
		String pexFilePath = "/permissions.yml";

		Config.instance.init(Constants.CONFIG_FOLDER + "/Empires.cfg", "Empires Mod");

		// REF: The localization can simply take the whole config instance to
		// get the localization needed.

		LOCAL = new Localization(Constants.CONFIG_FOLDER + "/Localization/", Config.instance.localization.get(),
				"/Empires/Localization/", Empires.class);
		LocalizationManager.register(LOCAL, "Empires");

		// Register handlers/trackers
		FMLCommonHandler.instance().bus().register(PlayerTracker.instance);
		MinecraftForge.EVENT_BUS.register(PlayerTracker.instance);

	    FMLCommonHandler.instance().bus().register(BukkitChatCompat.instance);
		MinecraftForge.EVENT_BUS.register(BukkitChatCompat.instance);

		FMLCommonHandler.instance().bus().register(ForgeChatHandler.instance);
		MinecraftForge.EVENT_BUS.register(ForgeChatHandler.instance);
		
		FMLCommonHandler.instance().bus().register(EmpireChatChannel.instance);
		MinecraftForge.EVENT_BUS.register(EmpireChatChannel.instance);

		FMLCommonHandler.instance().bus().register(Ticker.instance);
		MinecraftForge.EVENT_BUS.register(Ticker.instance);

		FMLCommonHandler.instance().bus().register(ToolManager.instance);
		MinecraftForge.EVENT_BUS.register(ToolManager.instance);

		FMLCommonHandler.instance().bus().register(SignManager.instance);
		MinecraftForge.EVENT_BUS.register(SignManager.instance);

		registerHandlers();

		FMLCommonHandler.instance().registerCrashCallable(new DatasourceCrashCallable());

	}

	public void loadConfig() {
		Config.instance.reload();
		PermissionProxy.init();
		LOCAL.load();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// Proxy, TileEntity, entity, GUI, and Packet Register
		System.out.println("Empires Mod: By Andrew2070");
		System.out.println("Empires Mod: Now Initializing...");
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		// Put hooks for other Mods here
		SellSign.SellSignType.instance.register();
		System.out.println("Empires Mod: Initialization Finished.");

	}

	@EventHandler
	public void serverLoad(FMLServerStartingEvent ev) {
		// long StartTime = System.currentTimeMillis();
		EconomyProxy.init();
		checkConfig();
		registerCommands();
		CommandsEMP.populateCompletionMap();
		System.out.println("Empires Mod: Server Detected, Registering Commands...");
		loadConfig();
		CommandManager.registerCommands(PermCommands.class, null, Empires.instance.LOCAL, null);
		if (PermissionProxy.getPermissionManager() instanceof EmpiresBridge) {
			CommandManager.registerCommands(PermissionManager.class, "Empires.cmd", Empires.instance.LOCAL, null);

		}
		jsonConfigs.add(new WildPermsConfig(Constants.CONFIG_FOLDER + "/JSON/WildPerms.json"));
		jsonConfigs.add(new FlagsConfig(Constants.CONFIG_FOLDER + "/JSON/DefaultFlags.json"));
		jsonConfigs.add(new RanksConfig(Constants.CONFIG_FOLDER + "/JSON/DefaultEmpireRanks.json"));
		jsonConfigs.add(new RelationshipsConfig(Constants.CONFIG_FOLDER + "/JSON/DefaultEmpireRelationships.json"));
		
		for (JsonConfig jsonConfig : jsonConfigs) {
			jsonConfig.init();
		}

		ProtectionParser.start();
		datasource = new EmpiresDatasource();
		LOG.info("Started");

	}

	@Mod.EventHandler
	public void serverStopping(FMLServerStoppingEvent ev) {
		datasource.deleteAllBlockOwners();
		ProtectionManager.saveBlockOwnersToDB();
		datasource.stop();

	}

	private void registerCommands() {
		RankPermissionManager bridge = new RankPermissionManager();
		CommandManager.registerCommands(CommandsRecruit.class, null, LOCAL, bridge);
		CommandManager.registerCommands(CommandsOfficer.class, "Empires.cmd", LOCAL, bridge);
		if (Config.instance.modifiableRanks.get())
			CommandManager.registerCommands(CommandsOfficer.ModifyRanks.class, "Empires.cmd", LOCAL, bridge);
		CommandManager.registerCommands(CommandsAdmin.class, null, LOCAL, null);
		if (Config.instance.enablePlots.get()) {
			CommandManager.registerCommands(CommandsRecruit.Plots.class, "Empires.cmd", LOCAL, null);
			CommandManager.registerCommands(CommandsOfficer.Plots.class, "Empires.cmd", LOCAL, bridge);
			CommandManager.registerCommands(CommandsAdmin.Plots.class, "Empires.adm.cmd", LOCAL, null);
		}
		CommandManager.registerCommands(CommandsNeutral.class, "Empires.cmd", LOCAL, null);
	}

	public WildPermsConfig getWildConfig() {
		for (JsonConfig jsonConfig : jsonConfigs) {
			if (jsonConfig instanceof WildPermsConfig)
				return (WildPermsConfig) jsonConfig;
		}
		return null;
	}

	public RanksConfig getRanksConfig() {
		for (JsonConfig jsonConfig : jsonConfigs) {
			if (jsonConfig instanceof RanksConfig)
				return (RanksConfig) jsonConfig;
		}
		return null;
	}

	public FlagsConfig getFlagsConfig() {
		for (JsonConfig jsonConfig : jsonConfigs) {
			if (jsonConfig instanceof FlagsConfig)
				return (FlagsConfig) jsonConfig;
		}
		return null;
	}

	private void registerHandlers() {

		FMLCommonHandler.instance().bus().register(new SafemodeHandler());
		Ticker playerTracker = new Ticker();

		FMLCommonHandler.instance().bus().register(playerTracker);
		MinecraftForge.EVENT_BUS.register(playerTracker);

		FMLCommonHandler.instance().bus().register(VisualsHandler.instance);

		FMLCommonHandler.instance().bus().register(ProtectionHandlers.instance);
		MinecraftForge.EVENT_BUS.register(ProtectionHandlers.instance);

		MinecraftForge.EVENT_BUS.register(ExtraEventsHandler.getInstance());

		ForgeChunkManager.setForcedChunkLoadingCallback(this, new EmpiresLoadingCallback());
	}

	public void loadConfigs() {
		Config.instance.reload();

		EconomyProxy.init();
		checkConfig();

		LOCAL.load();

		for (JsonConfig jsonConfig : jsonConfigs) {
			jsonConfig.init();
		}

		ProtectionParser.start();
	}

	private void checkConfig() {
		// Checking cost item
		if (EconomyProxy.isItemEconomy()) {
			String[] split = Config.instance.costItemName.get().split(":");
			if (split.length < 2 || split.length > 3) {
				throw new ConfigException(
						"Field costItem has an invalid value. Template: (modid):(unique_name)[:meta]. Use \"minecraft\" as modid for vanilla items/blocks.");
			}

			if (GameRegistry.findItem(split[0], split[1]) == null) {
				throw new ConfigException(
						"Field costItem has an invalid modid or unique name of the item. Template: (modid):(unique_name)[:meta]. Use \"minecraft\" as modid for vanilla items/blocks.");
			}

			if (split.length > 2 && (!StringUtils.tryParseInt(split[2]) || Integer.parseInt(split[2]) < 0)) {
				throw new ConfigException(
						"Field costItem has an invalid metadata. Template: (modid):(unique_name)[:meta]. Use \"minecraft\" as modid for vanilla items/blocks.");
			}
		}
	}

}
