package EmpiresMod.commands.Admin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import EmpiresMod.Empires;
import EmpiresMod.API.Chat.Component.ChatComponentContainer;
import EmpiresMod.API.Chat.Component.ChatComponentFormatted;
import EmpiresMod.API.Chat.Component.ChatManager;
import EmpiresMod.API.Commands.Command.Command;
import EmpiresMod.API.Commands.Command.CommandManager;
import EmpiresMod.API.Commands.Command.CommandResponse;
import EmpiresMod.API.Commands.Command.CommandsEMP;
import EmpiresMod.API.JSON.Configuration.FlagsConfig;
import EmpiresMod.API.permissions.CommandTree;
import EmpiresMod.API.permissions.CommandTreeNode;
import EmpiresMod.API.permissions.PermissionProxy;
import EmpiresMod.Configuration.Config;
import EmpiresMod.Datasource.EmpiresDatasource;
import EmpiresMod.Datasource.EmpiresUniverse;
import EmpiresMod.Handlers.SafemodeHandler;
import EmpiresMod.Handlers.VisualsHandler;
import EmpiresMod.Localization.LocalizationManager;
import EmpiresMod.Utilities.ChatUtils;
import EmpiresMod.Utilities.PlayerUtils;
import EmpiresMod.Utilities.StringUtils;
import EmpiresMod.Utilities.WorldUtils;
import EmpiresMod.commands.Officer.CommandsOfficer;
import EmpiresMod.entities.Empire.Citizen;
import EmpiresMod.entities.Empire.Empire;
import EmpiresMod.entities.Empire.EmpireBlock;
import EmpiresMod.entities.Empire.Plot;
import EmpiresMod.entities.Empire.Rank;
import EmpiresMod.entities.Empire.Wild;
import EmpiresMod.entities.Flags.Flag;
import EmpiresMod.entities.Flags.FlagType;
import EmpiresMod.entities.Managers.ToolManager;
import EmpiresMod.entities.Position.ChunkPos;
import EmpiresMod.entities.Tools.WhitelisterTool;
import EmpiresMod.exceptions.Command.CommandException;
import EmpiresMod.exceptions.Empires.EmpiresCommandException;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

/**
 * All commands for administrators go here
 */
@SuppressWarnings("unused")
public class CommandsAdmin extends CommandsEMP {

    private CommandsAdmin() {

    }

    @Command(
            name = "Empiresadmin",
            permission = "Empires.adm.cmd",
            syntax = "/empireadmin <command>",
            alias = {"empadmin", "empireadmin", "eadmin", "Empadmin", "Empireadmin", "Eadmin"})
    public static CommandResponse empireAdminCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "config",
            permission = "Empires.adm.cmd.config",
            parentName = "Empires.adm.cmd",
            syntax = "/empireadmin config <command>",
            console = true)
    public static CommandResponse configCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "reload",
            permission = "Empires.adm.cmd.config.reload",
            parentName = "Empires.adm.cmd.config",
            syntax = "/empireadmin config reload",
            console = true)
    public static CommandResponse configReloadCommand(ICommandSender sender, List<String> args) {
        ChatManager.send(sender, "Empires.cmd.config.load.start");
        Empires.instance.loadConfigs();
        getDatasource().checkAll();
        ChatManager.send(sender, "Empires.cmd.config.load.stop");
        return CommandResponse.DONE;
    }
    @Command(
            name = "desc",
            permission = "Empires.adm.cmd.desc",
            parentName = "Empires.adm.cmd",
            syntax = "/empireadmin desc <empire> <newdesc>",
            console = true)
    public static CommandResponse desc(ICommandSender sender, List<String> args) {

         if (args.isEmpty()) {
             return CommandResponse.SEND_SYNTAX;
         } 
     	
    	 Empire empire = getEmpireFromName(args.get(0));
         String desc = "";
         for (int i=1; i < args.size(); i++) {
         	String newdesc = args.get(i);
         	desc = desc + " " + newdesc;
         }
         
         if (desc.length() > Config.instance.maxDescChars.get()) {
         	throw new EmpiresCommandException("Empires.cmd.err.desc.maxChars");
         }
         empire.setDesc(desc);
         getDatasource().saveEmpire(empire);
         ChatManager.send(sender, "Empires.notification.desc.succesful");
         empire.notifyEveryone(getLocal().getLocalization("Empires.notification.empire.desc", sender.getDisplayName(), empire.getDesc()));
        return CommandResponse.DONE;
    }

    @Command(
            name = "reset",
            permission = "Empires.adm.cmd.config.reset",
            parentName = "Empires.adm.cmd.config",
            syntax = "/empireadmin config reset <command>",
            console = true)
    public static CommandResponse configResetCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "defaultRanks",
            permission = "Empires.adm.cmd.config.reset.defaultRanks",
            parentName = "Empires.adm.cmd.config.reset",
            syntax = "/empireadmin config reset ranks",
            console = true)
    public static CommandResponse configResetRanksCommand(ICommandSender sender, List<String> args) {
        Empires.instance.getRanksConfig().create(new Rank.Container());
        ChatManager.send(sender, "Empires.cmd.config.reset", Empires.instance.getRanksConfig().getName());
        return CommandResponse.DONE;
    }

    @Command(
            name = "wild",
            permission = "Empires.adm.cmd.config.reset.wildPerms",
            parentName = "Empires.adm.cmd.config.reset",
            syntax = "/empireadmin config reset wild",
            console = true)
    public static CommandResponse configResetWildCommand(ICommandSender sender, List<String> args) {
        Empires.instance.getWildConfig().create(new Flag.Container());
        ChatManager.send(sender, "Empires.cmd.config.reset", Empires.instance.getWildConfig().getName());
        return CommandResponse.DONE;
    }

    @Command(
            name = "defaultFlags",
            permission = "Empires.adm.cmd.config.reset.defaultFlags",
            parentName = "Empires.adm.cmd.config.reset",
            syntax = "/empireadmin config reset defaultFlags",
            console = true)
    public static CommandResponse configResetFlagsCommand(ICommandSender sender, List<String> args) {
        Empires.instance.getFlagsConfig().create(new ArrayList<FlagsConfig.Wrapper>());
        ChatManager.send(sender, "Empires.cmd.config.reset", Empires.instance.getFlagsConfig().getName());
        return CommandResponse.DONE;
    }

    @Command(
            name = "update",
            permission = "Empires.adm.cmd.update",
            parentName = "Empires.adm.cmd",
            syntax = "/empireadmin update <command>",
            console = true)
    public static CommandResponse updateCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "ranks",
            permission = "Empires.adm.cmd.update.ranks",
            parentName = "Empires.adm.cmd.update",
            syntax = "/empireadmin update ranks",
            console = true)
    public static CommandResponse updateRanksCommand(ICommandSender sender, List<String> args) {
        Empires.instance.getRanksConfig().create(new Rank.Container());
        for(Empire empire : getUniverse().empires) {
            getDatasource().resetRanks(empire);
        }
        ChatManager.send(sender, "Empires.notification.update.ranks");
        return CommandResponse.DONE;
    }

    @Command(
            name = "add",
            permission = "Empires.adm.cmd.add",
            parentName = "Empires.adm.cmd",
            syntax = "/empireadmin add <citizen> <empire> [rank]",
            console = true,
            completionKeys = {"citizenCompletion", "empireCompletion"})
    public static CommandResponse addCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 2)
            return CommandResponse.SEND_SYNTAX;

        Citizen target = getCitizenFromName(args.get(0));
        Empire empire = getEmpireFromName(args.get(1));

        if (empire.citizensMap.containsKey(target)) {
            throw new EmpiresCommandException("Empires.adm.cmd.err.add.already", target, empire);
        }

        Rank rank;
        if (args.size() > 2) {
            rank = getRankFromEmpire(empire, args.get(2));
        } else {
            rank = empire.ranksContainer.getDefaultRank();
        }

        getDatasource().linkCitizenToEmpire(target, empire, rank);
        empire.addPower(target.getPower());
        empire.addMaxPower(target.getMaxPower());

        ChatManager.send(sender, "Empires.notification.empire.citizen.add", target, empire, rank);
        ChatManager.send(target.getPlayer(), "Empires.notification.empire.added", empire);
        return CommandResponse.DONE;
    }

    @Command(
            name = "delete",
            permission = "Empires.adm.cmd.delete",
            parentName = "Empires.adm.cmd",
            syntax = "/empireadmin delete <empire>",
            console = true,
            completionKeys = {"empireCompletion"})
    public static CommandResponse deleteCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1) {
            return CommandResponse.SEND_SYNTAX;
        }

        for (String s : args) {
            if (!getUniverse().empires.contains(s)) {
                throw new EmpiresCommandException("Empires.cmd.err.empire.missing", s);
            }
        }
        for (String s : args) {
            Empire empire = getUniverse().empires.get(s);
            if (getDatasource().deleteEmpire(empire)) {
                ChatManager.send(sender, "Empires.notification.empire.deleted", empire);
            }
        }
        return CommandResponse.DONE;
    }

    @Command(
            name = "new",
            permission = "Empires.adm.cmd.new",
            parentName = "Empires.adm.cmd",
            syntax = "/empireadmin new <name>")
    public static CommandResponse newCommand(ICommandSender sender, List<String> args) throws net.minecraft.command.CommandException {
        if (args.size() < 1) {
            return CommandResponse.SEND_SYNTAX;
        }

        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        ChatManager.send(res.getPlayer(), "Empires.notification.empire.startedCreation", args.get(0));

        EntityPlayer player = (EntityPlayer) sender;
        if (getUniverse().empires.contains(args.get(0))) {
            throw new EmpiresCommandException("Empires.cmd.err.new.nameUsed", args.get(0));
        }
        if (getUniverse().blocks.contains(player.dimension, player.chunkCoordX, player.chunkCoordZ)) {
            throw new EmpiresCommandException("Empires.cmd.err.new.position");
        }
        if (args.get(0).length() > 32) {
            throw new EmpiresCommandException("Empires.cmd.err.new.nameTooLong");
        }

        Empire empire = getUniverse().newAdminEmpire(args.get(0), res);
        if (empire == null) {
            throw new EmpiresCommandException("Empires.cmd.err.new.failed");
        }

        ChatManager.send(sender, "Empires.notification.empire.created", empire);
        return CommandResponse.DONE;
    }

    @Command(
            name = "kick",
            permission = "Empires.adm.cmd.kick",
            parentName = "Empires.adm.cmd",
            syntax = "/empireadmin kick <citizen> <empire>",
            console = true,
            completionKeys = {"citizenCompletion", "empireCompletion"})
    public static CommandResponse remCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 2) {
            return CommandResponse.SEND_SYNTAX;
        }

        Citizen target = getCitizenFromName(args.get(0));
        Empire empire = getEmpireFromName(args.get(1));

        if (!empire.citizensMap.containsKey(target)) {
            throw new EmpiresCommandException("Empires.adm.cmd.err.kick.citizen", target, empire);
        }
        
        empire.subtractPower(target.getPower());
        empire.subtractMaxPower(target.getMaxPower());
        getDatasource().unlinkCitizenFromEmpire(target, empire);
        getDatasource().saveEmpire(empire);
        ChatManager.send(sender, "Empires.notification.empire.citizen.remove", target, empire);
        return CommandResponse.DONE;
    }

    @Command(
            name = "res",
            permission = "Empires.adm.cmd.res",
            parentName = "Empires.adm.cmd",
            syntax = "/empireadmin res <command>",
            console = true)
    public static CommandResponse resCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "blocks",
            permission = "Empires.adm.cmd.blocks",
            parentName = "Empires.adm.cmd",
            syntax = "/empireadmin blocks <command>",
            console = true)
    public static CommandResponse empireBlocksCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "info",
            permission = "Empires.adm.cmd.blocks.info",
            parentName = "Empires.adm.cmd.blocks",
            syntax = "/empireadmin blocks info <empire>",
            completionKeys = {"empireCompletion"},
            console = true)
    public static CommandResponse blocksInfoCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 1) {
            return CommandResponse.SEND_SYNTAX;
        }

        Empire empire = getEmpireFromName(args.get(0));

        IChatComponent header = LocalizationManager.get("Empires.format.list.header", new ChatComponentFormatted("{9|BLOCKS}"));
        String blocks = empire.empireBlocksContainer.size() + "/" + empire.getMaxBlocks();
        String extraBlocks = empire.getExtraBlocks() + "";
        String farBlocks = empire.empireBlocksContainer.getFarClaims() + "/" + empire.getMaxFarClaims();

        ChatComponentContainer extraBlocksSources = new ChatComponentContainer();
        extraBlocksSources.add(LocalizationManager.get("Empires.notification.blocks.info.extra", new ChatComponentFormatted("{9|EMPIRE}"), empire.empireBlocksContainer.getExtraBlocks()));
        for(Citizen res : empire.citizensMap.keySet()) {
            extraBlocksSources.add(LocalizationManager.get("Empires.notification.blocks.info.extra", res, res.getExtraBlocks()));
        }

        ChatManager.send(sender, "Empires.notification.blocks.info", header, blocks, extraBlocks, extraBlocksSources, farBlocks);
        return CommandResponse.DONE;
    }
