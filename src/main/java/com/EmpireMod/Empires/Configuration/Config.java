package com.EmpireMod.Empires.Configuration;


import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Items;


public class Config extends ConfigTemplate {

    public static final Config instance = new Config();
    

    public ConfigProperty<Boolean> maintenanceMode = new ConfigProperty<Boolean>(
            "maintenanceMode", "general",
            "Allows toggling server maintenance mode for in-game town removals etc. (similar to whitelist).",
            false);

    public ConfigProperty<String> maintenanceModeMessage = new ConfigProperty<String>(
            "maintenanceModeMessage", "general",
            "The menu custom message to display when the server is in maintenance mode",
            "Server Under Maintenance, [ETA: 5 minutes].");

    public ConfigProperty<String> permissionSystem = new ConfigProperty<String>(
            "permissionSystem", "general",
            "The permission system it should be used as default. $ForgeEssentials for FE permission system, $Bukkit for Bukkit permission system, and $Empires for our own permission system.",
            "$Empires Permissions");

    public ConfigProperty<String> localization = new ConfigProperty<String>(
            "localization", "general",
            "The localization file used, currently we only support English.",
            "en_US");

    public ConfigProperty<Boolean> fullAccessForOPS = new ConfigProperty<Boolean>(
            "fullAccessForOPS", "permissions",
            "Players that have operator level permissions will have access to any command.",
            true);

    public ConfigProperty<String> defaultGroup = new ConfigProperty<String>(
            "defaultGroup", "groups",
            "The default permission group assigned to any player that does not have one assigned yet.",
            "default");
    
    public ConfigProperty<String> safeModeMsg = new ConfigProperty<String>(
            "safeModeMessage", "general",
            "Message to display to users when Empires is in safemode.",
            "Empires Mod has been triggered into safe-mode. Please inform a server admin to disable it!");
    
    /* ----- Datasource Config ----- */

    public ConfigProperty<String> dbType = new ConfigProperty<String>(
            "type", "datasource", "Datasource Type. Eg: MySQL, SQLite, etc.",
            "SQLite");


    public ConfigProperty<Integer> distanceBetweenEmpires = new ConfigProperty<Integer>(
            "distance", "Empires",
            "The Default Minimum distance (in chunks) between 2 Empires. Prevents Empire's from being too close on creation.",
            1);
    
    
    public ConfigProperty<Integer> blocksLeader = new ConfigProperty<Integer>(
            "blocksLeader", "Empires",
            "The amount of maximum claims a Empire gets from the leader, this should match the defaultMaxPower statement.",
            20);
    
    
    public ConfigProperty<Integer> blocksCitizen = new ConfigProperty<Integer>(
            "blocksCitizens", "Empires",
            "The amount of maximum claims a Empire gets from each player, set this value to be equal to the defaultMaxPower statement.",
            20);
    
    
    public ConfigProperty<Integer> placeProtectionRange = new ConfigProperty<Integer>(
            "placeProtectionRange", "Empires",
            "The distance in blocks from a protected Empire where you can't place a block in the Wild.",
            4);
    
    
    public ConfigProperty<Boolean> modifiableRanks = new ConfigProperty<Boolean>(
            "modifiableRanks", "Empires",
            "If true Citizens with permission can modify the ranks of their Empires. This feature hasn't been fully tested yet and it might cause problems!",
            true);
    
    
    public ConfigProperty<Integer> maxFarClaims = new ConfigProperty<Integer>(
            "maxFarClaims", "Empires",
            "The maximum amount of chunks not attached to any other claimed chunk that are allowed per Empire. Set to 0 to disable far claims altogether.",
            10);
    
    
    public ConfigProperty<Double> defaultMaxPower = new ConfigProperty<Double>(
            "defaultMaxPower", "Citizens",
            "The default maximum amount of power are allowed per player, be vary that claim blocks should equal the number to the nearest whole number.",
            20.00);
    
