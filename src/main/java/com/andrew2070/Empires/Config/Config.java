package com.andrew2070.Empires.Config;


import com.andrew2070.Empires.Config.ConfigTemplate;
import cpw.mods.fml.common.registry.GameRegistry;
import com.andrew2070.Empires.Config.ConfigProperty;
import net.minecraft.init.Items;


public class Config extends ConfigTemplate {

    public static final Config instance = new Config();
    

    public ConfigProperty<Boolean> maintenanceMode = new ConfigProperty<Boolean>(
            "maintenanceMode", "general",
            "Allows toggling maintenance mode",
            false);

    public ConfigProperty<String> maintenanceModeMessage = new ConfigProperty<String>(
            "maintenanceModeMessage", "general",
            "Custom message to display when in maintenance mode",
            "Server is in maintenance mode currently. Please come back later.");


    public ConfigProperty<String> permissionSystem = new ConfigProperty<String>(
            "permissionSystem", "general",
            "The permission system it should be used as default. $ForgeEssentials for FE permission system, $Bukkit for Bukkit permission system, $ServerTools for ServerTools-PERMISION's permisison system and $Empires for our own permission system.",
            "$Empires Permissions");

    public ConfigProperty<String> localization = new ConfigProperty<String>(
            "localization", "general",
            "The localization used",
            "en_US");

    public ConfigProperty<Boolean> fullAccessForOPS = new ConfigProperty<Boolean>(
            "fullAccessForOPS", "permissions",
            "Players that are opped will have access to any command.",
            true);

    public ConfigProperty<String> defaultGroup = new ConfigProperty<String>(
            "defaultGroup", "groups",
            "The default group assigned to any player that does not have one assigned yet.",
            "default");
    
    public ConfigProperty<String> safeModeMsg = new ConfigProperty<String>(
            "safeModeMessage", "general",
            "Message to display to users when Empires is in safemode.",
            "Empires is in safe mode. Please tell a server admin!");

    /* ----- Datasource Config ----- */

    public ConfigProperty<String> dbType = new ConfigProperty<String>(
            "type", "datasource", "Datasource Type. Eg: MySQL, SQLite, etc.",
            "SQLite");

    /* ----- Others ----- */