//Deprecated
   /*/ @Command(
            name = "extra",
            permission = "Empires.adm.cmd.blocks.extra",
            parentName = "Empires.adm.cmd.blocks",
            syntax = "/empireadmin blocks extra <command>",
            console = true)
    public static CommandResponse empireBlocksMaxCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "set",
            permission = "Empires.adm.cmd.blocks.extra.set",
            parentName = "Empires.adm.cmd.blocks.extra",
            syntax = "/empireadmin blocks extra set <empire> <amount>",
            completionKeys = {"empireCompletion"},
            console = true)
    public static CommandResponse empireBlocksMaxSetCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 2) {
            return CommandResponse.SEND_SYNTAX;
        }

        checkPositiveInteger(args.get(1));

        Empire empire = getEmpireFromName(args.get(0));
        empire.empireBlocksContainer.setExtraBlocks(Integer.parseInt(args.get(1)));
        getDatasource().saveEmpire(empire);
        ChatManager.send(sender, "Empires.notification.empire.blocks.extra.set", empire.empireBlocksContainer.getExtraBlocks(), empire);
        return CommandResponse.DONE;
    }
    /*/
  
    @Command(
            name = "powerboost",
            permission = "Empires.adm.cmd.powerboost",
            syntax = "/empireadmin powerboost <citizen> <amount>",
            parentName = "Empires.adm.cmd",
            completionKeys = {"citizenCompletion"},
            console = true)
    public static CommandResponse powerBoostCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 2) {
            return CommandResponse.SEND_SYNTAX;
        }
        

        checkPositiveInteger(args.get(1));
        Citizen citizen = getCitizenFromName(args.get(0));
        double boostValue = Integer.parseInt(args.get(1));
        
        if (boostValue < citizen.getPower()) {
        	double difference = citizen.getPower() - boostValue;
        	citizen.subtractPower(difference);
        	citizen.subtractMaxPower(difference);
        	try {
        	Empire empire = CommandsEMP.getEmpireFromCitizen(citizen);
        	empire.subtractPower(difference);
        	empire.subtractMaxPower(difference);
        	getDatasource().saveEmpire(empire);
        	} catch (CommandException e) {
        		//keep it from breaking if a player has no empire
        	}
        }
        citizen.setOldPower(citizen.getPower());
        citizen.setOldMaxPower(citizen.getMaxPower());
        citizen.setMaxPower(boostValue);
        citizen.setPower(boostValue);
        getDatasource().saveCitizen(citizen);
        Empires.instance.datasource.saveCitizen(citizen);
        try {
        Empire empire = CommandsEMP.getEmpireFromCitizen(citizen);
        empire.recalculatePower(citizen);
        empire.subtractPower(citizen.getOldPower());
        getDatasource().saveEmpire(empire);
        
        } catch (CommandException e) {
        	//keep it from breaking if a player has no empire
        } 
        ChatManager.send(sender, "Empires.notification.citizen.powerboost", boostValue, citizen.getPlayerName());
        return CommandResponse.DONE;
    }
    