    public ConfigProperty<Double> defaultPower = new ConfigProperty<Double>(
            "defaultPowers", "Citizens",
            "The default amount of power each player starts with (Normally 1.00) (To Defend Initial Claim):",
            0.00);
    
  
    public ConfigProperty<Double> minPower = new ConfigProperty<Double>(
            "minPower", "Citizens",
            "The default minimum amount of power are allowed per player.",
            -20.00);
    
    
    public ConfigProperty<Double> PowerPerHour = new ConfigProperty<Double>(
            "PowerPerHour", "Citizens",
            "The amount of power a player receives every hour)",
            1.00);
    
    
    public ConfigProperty<Double> PowerPerDeath = new ConfigProperty<Double>(
            "PowerPerDeath", "Citizens",
            "The amount of power a player loses when they die:",
            2.50);
    
    public ConfigProperty<Double> PowerUpdateTime = new ConfigProperty<Double>(
            "PowerUpdateTime", "Citizens",
            "The amount of time in milliseconds that power should update (default = 600000 a.k.a 10 min)",
            60000.00);
    
    
    public ConfigProperty<Integer> teleportCooldown = new ConfigProperty<Integer>(
            "teleportCooldown", "Citizens",
            "The amount of time in server ticks for how long a player needs to wait between teleports.",
            200);
    
    
    public ConfigProperty<String> costItemName = new ConfigProperty<String>(
            "costItem", "cost",
            "The item which is used for paying for claims and making new Empires. Use $ForgeEssentials if you want to use ForgeEssentials economy or $Vault if you want Vault economy.", 
            GameRegistry.findUniqueIdentifierFor(Items.diamond).toString());
    
    
    public ConfigProperty<Integer> costAmountMakeEmpire = new ConfigProperty<Integer>(
            "costAmountMakeEmpire", "cost",
            "The amount of the cost item you need to create a Empire. Making a Empire will cost this amount + amount to claim a chunk.",
            5);
    
    
    public ConfigProperty<Integer> costAmountClaim = new ConfigProperty<Integer>(
            "costAmountClaim", "cost",
            "The amount of the cost item you need to claim a chunk.",
            5);
    
    
    public ConfigProperty<Integer> costAmountClaimFar = new ConfigProperty<Integer>(
            "costAmountClaimFar", "cost",
            "The amount of the cost item you need to claim a chunk that is not adjacent to the Empire.",
            64);
    
    
    public ConfigProperty<Integer> costAmountSpawn = new ConfigProperty<Integer>(
            "costAmountSpawn", "cost",
            "The amount of the cost item you need to warp to the Empire's spawn point.",
            0);
    
    
    public ConfigProperty<Integer> costAmountOtherSpawn = new ConfigProperty<Integer>(
            "costAmountOtherSpawn", "cost",
            "The amount of the cost item you need to warp to the spawn point of a Empire that the player is not a Citizen of.",
            3);
    
    
    public ConfigProperty<Integer> costAmountSetSpawn = new ConfigProperty<Integer>(
            "costAmountSetSpawn", "cost",
            "The amount of the cost item you need to create a spawn point for the Empire.",
            15);
    
    
    public ConfigProperty<Integer> costEmpireUpkeep = new ConfigProperty<Integer>(
            "costEmpireUpkeep", "cost",
            "The amount of the cost item Empires have to pay everyday to maintain it.",
            10);
    
    
    public ConfigProperty<Integer> costAdditionalUpkeep = new ConfigProperty<Integer>(
            "costAdditionalUpkeep", "cost", "The amount of the cost item Empires have to pay everyday per chunk owned to maintain it.",
            0);
    
    
    public ConfigProperty<Integer> costAdditionalChunkloadedUpkeep = new ConfigProperty<Integer>(
            "costAdditionalChunkloadedUpkeep", "cost",
            "The amount of the cost item Empires have to pay everyday per chunkloaded claim. This should usually be higher than the normal claim",
            0);
    
    
    public ConfigProperty<Integer> costAdditionClaim = new ConfigProperty<Integer>(
            "costAdditionClaim", "cost", "The additional amount of the cost item people need to pay for each block already claimed [Ex: if you have 3 chunks in Empire claiming the next one will cost costAdditionClaim*3 + costAmountClaim]. This can be used with costMultiplicativeClaim.",
            0);
    
    
    public ConfigProperty<Double> costMultiplicativeClaim = new ConfigProperty<Double>(
            "costMultiplicativeClaim", "cost",
            "The multiplicative amount of the cost item people need to pay for each block already claimed [Ex: if you have 2 chunks the next one will cost costMultiplicativeClaim ^ 2 * costAmountClaim]. This can be used with costAdditionClaim.",
            1.0D);
    
    
    public ConfigProperty<Integer> costAmountChunkloadedClaim = new ConfigProperty<Integer>(
            "costAmountChunkloadedClaim", "cost",
            "The additional amount of the cost item people need to pay to make a regular claim chunkloaded",
            10);
    
    
    public ConfigProperty<Integer> defaultBankAmount = new ConfigProperty<Integer>(
            "defaultBankAmount", "cost",
            "The amount of the cost item that the Empires are gonna start with in their banks after created.",
            5);
    
    
    public ConfigProperty<Integer> empireNameMaxChars = new ConfigProperty<Integer>(
            "empireNameMaxChars", "Empires",
            "The maximum amount of characters that an Empire's name can contain: (default = 32):",
            32);
    
    
    public ConfigProperty<Integer> defaultMaxPlots = new ConfigProperty<Integer>(
            "defaultMaxPlotsPerPlayer", "Empires",
            "The maximum amount of plots a player can make in a Empire as a default.",
            1);
    
    
    public ConfigProperty<Integer> upkeepEmpireDeletionDays = new ConfigProperty<Integer>(
            "upkeepEmpireDeletionDays", "Empires",
            "The amount of days a Empire can go on without paying upkeep.",
            14);
    
    
    public ConfigProperty<Integer> minPlotsArea = new ConfigProperty<Integer>(
            "minPlotsArea", "plots",
            "The minimum area required to create a plot. (X*Z)",
            9);
    
    
    public ConfigProperty<Integer> minPlotsHeight = new ConfigProperty<Integer>(
            "minPlotsHeight", "plots",
            "The minimum height required to create a plot. (Y)",
            1);
    
    
    public ConfigProperty<Integer> maxPlotsArea = new ConfigProperty<Integer>(
            "maxPlotsArea", "plots",
            "The maximum area a plot can have. (X*Z)",
            300);
    
    
    public ConfigProperty<Integer> maxPlotsHeight = new ConfigProperty<Integer>(
            "maxPlotsHeight", "plots",
            "The maximum height a plot can have. (Y) [255 for unlimited height.]",
            256);
    
    
    public ConfigProperty<Boolean> enablePlots = new ConfigProperty<Boolean>(
            "enablePlots", "plots",
            "Set this to false to disable all types of plot interaction.",
            true);
    
    
    public ConfigProperty<Boolean> defaultPlotHightDependence = new ConfigProperty<Boolean>(
            "defaultPlotHeightDependence", "plots",
            "This sets if the plot selection tool defaults to height dependent or not",
            true
    );
    
    
    public ConfigProperty<Integer> defaultProtectionSize = new ConfigProperty<Integer>(
            "defaultProtectionSize", "protection",
            "The range that it's going to check in if a protection's segment that has a tileentity does not provide getters for its area of influence.",
            32);
    
    
    public ConfigProperty<Boolean> fireSpreadInEmpires = new ConfigProperty<Boolean>(
            "fireSpreadInEmpires", "protection",
            "Allow fire to spread and burn up blocks in all Empires and plots on the server. default: true",
            true);
    
    
    public ConfigProperty<Boolean> taintSpreadInEmpires = new ConfigProperty<Boolean>(
            "taintSpreadInEmpires", "protection",
            "Allow Thaumcraft Taint biomes to spread in all Empires and plots on the server.",
            false);
    
    
    public ConfigProperty<Boolean> mobTravelInEmpires = new ConfigProperty<Boolean>(
            "mobTravelInEmpires", "protection",
            "Allow mobs to travel into, but not spawn in a mob protected Empires and plots on the server.",
            false);
    
}