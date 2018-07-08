package EmpiresMod.commands.Neutral;

import java.util.ArrayList;
import java.util.List;

import EmpiresMod.API.Chat.Component.ChatComponentBorders;
import EmpiresMod.API.Chat.Component.ChatComponentEmpireList;
import EmpiresMod.API.Chat.Component.ChatComponentFormatted;
import EmpiresMod.API.Chat.Component.ChatComponentMultiPage;
import EmpiresMod.API.Chat.Component.ChatManager;
import EmpiresMod.API.Commands.Command.Command;
import EmpiresMod.API.Commands.Command.CommandManager;
import EmpiresMod.API.Commands.Command.CommandResponse;
import EmpiresMod.API.Commands.Command.CommandsEMP;
import EmpiresMod.API.permissions.CommandTree;
import EmpiresMod.API.permissions.CommandTreeNode;
import EmpiresMod.Configuration.Config;
import EmpiresMod.Datasource.EmpiresUniverse;
import EmpiresMod.Localization.LocalizationManager;
import EmpiresMod.Proxies.EconomyProxy;
import EmpiresMod.Utilities.EmpireUtils;
import EmpiresMod.Utilities.Formatter;
import EmpiresMod.Utilities.StringUtils;
import EmpiresMod.entities.Empire.Citizen;
import EmpiresMod.entities.Empire.Empire;
import EmpiresMod.entities.Empire.Relationship;
import EmpiresMod.entities.Empire.Relationship.Type;
import EmpiresMod.entities.Flags.FlagType;
import EmpiresMod.exceptions.Empires.EmpiresCommandException;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IChatComponent;

/**
 * All commands that can be accessed by everyone whether or not he's in a empire
 */
public class CommandsNeutral extends CommandsEMP {
    @Command(
            name = "info",
            permission = "Empires.cmd.outsider.info",
            parentName = "Empires.cmd",
            syntax = "/empire info [empire]",
            completionKeys = {"empireCompletionAndAll"},
            console = true)
    public static CommandResponse infoCommand(ICommandSender sender, List<String> args) {
        List<Empire> empires = new ArrayList<Empire>();

        if (args.size() < 1) {
            if (sender instanceof EntityPlayer) {
                Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
                empires.add(getEmpireFromCitizen(res));
            } else {
                throw new EmpiresCommandException("Empires.cmd.err.notPlayer");
            }
        } else {
            if ("@a".equals(args.get(0))) {
                empires = new ArrayList<Empire>(getUniverse().empires);
                // TODO Sort
            } else {
                if(getEmpireFromName(args.get(0)) != null) {
                    empires.add(getEmpireFromName(args.get(0)));
                }
            }
        }   

        for (Empire empire : empires) {
            
        	IChatComponent header = LocalizationManager.get("Empires.format.list.header", new ChatComponentFormatted("{6|%s}", ChatComponentBorders.borderEditor(empire.getName())));
        	

        	Relationship.Type ally = getRelationTypeFromEmpire(empire, "ally");
        	Relationship.Type truce = getRelationTypeFromEmpire(empire, "truce");
        	Relationship.Type enemy = getRelationTypeFromEmpire(empire, "enemy");
        	String allyEmpires = empire.getRelation(ally);
        	String truceEmpires = empire.getRelation(truce);
        	String enemyEmpires = empire.getRelation(enemy);
        	String description = empire.getDesc();

           ChatManager.send(sender, "Empires.format.empire.long", header, description, empire.citizensMap.size(), empire.empireBlocksContainer.size(), empire.getMaxBlocks(), empire.getPower(), empire.getMaxPower(), allyEmpires, truceEmpires, enemyEmpires, empire.citizensMap, empire.ranksContainer);
           //System.out.println(sender + "Empires.format.empire.long" + header + empire.citizensMap.size() + allyEmpires + truceEmpires + enemyEmpires + empire.empireBlocksContainer.size() + empire.getMaxBlocks() + empire.getPower() + empire.getMaxPower() + empire.citizensMap + empire.ranksContainer);
        	 }
        return CommandResponse.DONE;
}