//Deprecated
/*/
    @Command(
            name = "add",
            permission = "Empires.adm.cmd.blocks.extra.add",
            parentName = "Empires.adm.cmd.blocks.extra",
            syntax = "/empireadmin blocks extra add <empire> <amount>",
            completionKeys = {"empireCompletion"},
            console = true)
    public static CommandResponse empireBlocksMaxAddCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 2) {
            return CommandResponse.SEND_SYNTAX;
        }

        checkPositiveInteger(args.get(1));

        Empire empire = getEmpireFromName(args.get(0));
        int amount = Integer.parseInt(args.get(1));
        empire.empireBlocksContainer.setExtraBlocks(empire.empireBlocksContainer.getExtraBlocks() + amount);
        getDatasource().saveEmpire(empire);
        ChatManager.send(sender, "Empires.notification.empire.blocks.extra.set", empire.empireBlocksContainer.getExtraBlocks(), empire);
        return CommandResponse.DONE;
    }

    @Command(
            name = "remove",
            permission = "Empires.adm.cmd.blocks.extra.remove",
            parentName = "Empires.adm.cmd.blocks.extra",
            syntax = "/empireadmin blocks extra remove <empire> <amount>",
            completionKeys = {"empireCompletion"},
            console = true)
    public static CommandResponse empireBlocksMaxRemoveCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 2) {
            return CommandResponse.SEND_SYNTAX;
        }

        checkPositiveInteger(args.get(1));

        Empire empire = getEmpireFromName(args.get(0));
        int amount = Integer.parseInt(args.get(1));
        empire.empireBlocksContainer.setExtraBlocks(empire.empireBlocksContainer.getExtraBlocks() - amount);
        getDatasource().saveEmpire(empire);
        ChatManager.send(sender, "Empires.notification.empire.blocks.extra.set", empire.empireBlocksContainer.getExtraBlocks(), empire);
        return CommandResponse.DONE;
    }
    /*/

    @Command(
            name = "far",
            permission = "Empires.adm.cmd.blocks.far",
            parentName = "Empires.adm.cmd.blocks",
            syntax = "/empireadmin blocks far <command>")
    public static CommandResponse empireBlocksFarClaimsCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "set",
            permission = "Empires.adm.cmd.blocks.far.set",
            parentName = "Empires.adm.cmd.blocks.far",
            syntax = "/empireadmin blocks far set <empire> <amount>",
            completionKeys = {"empireCompletionAndAll"},
            console = true)
    public static CommandResponse empireBlocksFarclaimsSetCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 2) {
            return CommandResponse.SEND_SYNTAX;
        }

        checkPositiveInteger(args.get(1));

        Empire empire = getEmpireFromName(args.get(0));
        empire.empireBlocksContainer.setExtraFarClaims(Integer.parseInt(args.get(1)));
        getDatasource().saveEmpire(empire);
        ChatManager.send(sender, "Empires.notification.empire.blocks.farClaims.set", empire.empireBlocksContainer.getExtraFarClaims(), empire);
        return CommandResponse.DONE;
    }

    @Command(
            name = "add",
            permission = "Empires.adm.cmd.blocks.far.add",
            parentName = "Empires.adm.cmd.blocks.far",
            syntax = "/empireadmin blocks far add <empire> <amount>",
            completionKeys = {"empireCompletionAndAll"},
            console = true)
    public static CommandResponse empireBlocksFarclaimsAddCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 2) {
            return CommandResponse.SEND_SYNTAX;
        }

        checkPositiveInteger(args.get(1));

        Empire empire = getEmpireFromName(args.get(0));
        int amount = Integer.parseInt(args.get(1));
        empire.empireBlocksContainer.setExtraFarClaims(empire.empireBlocksContainer.getExtraFarClaims() + amount);
        getDatasource().saveEmpire(empire);
        ChatManager.send(sender, "Empires.notification.empire.blocks.farClaims.set", empire.empireBlocksContainer.getExtraFarClaims(), empire);
        return CommandResponse.DONE;
    }

    @Command(
            name = "remove",
            permission = "Empires.adm.cmd.blocks.far.remove",
            parentName = "Empires.adm.cmd.blocks.far",
            syntax = "/empireadmin blocks far remove <empire> <amount>",
            completionKeys = {"empireCompletionAndAll"},
            console = true)
    public static CommandResponse empireBlocksFarClaimsRemoveCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 2) {
            return CommandResponse.SEND_SYNTAX;
        }

        checkPositiveInteger(args.get(1));

        Empire empire = getEmpireFromName(args.get(0));
        int amount = Integer.parseInt(args.get(1));
        empire.empireBlocksContainer.setExtraFarClaims(empire.empireBlocksContainer.getExtraFarClaims() - amount);
        getDatasource().saveEmpire(empire);
        ChatManager.send(sender, "Empires.notification.empire.blocks.farClaims.set", empire.empireBlocksContainer.getExtraFarClaims(), empire);
        return CommandResponse.DONE;
    }