    public ConfigProperty<Integer> distanceBetweenEmpires = new ConfigProperty<Integer>(
            "distance", "empires",
            "Minimum distance (in chunks) between 2 empires. Checked when creating a empire",
            5);
    public ConfigProperty<Integer> blocksLeader = new ConfigProperty<Integer>(
            "blocksLeader", "empires",
            "The amount of maximum blocks a empire gets from the leader.",
            5);
    public ConfigProperty<Integer> blocksCitizen = new ConfigProperty<Integer>(
            "blocksCitizens", "empires",
            "The amount of maximum blocks a empire gets from each player.",
            3);
    public ConfigProperty<Integer> maxEmpires = new ConfigProperty<Integer>(
            "maxEmpires", "Citizens",
            "The amount of empires a Citizen can be in, don't change this or a lot of things will break",
            1);
    public ConfigProperty<Integer> placeProtectionRange = new ConfigProperty<Integer>(
            "placeProtectionRange", "empires",
            "The distance in blocks from a protected empire where you can't place a block in the Wild.",
            1);
    public ConfigProperty<Boolean> modifiableRanks = new ConfigProperty<Boolean>(
            "modifiableRanks", "empires",
            "If true Citizens with permission can modify the ranks of their empires. This feature hasn't been fully tested yet and it might cause problems!",
            false);
    public ConfigProperty<Integer> maxFarClaims = new ConfigProperty<Integer>(
            "maxFarClaims", "empires",
            "The maximum amount of chunks not attached to any other claimed chunk that are allowed per empire. Set to 0 to disable far claims altogether.",
            0);
    public ConfigProperty<Integer> teleportCooldown = new ConfigProperty<Integer>(
            "teleportCooldown", "Citizens",
            "The amount of time in server ticks for how long a player needs to wait between teleports.",
            200);
    public ConfigProperty<String> costItemName = new ConfigProperty<String>(
            "costItem", "cost",
            "The item which is used for paying for claims and making new empires. Use $ForgeEssentials if you want to use ForgeEssentials economy or $Vault if you want Vault economy.",
            GameRegistry.findUniqueIdentifierFor(Items.diamond).toString());
    public ConfigProperty<Integer> costAmountMakeEmpire = new ConfigProperty<Integer>(
            "costAmountMakeEmpire", "cost",
            "The amount of the cost item you need to create a empire. Making a empire will cost this amount + amount to claim a chunk.",
            5);
    public ConfigProperty<Integer> costAmountClaim = new ConfigProperty<Integer>(
            "costAmountClaim", "cost",
            "The amount of the cost item you need to claim a chunk.",
            3);
    public ConfigProperty<Integer> costAmountClaimFar = new ConfigProperty<Integer>(
            "costAmountClaimFar", "cost",
            "The amount of the cost item you need to claim a chunk that is not adjacent to the empire.",
            8);
    public ConfigProperty<Integer> costAmountSpawn = new ConfigProperty<Integer>(
            "costAmountSpawn", "cost",
            "The amount of the cost item you need to warp to the empire's spawn point.",
            0);
    public ConfigProperty<Integer> costAmountOtherSpawn = new ConfigProperty<Integer>(
            "costAmountOtherSpawn", "cost",
            "The amount of the cost item you need to warp to the spawn point of a empire that the player is not a Citizen of.",
            1);
    public ConfigProperty<Integer> costAmountSetSpawn = new ConfigProperty<Integer>(
            "costAmountSetSpawn", "cost",
            "The amount of the cost item you need to create a spawn point for the empire.",
            1);
    public ConfigProperty<Integer> costEmpireUpkeep = new ConfigProperty<Integer>(
            "costEmpireUpkeep", "cost",
            "The amount of the cost item empires have to pay everyday to maintain it.",
            0);
    public ConfigProperty<Integer> costAdditionalUpkeep = new ConfigProperty<Integer>(
            "costAdditionalUpkeep", "cost", "The amount of the cost item empires have to pay everyday per chunk owned to maintain it.",
            0);
    public ConfigProperty<Integer> costAdditionalChunkloadedUpkeep = new ConfigProperty<Integer>(
            "costAdditionalChunkloadedUpkeep", "cost",
            "The amount of the cost item empires have to pay everyday per chunkloaded claim. This should usually be higher than the normal claim",
            0);
    public ConfigProperty<Integer> costAdditionClaim = new ConfigProperty<Integer>(
            "costAdditionClaim", "cost", "The additional amount of the cost item people need to pay for each block already claimed [Ex: if you have 3 chunks in empire claiming the next one will cost costAdditionClaim*3 + costAmountClaim]. This can be used with costMultiplicativeClaim.",
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
            "The amount of the cost item that the empires are gonna start with in their banks after created.",
            5);
    public ConfigProperty<Integer> defaultMaxPlots = new ConfigProperty<Integer>(
            "defaultMaxPlotsPerPlayer", "empires",
            "The maximum amount of plots a player can make in a empire as a default.",
            1);
    public ConfigProperty<Integer> upkeepEmpireDeletionDays = new ConfigProperty<Integer>(
            "upkeepEmpireDeletionDays", "empires",
            "The amount of days a empire can go on without paying upkeep.",
            7);
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
            "defaultPlotHightDependence", "plots",
            "This sets if the plot selection tool defaults to hight dependent or not",
            true
    );
    public ConfigProperty<Integer> defaultProtectionSize = new ConfigProperty<Integer>(
            "defaultProtectionSize", "protection",
            "The range that it's going to check in if a protection's segment that has a tileentity does not provide getters for its area of influence.",
            32);
    public ConfigProperty<Boolean> fireSpreadInEmpires = new ConfigProperty<Boolean>(
            "fireSpreadInEmpires", "protection",
            "Allow fire to spread and burn up blocks in all empires and plots on the server.",
            false);
    public ConfigProperty<Boolean> taintSpreadInEmpires = new ConfigProperty<Boolean>(
            "taintSpreadInEmpires", "protection",
            "Allow Thaumcraft Taint biomes to spread in all empires and plots on the server.",
            false);
    public ConfigProperty<Boolean> mobTravelInEmpires = new ConfigProperty<Boolean>(
            "mobTravelInEmpires", "protection",
            "Allow mobs to travel into, but not spawn in a mob protected empires and plots on the server.",
            false);
}