    @Command(
            name = "citizen",
            permission = "Empires.cmd.outsider.res",
            parentName = "Empires.cmd",
            syntax = "/empire citizen <citizen>",
            completionKeys = {"citizenCompletion"},
            alias = {"ciz", "Citizen"},
            console = true)
    public static CommandResponse resCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1) {
        	Citizen res1 = getCitizenFromName(sender.getName());
        	  IChatComponent header = LocalizationManager.get("Empires.format.list.header", new ChatComponentFormatted("{6|%s}", ChatComponentBorders.borderEditor(res1.getPlayerName())));
        	 ChatManager.send(sender, "Empires.format.citizen.long", header, res1.empiresContainer, Formatter.formatDate(res1.getJoinDate()), Formatter.formatDate(res1.getLastOnline()), res1.getExtraBlocks(), res1.getPower(), res1.getMaxPower());
        	 return CommandResponse.DONE;
        }

        Citizen res = getCitizenFromName(args.get(0));
        if (res == null) {
            throw new EmpiresCommandException("Empires.cmd.err.citizen.missing", args.get(0));
        }
        IChatComponent header = LocalizationManager.get("Empires.format.list.header", new ChatComponentFormatted("{6|%s}", ChatComponentBorders.borderEditor(res.getPlayerName())));
        ChatManager.send(sender, "Empires.format.citizen.long", header, res.empiresContainer, Formatter.formatDate(res.getJoinDate()), Formatter.formatDate(res.getLastOnline()), res.getExtraBlocks(), res.getPower(), res.getMaxPower());
        return CommandResponse.DONE;
    }

    @Command(
            name = "list",
            permission = "Empires.cmd.outsider.list",
            parentName = "Empires.cmd",
            syntax = "/empire list [page]",
            console = true)
    public static CommandResponse listCommand(ICommandSender sender, List<String> args) {
        int page = 1;
        if (args.size() >= 1) {
            page = Integer.parseInt(args.get(0));
        }
        if (page <= 0) {
            page = 1;
        }

        // TODO: Cache this
        ChatComponentMultiPage empireList = new ChatComponentEmpireList(getUniverse().empires);
        empireList.sendPage(sender, page);

        return CommandResponse.DONE;
    }

    @Command(
            name = "create",
            permission = "Empires.cmd.outsider.new",
            parentName = "Empires.cmd",
            syntax = "/empire create <name>")
    public static CommandResponse newEmpireCommand(ICommandSender sender, List<String> args) throws CommandException {
        if (args.size() < 1) {
            return CommandResponse.SEND_SYNTAX;
        }

        EntityPlayer player = (EntityPlayer) sender;
        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender); // Attempt to get or make the Citizen

        ChatManager.send(sender, "Empires.notification.empire.startedCreation", args.get(0));

        if (res.empiresContainer.size() >= 1) {
            throw new EmpiresCommandException("Empires.cmd.err.citizen.maxEmpires");
        }
        if (getUniverse().empires.contains(args.get(0))) {
            throw new EmpiresCommandException("Empires.cmd.err.new.nameUsed", args.get(0));
        }
        if (getUniverse().blocks.contains(player.dimension, (int) player.posX >> 4, (int) player.posZ >> 4)) {
            throw new EmpiresCommandException("Empires.cmd.err.new.position");
        }
        for (int x = ((int) player.posX >> 4) - Config.instance.distanceBetweenEmpires.get(); x <= ((int) player.posX >> 4) + Config.instance.distanceBetweenEmpires.get(); x++) {
            for (int z = ((int) player.posZ >> 4) - Config.instance.distanceBetweenEmpires.get(); z <= ((int) player.posZ >> 4) + Config.instance.distanceBetweenEmpires.get(); z++) {
                Empire nearbyEmpire = EmpireUtils.getEmpireAtPosition(player.dimension, x, z);
                if (nearbyEmpire != null && !nearbyEmpire.flagsContainer.getValue(FlagType.NEARBY)) {
                    throw new EmpiresCommandException("Empires.cmd.err.new.tooClose", nearbyEmpire, Config.instance.distanceBetweenEmpires.get());
                }
            }
        }
        
        //Need to insert an If statement that blocks the use of spaces in empire names ie: Republic of LALA has 2 spaces, hence unsearchable.
        
        if (args.get(0).length() > Config.instance.empireNameMaxChars.get()) {
            throw new EmpiresCommandException("Empires.cmd.err.new.nameTooLong");
        }

       
        
        makePayment(player, Config.instance.costAmountMakeEmpire.get() + Config.instance.costAmountClaim.get());

        Empire empire = getUniverse().newEmpire(args.get(0), res); // Attempt to create the Empire
        empire.setPower(res.getPower()); 
        empire.setMaxPower(res.getMaxPower());
		 Relationship ally = new Relationship("ally", empire, Relationship.Type.ALLY);
		 Relationship truce = new Relationship("truce", empire, Relationship.Type.TRUCE);
		 Relationship enemy = new Relationship("enemy", empire, Relationship.Type.ENEMY);
    	empire.relationContainer.add(ally);
		empire.relationContainer.add(truce);
		empire.relationContainer.add(enemy);
		
        Relationship rel1 = getRelationFromEmpire(empire, "ally");
        Relationship rel2 = getRelationFromEmpire(empire, "truce");
        Relationship rel3 = getRelationFromEmpire(empire, "enemy");
         empire.setRelation(empire, rel1);
         empire.setRelation(empire, rel2);
         empire.setRelation(empire, rel3);
        if (empire == null) {
            throw new EmpiresCommandException("Empires.cmd.err.new.failed");
        }
        
        ChatManager.send(sender, "Empires.notification.empire.created", empire);
        return CommandResponse.DONE;
    }

    @Command(
            name = "map",
            permission = "Empires.cmd.outsider.map",
            parentName = "Empires.cmd",
            syntax = "/empire map [on|off]")
    public static CommandResponse mapCommand(ICommandSender sender, List<String> args) {
        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        if (args.size() == 0) {
            Formatter.sendMap(res);
        } else {
            //res.setMapOn(args.get(0).equals("on"));
        }
        return CommandResponse.DONE;
    }

    @Command(
            name = "accept",
            permission = "Empires.cmd.outsider.accept",
            parentName = "Empires.cmd",
            syntax = "/empire accept [empire]",
            completionKeys = {"empireCompletion"})
    public static CommandResponse acceptCommand(ICommandSender sender, List<String> args) {
        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        List<Empire> invites = getInvitesFromCitizen(res);
        Empire empire;
        if (args.size() == 0) {
            if(invites.size() > 1) {
                throw new EmpiresCommandException("Empires.cmd.err.invite.accept");
            }
            empire = invites.get(0);
        } else {
            empire = getEmpireFromName(args.get(0));
            // Basically true only if player specifies a empire that is not in its invites
            if (!invites.contains(empire)) {
                throw new EmpiresCommandException("Empires.cmd.err.invite.missing");
            }
            
        }
        if (res.empiresContainer.size() >= 1) {
            throw new EmpiresCommandException("Empires.cmd.err.citizen.maxEmpires");
        }
        
        if (res.empireBansContainer.contains(empire)) {
        	throw new EmpiresCommandException("Empires.cmd.errr.invite.banned");
        }
        
        getDatasource().deleteEmpireInvite(res, empire, true);
        
        empire.addPower(res.getPower()); 
        empire.addMaxPower(res.getMaxPower());

        // Notify everyone
        ChatManager.send(sender, "Empires.notification.empire.invited.accept", empire);
        empire.notifyEveryone(LocalizationManager.get("Empires.notification.empire.joined", res, empire));
        return CommandResponse.DONE;
        
        
        
        
    }

    @Command(
            name = "refuse",
            permission = "Empires.cmd.outsider.refuse",
            parentName = "Empires.cmd",
            syntax = "/empire refuse [empire]",
            completionKeys = {"empireCompletion"})
    public static CommandResponse refuseCommand(ICommandSender sender, List<String> args) {
        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        List<Empire> invites = getInvitesFromCitizen(res);
        Empire empire;
        if(invites.size() > 1) {
            throw new EmpiresCommandException("Empires.cmd.err.invite.refuse");
        }

        if (args.size() == 0) {
            empire = invites.get(0);
        } else {
            empire = getEmpireFromName(args.get(0));
        }
        if (!invites.contains(empire)) {
            throw new EmpiresCommandException("Empires.cmd.err.invite.missing");
        }

        getDatasource().deleteEmpireInvite(res, empire, false);

        ChatManager.send(sender, "Empires.notification.empire.invited.refuse", empire);
        return CommandResponse.DONE;
    }


    @Command(
            name = "help",
            permission = "Empires.cmd.outsider.help",
            parentName = "Empires.cmd",
            syntax = "/empire help <command>",
            alias = {"?", "h"},
            console = true)
    public static CommandResponse helpCommand(ICommandSender sender, List<String> args) {
        int page = 1;
        if(!args.isEmpty() && StringUtils.tryParseInt(args.get(0)) && Integer.parseInt(args.get(0)) > 0) {
            page = Integer.parseInt(args.get(0));
            args = args.subList(1, args.size());
        }

        CommandTree tree = CommandManager.getTree("Empires.cmd");
        CommandTreeNode node = tree.getNodeFromArgs(args);
        node.sendHelpMessage(sender, page);
        return CommandResponse.DONE;
    }

    @Command(
            name = "syntax",
            permission = "Empires.cmd.outsider.syntax",
            parentName = "Empires.cmd",
            syntax = "/empire syntax <command>",
            console = true)
    public static CommandResponse syntaxCommand(ICommandSender sender, List<String> args) {
        CommandTree tree = CommandManager.getTree("Empires.cmd");
        CommandTreeNode node = tree.getNodeFromArgs(args);
        node.sendSyntax(sender);
        return CommandResponse.DONE;
    }

    @Command(
            name = "invites",
            permission = "Empires.cmd.outsider.invites",
            parentName = "Empires.cmd",
            syntax = "/empire invites")
    public static CommandResponse invitesCommand(ICommandSender sender, List<String> args) {
        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        if (res.empireInvitesContainer.size() == 0) {
            ChatManager.send(sender, "Empires.cmd.err.invite.missing");
        } else {
            ChatManager.send(sender, res.empireInvitesContainer.toChatMessage());
        }
        return CommandResponse.DONE;
    }

    @Command(
            name = "prices",
            permission = "Empires.cmd.outsider.prices",
            parentName = "Empires.cmd",
            syntax = "/empire prices")
    public static CommandResponse pricesCommand(ICommandSender sender, List<String> args) {
        Citizen res = getUniverse().getOrMakeCitizen(sender);

        IChatComponent header = LocalizationManager.get("Empires.format.list.header", new ChatComponentFormatted("{9|PRICES}"));
        ChatManager.send(sender, "Empires.notification.prices",
                header,
                EconomyProxy.getCurrency(Config.instance.costAmountMakeEmpire.get()),
                EconomyProxy.getCurrency(Config.instance.costAmountClaim.get()),
                EconomyProxy.getCurrency(Config.instance.costAdditionClaim.get()),
                EconomyProxy.getCurrency(Config.instance.costAmountClaimFar.get()),
                EconomyProxy.getCurrency(Config.instance.costAmountSpawn.get()),
                EconomyProxy.getCurrency(Config.instance.costAmountSetSpawn.get()),
                EconomyProxy.getCurrency(Config.instance.costAmountOtherSpawn.get()),
                EconomyProxy.getCurrency(Config.instance.costEmpireUpkeep.get()),
                EconomyProxy.getCurrency(Config.instance.costAdditionalUpkeep.get()));

        return CommandResponse.DONE;
    }

}