//Deprecated
/*/
    @Command(
            name = "blocks",
            permission = "Empires.adm.cmd.res.blocks",
            parentName = "Empires.adm.cmd.res",
            syntax = "/empireadmin res blocks <command>",
            console = true)
    public static CommandResponse resBlocksCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "extra",
            permission = "Empires.adm.cmd.res.blocks.extra",
            parentName = "Empires.adm.cmd.res.blocks",
            syntax = "/empireadmin res blocks extra <command>",
            console = true)
    public static CommandResponse resBlocksMaxCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "set",
            permission = "Empires.adm.cmd.res.blocks.extra.set",
            parentName = "Empires.adm.cmd.res.blocks.extra",
            syntax = "/empireadmin res blocks extra set <citizen> <extraBlocks>",
            completionKeys = {"citizenCompletion"},
            console = true)
    public static CommandResponse resBlocksSetCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 2) {
            return CommandResponse.SEND_SYNTAX;
        }

        checkPositiveInteger(args.get(1));

        Citizen target = getCitizenFromName(args.get(0));
        int amount = Integer.parseInt(args.get(1));
        target.setExtraBlocks(amount);
        getDatasource().saveCitizen(target);
        ChatManager.send(sender, "Empires.notification.res.blocks.extra.set", target.getExtraBlocks(), target);
        return CommandResponse.DONE;
    }

    @Command(
            name = "add",
            permission = "Empires.adm.cmd.res.blocks.extra.add",
            parentName = "Empires.adm.cmd.res.blocks.extra",
            syntax = "/empireadmin res blocks extra add <citizen> <extraBlocks>",
            completionKeys = {"citizenCompletion"},
            console = true)
    public static CommandResponse resBlocksAddCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 2) {
            return CommandResponse.SEND_SYNTAX;
        }

        checkPositiveInteger(args.get(1));

        Citizen target = getCitizenFromName(args.get(0));
        int amount = Integer.parseInt(args.get(1));
        target.setExtraBlocks(target.getExtraBlocks() + amount);
        getDatasource().saveCitizen(target);
        ChatManager.send(sender, "Empires.notification.res.blocks.extra.set", target.getExtraBlocks(), target);
        return CommandResponse.DONE;
    }

    @Command(
            name = "remove",
            permission = "Empires.adm.cmd.res.blocks.extra.remove",
            parentName = "Empires.adm.cmd.res.blocks.extra",
            syntax = "/empireadmin res blocks extra remove <citizen> <extraBlocks>",
            completionKeys = {"citizenCompletion"},
            console = true)
    public static CommandResponse resBlocksRemoveCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 2) {
            return CommandResponse.SEND_SYNTAX;
        }

        checkPositiveInteger(args.get(1));

        Citizen target = getCitizenFromName(args.get(0));
        int amount = Integer.parseInt(args.get(1));
        target.setExtraBlocks(target.getExtraBlocks() - amount);
        getDatasource().saveCitizen(target);
        ChatManager.send(sender, "Empires.notification.res.blocks.extra.set", target.getExtraBlocks(), target);
        return CommandResponse.DONE;
    }
/*/

    @Command(
            name = "ranks",
            permission = "Empires.adm.cmd.ranks",
            parentName = "Empires.adm.cmd",
            syntax = "/empireadmin ranks <command>",
            console = true)
    public static CommandResponse ranksCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "add",
            permission = "Empires.adm.cmd.ranks.add",
            parentName = "Empires.adm.cmd.ranks",
            syntax = "/empireadmin ranks add <empire> <name> [templateRank]",
            completionKeys = {"empireCompletion", "-", "ranksCompletion"},
            console = true)
    public static CommandResponse ranksAddCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 2) {
            return CommandResponse.SEND_SYNTAX;
        }

        Empire empire = getEmpireFromName(args.get(0));
        if (empire.ranksContainer.contains(args.get(1))) {
            throw new EmpiresCommandException("Empires.cmd.err.ranks.add.already", empire.ranksContainer.get(args.get(1)));
        }

        Rank rank = new Rank(args.get(1), empire, Rank.Type.OFFICER);
        if(args.size() > 2) {
            Rank template = getRankFromEmpire(empire, args.get(2));
            rank.permissionsContainer.addAll(template.permissionsContainer);
        }

        getDatasource().saveRank(rank);
        ChatManager.send(sender, "Empires.notification.empire.ranks.add", rank, empire);
        return CommandResponse.DONE;
    }

    @Command(
            name = "remove",
            permission = "Empires.adm.cmd.ranks.remove",
            parentName = "Empires.adm.cmd.ranks",
            syntax = "/empireadmin ranks remove <empire> <rank>",
            completionKeys = {"empireCompletion", "rankCompletion"},
            console = true)
    public static CommandResponse ranksRemoveCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 2) {
            return CommandResponse.SEND_SYNTAX;
        }

        Empire empire = getEmpireFromName(args.get(0));
        Rank rank = getRankFromEmpire(empire, args.get(1));

        if (rank.getType().unique) {
            throw new EmpiresCommandException("Empires.cmd.err.ranks.cantDelete");
        }

        for(Rank citizenRank : empire.citizensMap.values()) {
            if(citizenRank == rank) {
                throw new EmpiresCommandException("Empires.cmd.err.ranks.assigned");
            }
        }

        getDatasource().deleteRank(rank);
        ChatManager.send(sender, "Empires.notification.empire.ranks.rem", rank, empire);

        return CommandResponse.DONE;
    }

    @Command(
            name = "set",
            permission = "Empires.adm.cmd.ranks.set",
            parentName = "Empires.adm.cmd.ranks",
            syntax = "/empireadmin ranks set <empire> <rank> <type>",
            completionKeys = {"empireCompletion", "rankCompletion"},
            console = true)
    public static CommandResponse ranksSetCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 3) {
            return CommandResponse.SEND_SYNTAX;
        }

        Empire empire = getEmpireFromName(args.get(0));
        Rank rank = getRankFromEmpire(empire, args.get(1));
        Rank.Type type = getRankTypeFromString(args.get(2));

        if(type.unique) {
            Rank fromRank = empire.ranksContainer.get(type);
            if(fromRank == rank) {
                throw new EmpiresCommandException("Empires.cmd.err.ranks.set.already", type);
            }
            fromRank.setType(Rank.Type.OFFICER);
            rank.setType(type);

            getDatasource().saveRank(rank);
            getDatasource().saveRank(fromRank);
        } else {
            rank.setType(type);

            getDatasource().saveRank(rank);
        }

        ChatManager.send(sender, "Empires.notification.ranks.set.successful", rank, type);
        return CommandResponse.DONE;
    }


    @Command(
            name = "add",
            permission = "Empires.adm.cmd.ranks.perm.add",
            parentName = "Empires.adm.cmd.ranks.perm",
            syntax = "/empireadmin ranks perm add <empire> <rank> <perm>",
            completionKeys = {"empireCompletion", "rankCompletion"},
            console = true)
    public static CommandResponse ranksPermAddCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 3) {
            return CommandResponse.SEND_SYNTAX;
        }

        Empire empire = getEmpireFromName(args.get(0));
        Rank rank = getRankFromEmpire(empire, args.get(1));

        getDatasource().saveRankPermission(rank, args.get(2));
        ChatManager.send(sender, "Empires.notification.empire.ranks.perm.add");

        return CommandResponse.DONE;
    }

    @Command(
            name = "remove",
            permission = "Empires.adm.cmd.ranks.perm.remove",
            parentName = "Empires.adm.cmd.ranks.perm",
            syntax = "/empireadmin ranks perm remove <empire> <rank> <perm>",
            completionKeys = {"empireCompletion", "rankCompletion"},
            console = true)
    public static CommandResponse ranksPermRemoveCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 3) {
            return CommandResponse.SEND_SYNTAX;
        }

        Empire empire = getEmpireFromName(args.get(0));
        Rank rank = getRankFromEmpire(empire, args.get(1));

        getDatasource().deleteRankPermission(rank, args.get(2));
        ChatManager.send(sender, "Empires.notification.empire.ranks.perm.remove");

        return CommandResponse.DONE;
    }

    @Command(
            name = "reset",
            permission = "Empires.adm.cmd.ranks.reset",
            parentName = "Empires.adm.cmd.ranks",
            syntax = "/empireadmin ranks reset <empire>",
            completionKeys = {"empireCompletion"},
            console = true)
    public static CommandResponse ranksResetCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 1) {
            return CommandResponse.SEND_SYNTAX;
        }

        Empire empire = getEmpireFromName(args.get(0));
        getDatasource().resetRanks(empire);
        ChatManager.send(sender, "Empires.notification.ranks.reset");

        return CommandResponse.DONE;
    }

    @Command(
            name = "perm",
            permission = "Empires.adm.cmd.ranks.perm",
            parentName = "Empires.adm.cmd.ranks",
            syntax = "/empireadmin ranks perm <command>")
    public static CommandResponse ranksPermCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "list",
            permission = "Empires.adm.cmd.ranks.perm.list",
            parentName = "Empires.adm.cmd.ranks.perm",
            syntax = "/empireadmin ranks perm list <empire> <rank>",
            completionKeys = {"empireCompletion", "rankCompletion"})
    public static CommandResponse ranksPermListCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 2) {
            return CommandResponse.SEND_SYNTAX;
        }

        Empire empire = getEmpireFromName(args.get(0));
        Rank rank = getRankFromEmpire(empire, args.get(1));

        ChatManager.send(sender, rank.permissionsContainer.toChatMessage());
        return CommandResponse.DONE;
    }

    @Command(
            name = "safemode",
            permission = "Empires.adm.cmd.safemode",
            parentName = "Empires.adm.cmd",
            syntax = "/empireadmin safemode <on|off>",
            console = true)
    public static CommandResponse safemodeCommand(ICommandSender sender, List<String> args) {
        boolean safemode;
        if (args.size() < 1) { // Toggle safemode
            safemode = !SafemodeHandler.isInSafemode();
        } else { // Set safemode
            safemode = ChatUtils.equalsOn(args.get(0));
        }

        SafemodeHandler.setSafemode(safemode);
        SafemodeHandler.kickPlayers();
        return CommandResponse.DONE;
    }

    @Command(
            name = "db",
            permission = "Empires.adm.cmd.db",
            parentName = "Empires.adm.cmd",
            syntax = "/empireadmin db <command>",
            console = true)
    public static CommandResponse dbCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "purge",
            permission = "Empires.adm.cmd.db.purge",
            parentName = "Empires.adm.cmd.db",
            syntax = "/empireadmin db purge",
            console = true)
    public static CommandResponse dbCommandPurge(ICommandSender sender, List<String> args) {
        for (Empire empire : getUniverse().empires) {
            getDatasource().deleteEmpire(empire);
        }
        for (Citizen citizen : getUniverse().citizens) {
            getDatasource().deleteCitizen(citizen);
        }

        ChatManager.send(sender, "Empires.notification.db.purging");
        return CommandResponse.DONE;
    }

    @Command(
            name = "reload",
            permission = "Empires.adm.cmd.db.reload",
            parentName = "Empires.adm.cmd.db",
            syntax = "/empireadmin db reload",
            console = true)
    public static CommandResponse dbReloadCommand(ICommandSender sender, List<String> args) {
        EmpiresUniverse.instance.clear();
        Empires.instance.datasource = new EmpiresDatasource();
        ChatManager.send(sender, "Empires.notification.db.reloaded");
        return CommandResponse.DONE;
    }

    @Command(
            name = "perm",
            permission = "Empires.adm.cmd.perm",
            parentName = "Empires.adm.cmd",
            syntax = "/empireadmin perm <command>",
            console = true)
    public static CommandResponse permCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "empire",
            permission = "Empires.adm.cmd.perm.empire",
            parentName = "Empires.adm.cmd.perm",
            syntax = "/empireadmin perm empire <command>",
            console = true)
    public static CommandResponse permEmpireCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "list",
            permission = "Empires.adm.cmd.perm.empire.list",
            parentName = "Empires.adm.cmd.perm.empire",
            syntax = "/empireadmin perm empire list <empire>",
            completionKeys = {"empireCompletion"},
            console = true)
    public static CommandResponse permEmpireListCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1) {
            return CommandResponse.SEND_SYNTAX;
        }

        Empire empire = getEmpireFromName(args.get(0));
        ChatManager.send(sender, empire.flagsContainer.toChatMessage());
        return CommandResponse.DONE;
    }

    @Command(
            name = "set",
            permission = "Empires.adm.cmd.perm.empire.set",
            parentName = "Empires.adm.cmd.perm.empire",
            syntax = "/empireadmin perm empire set <empire> <flag> <value>",
            completionKeys = {"empireCompletion", "flagCompletion"},
            console = true)
    public static CommandResponse permEmpireSetCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 3) {
            return CommandResponse.SEND_SYNTAX;
        }

        Empire empire = getEmpireFromName(args.get(0));
        Flag flag = getFlagFromName(empire.flagsContainer, args.get(1));
        EntityPlayer playerSender = null;
        if (sender instanceof EntityPlayer) {
            playerSender = (EntityPlayer) sender;
        }

        if (!flag.flagType.configurable && (playerSender == null || !PermissionProxy.getPermissionManager().hasPermission(playerSender.getPersistentID(), "Empires.adm.cmd.perm.empire.set." + flag.flagType + ".bypass"))) {
            throw new EmpiresCommandException("Empires.cmd.err.flag.unconfigurable", args.get(1));
        } else {
            if (flag.setValue(args.get(2))) {
                ChatManager.send(sender, "Empires.notification.perm.success");
            } else {
                throw new EmpiresCommandException("Empires.cmd.err.perm.valueNotValid");
            }
        }
        getDatasource().saveFlag(flag, empire);
        return CommandResponse.DONE;
    }

    @Command(
            name = "toggle",
            permission = "Empires.adm.cmd.perm.empire.toggle",
            parentName = "Empires.adm.cmd.perm.empire",
            syntax = "/empireadmin perm empire toggle <empire> <flag>",
            completionKeys = {"empireCompletion", "flagCompletion"},
            console = true)
    public static CommandResponse permEmpireToggleCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 2) {
            return CommandResponse.SEND_SYNTAX;
        }

        Empire empire = getEmpireFromName(args.get(0));
        Flag flag = getFlagFromName(empire.flagsContainer, args.get(1));
        EntityPlayer playerSender = null;
        if (sender instanceof EntityPlayer) {
            playerSender = (EntityPlayer) sender;
        }

        if (!flag.flagType.configurable && (playerSender == null || !PermissionProxy.getPermissionManager().hasPermission(playerSender.getPersistentID(), "Empires.adm.cmd.perm.empire.toggle." + flag.flagType + ".bypass"))) {
            throw new EmpiresCommandException("Empires.cmd.err.flag.unconfigurable", args.get(1));
        } else {
            if (flag.toggle()) {
                ChatManager.send(sender, "Empires.notification.perm.success");
            } else {
                throw new EmpiresCommandException("Empires.cmd.err.perm.valueNotValid");
            }
        }
        getDatasource().saveFlag(flag, empire);
        return CommandResponse.DONE;
    }

    @Command(
            name = "whitelist",
            permission = "Empires.adm.cmd.perm.empire.whitelist",
            parentName = "Empires.adm.cmd.perm.empire",
            syntax = "/empireadmin perm empire whitelist <empire>",
            completionKeys = {"empireCompletion"})
    public static CommandResponse permEmpireWhitelistCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1)
            return CommandResponse.SEND_SYNTAX;

        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        ToolManager.instance.register(new WhitelisterTool(res));
        return CommandResponse.DONE;
    }

    @Command(
            name = "wild",
            permission = "Empires.adm.cmd.perm.wild",
            parentName = "Empires.adm.cmd.perm",
            syntax = "/empireadmin perm wild <command>",
            console = true)
    public static CommandResponse permWildCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "list",
            permission = "Empires.adm.cmd.perm.wild.list",
            parentName = "Empires.adm.cmd.perm.wild",
            syntax = "/empireadmin perm wild list",
            completionKeys = {"flagCompletion"},
            console = true)
    public static CommandResponse permWildListCommand(ICommandSender sender, List<String> args) {
        ChatManager.send(sender, Wild.instance.flagsContainer.toChatMessage());
        return CommandResponse.DONE;
    }

    @Command(
            name = "set",
            permission = "Empires.adm.cmd.perm.wild.set",
            parentName = "Empires.adm.cmd.perm.wild",
            syntax = "/empireadmin perm wild set <flag> <value>",
            completionKeys = {"flagCompletion"},
            console = true)
    public static CommandResponse permWildSetCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 2) {
            return CommandResponse.SEND_SYNTAX;
        }

        FlagType type = getFlagTypeFromName(args.get(0));
        Flag flag = getFlagFromType(Wild.instance.flagsContainer, type);

        if (flag.setValue(args.get(1))) {
            ChatManager.send(sender, "Empires.notification.perm.success");
        } else {
            throw new EmpiresCommandException("Empires.cmd.err.perm.valueNotValid");
        }

        Empires.instance.getWildConfig().write(Wild.instance.flagsContainer);
        return CommandResponse.DONE;
    }

    @Command(
            name = "toggle",
            permission = "Empires.adm.cmd.perm.wild.toggle",
            parentName = "Empires.adm.cmd.perm.wild",
            syntax = "/empireadmin perm wild toggle <flag>",
            completionKeys = {"flagCompletion"},
            console = true)
    public static CommandResponse permWildToggleCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1) {
            return CommandResponse.SEND_SYNTAX;
        }

        FlagType type = getFlagTypeFromName(args.get(0));
        Flag flag = getFlagFromType(Wild.instance.flagsContainer, type);

        if (flag.toggle()) {
            ChatManager.send(sender, "Empires.notification.perm.success");
        } else {
            throw new EmpiresCommandException("Empires.cmd.err.perm.valueNotValid");
        }

        Empires.instance.getWildConfig().write(Wild.instance.flagsContainer);
        return CommandResponse.DONE;
    }

    @Command(
            name = "claim",
            permission = "Empires.adm.cmd.claim",
            parentName = "Empires.adm.cmd",
            syntax = "/empireadmin claim <empire> [range]",
            completionKeys = {"empireCompletion"})
    public static CommandResponse claimCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1) {
            return CommandResponse.SEND_SYNTAX;
        }

        EntityPlayer player = (EntityPlayer) sender;
        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(player);
        Empire empire = getEmpireFromName(args.get(0));

        boolean isFarClaim = false;

        if(args.size() < 2) {

            if (empire.empireBlocksContainer.size() >= empire.getMaxBlocks()) {
                throw new EmpiresCommandException("Empires.cmd.err.empire.maxBlocks", 1);
            }
            if (getUniverse().blocks.contains(player.dimension, player.chunkCoordX, player.chunkCoordZ)) {
                throw new EmpiresCommandException("Empires.cmd.err.claim.already");
            }
            if (!CommandsOfficer.checkNearby(player.dimension, player.chunkCoordX, player.chunkCoordZ, empire)) { // Checks if the player can claim far
                ChatManager.send(sender, "Empires.adm.cmd.far.claim");
                isFarClaim = true;
            }
            EmpireBlock block = getUniverse().newBlock(player.dimension, player.chunkCoordX, player.chunkCoordZ, isFarClaim, 0, empire);
            if (block == null) {
                throw new EmpiresCommandException("Empires.cmd.err.claim.failed");
            }
            getDatasource().saveBlock(block);
            ChatManager.send(sender, "Empires.notification.block.added", block.getX() * 16, block.getZ() * 16, block.getX() * 16 + 15, block.getZ() * 16 + 15, empire);
        } else {

            checkPositiveInteger(args.get(1));

            int radius = Integer.parseInt(args.get(1));
            List<ChunkPos> chunks = WorldUtils.getChunksInBox(player.dimension, (int) (player.posX - radius * 16), (int) (player.posZ - radius * 16), (int) (player.posX + radius * 16), (int) (player.posZ + radius * 16));
            isFarClaim = true;
            for(Iterator<ChunkPos> it = chunks.iterator(); it.hasNext();) {
                ChunkPos chunk = it.next();
                if(CommandsOfficer.checkNearby(player.dimension, chunk.getX(), chunk.getZ(), empire)) {
                    isFarClaim = false;
                }
                if (getUniverse().blocks.contains(player.dimension, chunk.getX(), chunk.getZ()))
                    it.remove();
            }
            if(isFarClaim) {
                ChatManager.send(sender, "Empires.adm.cmd.far.claim");
            }
            if (empire.empireBlocksContainer.size() + chunks.size() > empire.getMaxBlocks()) {
                throw new EmpiresCommandException("Empires.cmd.err.empire.maxBlocks", chunks.size());
            }

            for(ChunkPos chunk : chunks) {
                EmpireBlock block = getUniverse().newBlock(player.dimension, chunk.getX(), chunk.getZ(), isFarClaim, 0, empire);
                // Just so that only one of the blocks will be marked as far claim.
                isFarClaim = false;
                getDatasource().saveBlock(block);
                ChatManager.send(sender, "Empires.notification.block.added", block.getX() * 16, block.getZ() * 16, block.getX() * 16 + 15, block.getZ() * 16 + 15, empire);
            }
        }
        return CommandResponse.DONE;
    }

    @Command(
            name = "chunkload",
            permission = "Empires.adm.cmd.claim.chunkload",
            parentName = "Empires.adm.cmd.claim",
            syntax = "/empireadmin claim chunkload")
    public static CommandResponse claimChunkloadCommand(ICommandSender sender, List<String> args) {

        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        EmpireBlock block = getBlockAtCitizen(res);

        if (block.isChunkloaded()) {
            throw new EmpiresCommandException("Empires.cmd.err.claim.chunkload.already");
        }

        block.getEmpire().ticketMap.chunkLoad(block);
        ChatManager.send(sender, "Empires.notification.claim.chunkload");

        return CommandResponse.DONE;
    }

    @Command(
            name = "chunkunload",
            permission = "Empires.adm.cmd.claim.chunkunload",
            parentName = "Empires.adm.cmd.claim",
            syntax = "/empireadmin claim chunkunload")
    public static CommandResponse claimUnchunkloadCommand(ICommandSender sender, List<String> args) {

        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        EmpireBlock block = getBlockAtCitizen(res);

        if (!block.isChunkloaded()) {
            throw new EmpiresCommandException("Empires.cmd.err.claim.unchunkload.missing");
        }

        block.getEmpire().ticketMap.chunkUnload(block);
        ChatManager.send(sender, "Empires.notification.claim.chunkunload");

        return CommandResponse.DONE;
    }

    @Command(
            name = "all",
            permission = "Empires.adm.cmd.claim.chunkload.all",
            parentName = "Empires.adm.cmd.claim.chunkload",
            syntax = "/empireadmin claim chunkload all <empire>",
            completionKeys = {"empireCompletion"})
    public static CommandResponse claimChunkloadAllCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1) {
            return CommandResponse.SEND_SYNTAX;
        }

        Empire empire = getEmpireFromName(args.get(0));
        int chunksToLoad = empire.empireBlocksContainer.size() - empire.ticketMap.getChunkloadedAmount();

        empire.ticketMap.chunkLoadAll();
        ChatManager.send(sender, "Empires.notification.claim.chunkload.all", chunksToLoad);

        return CommandResponse.DONE;
    }

    @Command(
            name = "all",
            permission = "Empires.adm.cmd.claim.chunkunload.all",
            parentName = "Empires.adm.cmd.claim.chunkunload",
            syntax = "/empireadmin claim chunkunload all <empire>",
            completionKeys = {"empireCompletion"})
    public static CommandResponse claimChunkunloadAllCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1) {
            return CommandResponse.SEND_SYNTAX;
        }

        Empire empire = getEmpireFromName(args.get(0));
        int chunkToUnload = empire.ticketMap.getChunkloadedAmount();

        empire.ticketMap.chunkUnloadAll();
        ChatManager.send(sender, "Empires.notification.claim.chunkload.all", chunkToUnload);

        return CommandResponse.DONE;
    }

    @Command(
            name = "unclaim",
            permission = "Empires.adm.cmd.unclaim",
            parentName = "Empires.adm.cmd",
            syntax = "/empireadmin unclaim")
    public static CommandResponse unclaimCommand(ICommandSender sender, List<String> args) {
        EntityPlayer pl = (EntityPlayer) sender;
        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(pl);
        EmpireBlock block = getBlockAtCitizen(res);
        Empire empire = block.getEmpire();

        if (block.isPointIn(empire.getSpawn().getDim(), empire.getSpawn().getX(), empire.getSpawn().getZ())) {
            throw new EmpiresCommandException("Empires.cmd.err.unclaim.spawnPoint");
        }

        getDatasource().deleteBlock(block);
        ChatManager.send(sender, "Empires.notification.block.removed", block.getX() << 4, block.getZ() << 4, (block.getX() << 4) + 15, (block.getZ() << 4) + 15, empire);
        return CommandResponse.DONE;
    }

    @Command(
            name = "help",
            permission = "Empires.adm.cmd.help",
            parentName = "Empires.adm.cmd",
            syntax = "/empireadmin help <command>",
            alias = {"?", "h"},
            console = true)
    public static CommandResponse helpCommand(ICommandSender sender, List<String> args) {
        int page = 1;
        if(!args.isEmpty() && StringUtils.tryParseInt(args.get(0)) && Integer.parseInt(args.get(0)) > 0) {
            page = Integer.parseInt(args.get(0));
            args = args.subList(1, args.size());
        }

        CommandTree tree = CommandManager.getTree("Empires.adm.cmd");
        CommandTreeNode node = tree.getNodeFromArgs(args);
        node.sendHelpMessage(sender, page);
        return CommandResponse.DONE;
    }

    @Command(
            name = "syntax",
            permission = "Empires.adm.cmd.syntax",
            parentName = "Empires.adm.cmd",
            syntax = "/empireadmin syntax <command>",
            console = true)
    public static CommandResponse syntaxCommand(ICommandSender sender, List<String> args) {
        CommandTree tree = CommandManager.getTree("Empires.adm.cmd");
        CommandTreeNode node = tree.getNodeFromArgs(args);
        node.sendSyntax(sender);
        return CommandResponse.DONE;
    }

    @Command(
            name = "debug",
            permission = "Empires.adm.cmd.debug",
            parentName = "Empires.adm.cmd",
            syntax = "/empireadmin debug <command>",
            console = false)
    public static CommandResponse debugCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "itemClass",
            permission = "Empires.adm.cmd.debug.item",
            parentName = "Empires.adm.cmd.debug",
            syntax = "/empireadmin debug itemClass",
            console = false)
    public static CommandResponse debugItemCommand(ICommandSender sender, List<String> args) {
        if(sender instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)sender;
            List<Class> list = new ArrayList<Class>();
            if(player.inventory.getCurrentItem() != null) {

                if(player.inventory.getCurrentItem().getItem() instanceof ItemBlock) {
                    Block block = ((ItemBlock)player.inventory.getCurrentItem().getItem()).block;
                    list.add(block.getClass());
                    if(block instanceof ITileEntityProvider) {
                    	TileEntity te = ((ITileEntityProvider) block).createNewTileEntity(MinecraftServer.getServer().worldServerForDimension(0), 0);
                        list.add(te == null ? TileEntity.class : te.getClass());
                    }
                } else {
                    list.add(player.inventory.getCurrentItem().getItem().getClass());
                }

                ChatManager.send(sender, new ChatComponentText("For item: " + player.inventory.getCurrentItem().getDisplayName()));
                for(Class cls : list) {
                    while (cls != Object.class) {
                        ChatManager.send(sender, new ChatComponentText(cls.getName()));
                        cls = cls.getSuperclass();
                    }
                }
            }
        }
        return CommandResponse.DONE;
    }

    public static class Plots {
        @Command(
                name = "plot",
                permission = "Empires.adm.cmd.plot",
                parentName = "Empires.adm.cmd",
                syntax = "/empireadmin plot <command>",
                console = true)
        public static CommandResponse plotCommand(ICommandSender sender, List<String> args) {
            return CommandResponse.SEND_HELP_MESSAGE;
        }

        @Command(
                name = "show",
                permission = "Empires.adm.cmd.plot.show",
                parentName = "Empires.adm.cmd.plot",
                syntax = "/empireadmin plot show <empire>",
                completionKeys = {"empireCompletion"})
        public static CommandResponse plotShowCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 1)
                return CommandResponse.SEND_SYNTAX;

            Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
            Empire empire = getEmpireFromName(args.get(0));
            empire.plotsContainer.show(res);
            ChatManager.send(sender, "Empires.notification.plot.showing");
            return CommandResponse.DONE;
        }

        @Command(
                name = "perm",
                permission = "Empires.adm.cmd.plot.perm",
                parentName = "Empires.adm.cmd.plot",
                syntax = "/empireadmin plot perm <command>",
                console = true)
        public static CommandResponse plotPermCommand(ICommandSender sender, List<String> args) {
            return CommandResponse.SEND_HELP_MESSAGE;
        }

        @Command(
                name = "set",
                permission = "Empires.adm.cmd.plot.perm.set",
                parentName = "Empires.adm.cmd.plot.perm",
                syntax = "/empireadmin plot perm set <empire> <plot> <flag> <value>",
                completionKeys = {"empireCompletion", "plotCompletion", "flagCompletion"},
                console = true)
        public static CommandResponse plotPermSetCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 4)
                return CommandResponse.SEND_SYNTAX;

            Empire empire = getEmpireFromName(args.get(0));
            Plot plot = getPlotFromName(empire, args.get(1));
            Flag flag = getFlagFromName(plot.flagsContainer, args.get(2));

            if (flag.setValue(args.get(3))) {
                ChatManager.send(sender, "Empires.notification.empire.perm.success");
            } else {
                throw new EmpiresCommandException("Empires.cmd.err.perm.valueNotValid");
            }

            getDatasource().saveFlag(flag, plot);
            return CommandResponse.DONE;
        }

        @Command(
                name = "toggle",
                permission = "Empires.adm.cmd.plot.perm.toggle",
                parentName = "Empires.adm.cmd.plot.perm",
                syntax = "/empireadmin plot perm toggle <empire> <plot> <flag>",
                completionKeys = {"empireCompletion", "plotCompletion", "flagCompletion"},
                console = true)
        public static CommandResponse plotPermToggleCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 3)
                return CommandResponse.SEND_SYNTAX;

            Empire empire = getEmpireFromName(args.get(0));
            Plot plot = getPlotFromName(empire, args.get(1));
            Flag flag = getFlagFromName(plot.flagsContainer, args.get(2));

            if (flag.toggle()) {
                ChatManager.send(sender, "Empires.notification.empire.perm.success");
            } else {
                throw new EmpiresCommandException("Empires.cmd.err.perm.valueNotValid");
            }

            getDatasource().saveFlag(flag, plot);
            return CommandResponse.DONE;
        }

        @Command(
                name = "list",
                permission = "Empires.adm.cmd.plot.perm.list",
                parentName = "Empires.adm.cmd.plot.perm",
                syntax = "/empireadmin plot perm list <empire> <plot>",
                completionKeys = {"empireCompletion", "plotCompletion"},
                console = true)
        public static CommandResponse plotPermListCommand(ICommandSender sender, List<String> args) {
            if(args.size() < 2)
                return CommandResponse.SEND_SYNTAX;

            Empire empire = getEmpireFromName(args.get(0));
            Plot plot = getPlotFromName(empire, args.get(1));
            ChatManager.send(sender, plot.flagsContainer.toChatMessage());
            return CommandResponse.DONE;
        }

        @Command(
                name = "rename",
                permission = "Empires.adm.cmd.plot.rename",
                parentName = "Empires.adm.cmd.plot",
                syntax = "/empireadmin plot rename <empire> <plot> <name>",
                completionKeys = {"empireCompletion", "plotCompletion"},
                console = true)
        public static CommandResponse plotRenameCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 3)
                return CommandResponse.SEND_SYNTAX;

            Empire empire = getEmpireFromName(args.get(0));
            Plot plot = getPlotFromName(empire, args.get(1));

            plot.setName(args.get(2));
            getDatasource().savePlot(plot);

            ChatManager.send(sender, "Empires.notification.plot.renamed");
            return CommandResponse.DONE;
        }

        @Command(
                name = "add",
                permission = "Empires.adm.cmd.plot.add",
                parentName = "Empires.adm.cmd.plot",
                syntax = "/empireadmin plot add <command>",
                console = true)
        public static CommandResponse plotAddCommand(ICommandSender sender, List<String> args) {
            return CommandResponse.SEND_HELP_MESSAGE;
        }

        @Command(
                name = "owner",
                permission = "Empires.adm.cmd.plot.add.owner",
                parentName = "Empires.adm.cmd.plot.add",
                syntax = "/empireadmin plot add owner <empire> <plot> <citizen>",
                completionKeys = {"empireCompletion", "plotCompletion", "citizenCompletion"},
                console = true)
        public static CommandResponse plotAddOwnerCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 3) {
                return CommandResponse.SEND_SYNTAX;
            }

            Citizen target = getCitizenFromName(args.get(2));

            Empire empire = getEmpireFromName(args.get(0));
            if (!target.empiresContainer.contains(empire)) {
                throw new EmpiresCommandException("Empires.cmd.err.citizen.notInEmpire", target);
            }

            Plot plot = getPlotFromName(empire, args.get(1));

            if(plot.membersContainer.contains(target) || plot.ownersContainer.contains(target)) {
                throw new EmpiresCommandException("Empires.cmd.err.plot.add.alreadyInPlot");
            }

            if (!empire.plotsContainer.canCitizenMakePlot(target)) {
                throw new EmpiresCommandException("Empires.cmd.err.plot.limit.toPlayer", target);
            }

            getDatasource().linkCitizenToPlot(target, plot, true);

            ChatManager.send(sender, "Empires.notification.plot.owner.sender.added", target, plot);
            ChatManager.send(target.getPlayer(), "Empires.notification.plot.owner.target.added", plot);
            return CommandResponse.DONE;
        }

        @Command(
                name = "member",
                permission = "Empires.adm.cmd.plot.add.member",
                parentName = "Empires.adm.cmd.plot.add",
                syntax = "/empireadmin plot add member <empire> <plot> <citizen>",
                completionKeys = {"empireCompletion", "plotCompletion", "citizenCompletion"},
                console = true)
        public static CommandResponse plotAddMemberCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 3) {
                return CommandResponse.SEND_SYNTAX;
            }

            Citizen target = getCitizenFromName(args.get(2));
            Empire empire = getEmpireFromName(args.get(0));
            Plot plot = getPlotFromName(empire, args.get(1));

            if(plot.membersContainer.contains(target) || plot.ownersContainer.contains(target)) {
                throw new EmpiresCommandException("Empires.cmd.err.plot.add.alreadyInPlot");
            }

            getDatasource().linkCitizenToPlot(target, plot, false);

            ChatManager.send(sender, "Empires.notification.plot.member.sender.added", target, plot);
            ChatManager.send(target.getPlayer(), "Empires.notification.plot.member.target.added", plot);
            return CommandResponse.DONE;
        }

        @Command(
                name = "remove",
                permission = "Empires.adm.cmd.plot.remove",
                parentName = "Empires.adm.cmd.plot",
                syntax = "/empireadmin plot remove <empire> <plot> <citizen>",
                completionKeys = {"empireCompletion", "plotCompletion", "citizenCompletion"},
                console = true)
        public static CommandResponse plotRemoveCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 3) {
                return CommandResponse.SEND_SYNTAX;
            }

            Citizen target = getCitizenFromName(args.get(2));
            Empire empire = getEmpireFromName(args.get(0));
            Plot plot = getPlotFromName(empire, args.get(1));

            if(!plot.membersContainer.contains(target) && !plot.ownersContainer.contains(target)) {
                throw new EmpiresCommandException("Empires.cmd.err.plot.remove.notInPlot");
            }

            getDatasource().unlinkCitizenFromPlot(target, plot);

            ChatManager.send(sender, "Empires.notification.plot.sender.removed", target, plot);
            ChatManager.send(target.getPlayer(), "Empires.notification.plot.target.removed", plot);
            return CommandResponse.DONE;
        }

        @Command(
                name = "info",
                permission = "Empires.adm.cmd.plot.info",
                parentName = "Empires.adm.cmd.plot",
                syntax = "/empireadmin plot info <empire> <plot>",
                completionKeys = {"empireCompletion", "plotCompletion"},
                console = true)
        public static CommandResponse plotInfoCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 2) {
                return CommandResponse.SEND_SYNTAX;
            }

            Empire empire = getEmpireFromName(args.get(0));
            Plot plot = getPlotFromName(empire, args.get(1));

            IChatComponent header = LocalizationManager.get("Empires.format.list.header", new ChatComponentFormatted("{9|%s}", plot.getName()));
            ChatManager.send(sender, "Empires.format.plot.long", plot.ownersContainer, plot.toVolume().toChatMessage());
            return CommandResponse.DONE;
        }

        @Command(
                name = "delete",
                permission = "Empires.adm.cmd.plot.delete",
                parentName = "Empires.adm.cmd.plot",
                syntax = "/empireadmin plot delete <empire> <plot>",
                completionKeys = {"empireCompletion", "plotCompletion"},
                console = true)
        public static CommandResponse plotDeleteCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 2) {
                return CommandResponse.SEND_SYNTAX;
            }

            Empire empire = getEmpireFromName(args.get(0));
            Plot plot = getPlotFromName(empire, args.get(1));
            getDatasource().deletePlot(plot);
            ChatManager.send(sender, "Empires.notification.plot.deleted", plot);
            return CommandResponse.DONE;
        }

        @Command(
                name = "hide",
                permission = "Empires.adm.cmd.plot.hide",
                parentName = "Empires.adm.cmd.plot",
                syntax = "/empireadmin plot hide")
        public static CommandResponse plotHideCommand(ICommandSender sender, List<String> args) {
            if(sender instanceof EntityPlayerMP) {
                VisualsHandler.instance.unmarkPlots((EntityPlayerMP) sender);
                ChatManager.send(sender, "Empires.notification.plot.vanished");
            }
            return CommandResponse.DONE;
        }
    }

    @Command(
            name = "borders",
            permission = "Empires.adm.cmd.borders",
            parentName = "Empires.adm.cmd",
            syntax = "/empireadmin borders <command>")
    public static CommandResponse bordersCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "show",
            permission = "Empires.adm.cmd.borders.show",
            parentName = "Empires.adm.cmd.borders",
            syntax = "/empireadmin borders show <empire>",
            completionKeys = {"empireCompletion"})
    public static CommandResponse bordersShowCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 1)
            return CommandResponse.SEND_SYNTAX;
        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        Empire empire = getEmpireFromName(args.get(0));
        empire.empireBlocksContainer.show(res);
        ChatManager.send(sender, "Empires.notification.empire.borders.show", empire);
        return CommandResponse.DONE;
    }

    @Command(
            name = "hide",
            permission = "Empires.adm.cmd.borders.hide",
            parentName = "Empires.adm.cmd.borders",
            syntax = "/empireadmin borders hide")
    public static CommandResponse bordersHideCommand(ICommandSender sender, List<String> args) {
        if(sender instanceof EntityPlayerMP) {
            VisualsHandler.instance.unmarkEmpires((EntityPlayerMP)sender);
            ChatManager.send(sender, "Empires.notification.empire.borders.hide");
        }
        return CommandResponse.DONE;
    }

    @Command(
            name = "rename",
            permission = "Empires.adm.cmd.rename",
            parentName = "Empires.adm.cmd",
            syntax = "/empireadmin rename <empire> <name>",
            completionKeys = {"empireCompletion"},
            console = true)
    public static CommandResponse renameCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 2) {
            return CommandResponse.SEND_SYNTAX;
        }

        Empire empire = getEmpireFromName(args.get(0));

        if (getUniverse().empires.contains(args.get(1))) {
            throw new EmpiresCommandException("Empires.cmd.err.new.nameUsed", args.get(1));
        }

        empire.rename(args.get(1));
        getDatasource().saveEmpire(empire);
        ChatManager.send(sender, "Empires.notification.empire.renamed");
        return CommandResponse.DONE;
    }

    @Command(
            name = "spawn",
            permission = "Empires.adm.cmd.spawn",
            parentName = "Empires.adm.cmd",
            syntax = "/empireadmin spawn <empire>",
            completionKeys = {"empireCompletion"},
            console = true)
    public static CommandResponse spawnCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 1) {
            return CommandResponse.SEND_SYNTAX;
        }

        Citizen res = getUniverse().getOrMakeCitizen(sender);
        Empire empire = getEmpireFromName(args.get(0));
        empire.sendToSpawn(res);
        return CommandResponse.DONE;
    }
}