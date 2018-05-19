package EmpiresMod.commands.Officer;

import java.util.Iterator;
import java.util.List;

import javax.management.relation.RelationType;

import EmpiresMod.API.Chat.Component.ChatManager;
import EmpiresMod.API.Commands.Command.Command;
import EmpiresMod.API.Commands.Command.CommandResponse;
import EmpiresMod.API.Commands.Command.CommandsEMP;
import EmpiresMod.API.container.relationshipMap;
import EmpiresMod.Configuration.Config;
import EmpiresMod.Datasource.EmpiresUniverse;
import EmpiresMod.Proxies.EconomyProxy;
import EmpiresMod.Utilities.EmpireUtils;
import EmpiresMod.Utilities.MathUtils;
import EmpiresMod.Utilities.WorldUtils;
import EmpiresMod.entities.Empire.AdminEmpire;
import EmpiresMod.entities.Empire.Citizen;
import EmpiresMod.entities.Empire.Empire;
import EmpiresMod.entities.Empire.EmpireBlock;
import EmpiresMod.entities.Empire.Plot;
import EmpiresMod.entities.Empire.Rank;
import EmpiresMod.entities.Empire.Relationship;
import EmpiresMod.entities.Empire.Relationship.Type;
import EmpiresMod.entities.Flags.Flag;
import EmpiresMod.entities.Flags.FlagType;
import EmpiresMod.entities.Managers.ToolManager;
import EmpiresMod.entities.Position.ChunkPos;
import EmpiresMod.entities.Tools.WhitelisterTool;
import EmpiresMod.exceptions.Empires.EmpiresCommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

/**
 * All commands that require the officer permission go here
 */
public class CommandsOfficer extends CommandsEMP {

    @Command(
            name = "setspawn",
            permission = "Empires.cmd.officer.setspawn",
            parentName = "Empires.cmd",
            syntax = "/empire setspawn")
    public static CommandResponse setSpawnCommand(ICommandSender sender, List<String> args) {
        EntityPlayer player = (EntityPlayer) sender;
        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(player);
        Empire empire = getEmpireFromCitizen(res);

        if (!empire.isPointInEmpire(player.dimension, (int) player.posX, (int) player.posZ)) {
            throw new EmpiresCommandException("Empires.cmd.err.setspawn.notInEmpire", empire);
        }

        makePayment(player, Config.instance.costAmountSetSpawn.get());

        empire.getSpawn().setDim(player.dimension).setPosition((float) player.posX, (float) player.posY, (float) player.posZ).setRotation(player.cameraYaw, player.cameraPitch);
        getDatasource().saveEmpire(empire);
        ChatManager.send(sender, "Empires.notification.empire.setspawn");
        return CommandResponse.DONE;
    }

   /*/ @Command(
            name = "setJail",
            permission = "Empires.cmd.officer.jail.set",
            parentName = "Empires.cmd",
            syntax = "/empire setJail")
    public static CommandResponse setJailCommand(ICommandSender sender, List<String> args) {
        EntityPlayer player = (EntityPlayer) sender;
        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(player);
        Empire empire = getEmpireFromCitizen(res);
        
        if (!empire.isPointInEmpire(player.dimension, (int) player.posX, (int) player.posZ)) {
            throw new EmpiresCommandException("Empires.cmd.err.setJail.notInEmpire", empire);
        }
        
        empire.getJail().setDim(player.dimension).setPosition((float) player.posX, (float) player.posY, (float) player.posZ).setRotation(player.cameraYaw, player.cameraPitch);
        //getDatasource().saveEmpire(empire);
        //Need to add db for jail.
        ChatManager.send(sender, "Empires.notification.empire.setJail");
        return CommandResponse.DONE;
    }
    
    /*/
    
    @Command(
            name = "setrelation",
            permission = "Empires.cmd.officer.relations.enemy.set",
            parentName = "Empires.cmd",
            syntax = "/empire setrelation <Empire> <enemy/ally/truce/neutral>")
    public static CommandResponse enemyCommand(ICommandSender sender, List<String> args) {
        EntityPlayer player = (EntityPlayer) sender;
        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(player);
        Empire empire = getEmpireFromCitizen(res);
        
        if (args.size() < 2) {
            return CommandResponse.SEND_SYNTAX;
        }

       Empire targetEmpire = getEmpireFromName(args.get(0));
       Relationship rel = getRelationFromEmpire(empire, args.get(1));
       
       if (empire.getName() == targetEmpire.getName()) {
    	  throw new EmpiresCommandException("Empires.cmd.err.relation.ownEmpire");
       }
       System.out.println(empire.getRelationship(Relationship.Type.ALLY));
       empire.setRelation(targetEmpire, rel);
   

        empire.notifyEveryone(getLocal().getLocalization("Empires.notification.empire.enemied", targetEmpire));
		return CommandResponse.DONE;
    }
    
    @Command(
            name = "claim",
            permission = "Empires.cmd.officer.claim",
            parentName = "Empires.cmd",
            syntax = "/empire claim [range]")
    public static CommandResponse claimCommand(ICommandSender sender, List<String> args) {
        EntityPlayer player = (EntityPlayer) sender;
        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(player);
        Empire empire = getEmpireFromCitizen(res);

        boolean isFarClaim = false;

        if (args.size() < 1) {
            if (empire.empireBlocksContainer.size() >= empire.getMaxBlocks()) {
                throw new EmpiresCommandException("Empires.cmd.err.empire.maxBlocks", 1);
            }
            
            if (empire.empireBlocksContainer.size() >= empire.getMaxPower()) {
            	 throw new EmpiresCommandException("Empires.cmd.err.empire.maxPower", 1);
            }
            
            if (getUniverse().blocks.contains(player.dimension, player.chunkCoordX, player.chunkCoordZ)) {
                throw new EmpiresCommandException("Empires.cmd.err.claim.already");
            }
            if (!checkNearby(player.dimension, player.chunkCoordX, player.chunkCoordZ, empire)) {
                if (empire.empireBlocksContainer.getFarClaims() >= empire.getMaxFarClaims()) {
                    throw new EmpiresCommandException("Empires.cmd.err.claim.far.notAllowed");
                }
                isFarClaim = true;
            }
            for (int x = player.chunkCoordX - Config.instance.distanceBetweenEmpires.get(); x <= player.chunkCoordX + Config.instance.distanceBetweenEmpires.get(); x++) {
                for (int z = player.chunkCoordZ - Config.instance.distanceBetweenEmpires.get(); z <= player.chunkCoordZ + Config.instance.distanceBetweenEmpires.get(); z++) {
                    Empire nearbyEmpire = EmpireUtils.getEmpireAtPosition(player.dimension, x, z);
                    if (nearbyEmpire != null && nearbyEmpire != empire && !nearbyEmpire.flagsContainer.getValue(FlagType.NEARBY)) {
                        throw new EmpiresCommandException("Empires.cmd.err.claim.tooClose", nearbyEmpire, Config.instance.distanceBetweenEmpires.get());
                    }
                }
            }

            if (isFarClaim && empire.empireBlocksContainer.getFarClaims() + 1 > empire.getMaxFarClaims()) {
                throw new EmpiresCommandException("Empires.cmd.err.claim.far.notAllowed");
            }

            int price = (isFarClaim ? Config.instance.costAmountClaimFar.get() : Config.instance.costAmountClaim.get()) + Config.instance.costAdditionClaim.get() * empire.empireBlocksContainer.size();

            makeBankPayment(player, empire, price);

            EmpireBlock block = getUniverse().newBlock(player.dimension, player.chunkCoordX, player.chunkCoordZ, isFarClaim, price, empire);
            if (block == null) {
                throw new EmpiresCommandException("Empires.cmd.err.claim.failed");
            }

            getDatasource().saveBlock(block);
            ChatManager.send(sender, "Empires.notification.block.added", block.getX() * 16, block.getZ() * 16, block.getX() * 16 + 15, block.getZ() * 16 + 15, empire);
        } else {

            checkPositiveInteger(args.get(0));

            int radius = Integer.parseInt(args.get(0));
            List<ChunkPos> chunks = WorldUtils.getChunksInBox(player.dimension, (int) (player.posX - radius * 16), (int) (player.posZ - radius * 16), (int) (player.posX + radius * 16), (int) (player.posZ + radius * 16));
            isFarClaim = true;

            for (Iterator<ChunkPos> it = chunks.iterator(); it.hasNext(); ) {
                ChunkPos chunk = it.next();
                if (checkNearby(player.dimension, chunk.getX(), chunk.getZ(), empire)) {
                    isFarClaim = false;
                }
                if (getUniverse().blocks.contains(player.dimension, chunk.getX(), chunk.getZ())) {
                    it.remove();
                }

                for (int x = chunk.getX() - Config.instance.distanceBetweenEmpires.get(); x <= chunk.getX() + Config.instance.distanceBetweenEmpires.get(); x++) {
                    for (int z = chunk.getZ() - Config.instance.distanceBetweenEmpires.get(); z <= chunk.getZ() + Config.instance.distanceBetweenEmpires.get(); z++) {
                        Empire nearbyEmpire = EmpireUtils.getEmpireAtPosition(player.dimension, x, z);
                        if (nearbyEmpire != null && nearbyEmpire != empire && !nearbyEmpire.flagsContainer.getValue(FlagType.NEARBY)) {
                            throw new EmpiresCommandException("Empires.cmd.err.claim.tooClose", nearbyEmpire, Config.instance.distanceBetweenEmpires.get());
                        }
                    }
                }
            }

            if (empire.empireBlocksContainer.size() + chunks.size() > empire.getMaxBlocks()) {
                throw new EmpiresCommandException("Empires.cmd.err.empire.maxBlocks", chunks.size());
            }
            
            if (empire.empireBlocksContainer.size() + chunks.size() > empire.getPower()) {
            	throw new EmpiresCommandException("Empires.cmd.err.empire.lackPower");
            }

            if (isFarClaim && empire.empireBlocksContainer.getFarClaims() + 1 > empire.getMaxFarClaims()) {
                throw new EmpiresCommandException("Empires.cmd.err.claim.far.notAllowed");
            }

            makeBankPayment(player, empire, (isFarClaim ? Config.instance.costAmountClaimFar.get() + Config.instance.costAmountClaim.get() * (chunks.size() - 1) : Config.instance.costAmountClaim.get() * chunks.size())
                    + MathUtils.sumFromNtoM(empire.empireBlocksContainer.size(), empire.empireBlocksContainer.size() + chunks.size() - 1) * Config.instance.costAdditionClaim.get());

            for (ChunkPos chunk : chunks) {
                int price = (isFarClaim ? Config.instance.costAmountClaimFar.get() : Config.instance.costAmountClaim.get()) + Config.instance.costAdditionClaim.get() * empire.empireBlocksContainer.size();
                EmpireBlock block = getUniverse().newBlock(player.dimension, chunk.getX(), chunk.getZ(), isFarClaim, price, empire);
                // Only one of the block will be a farClaim, rest will be normal claim
                isFarClaim = false;
                getDatasource().saveBlock(block);
                ChatManager.send(sender, "Empires.notification.block.added", block.getX() * 16, block.getZ() * 16, block.getX() * 16 + 15, block.getZ() * 16 + 15, empire);
            }
        }
        return CommandResponse.DONE;
    }

    @Command(
            name = "unclaim",
            permission = "Empires.cmd.officer.unclaim",
            parentName = "Empires.cmd",
            syntax = "/empire unclaim")
    public static CommandResponse unclaimCommand(ICommandSender sender, List<String> args) {
        EntityPlayer player = (EntityPlayer) sender;
        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        EmpireBlock block = getBlockAtCitizen(res);
        Empire empire = getEmpireFromCitizen(res);

        if (empire != block.getEmpire()) {
            throw new EmpiresCommandException("Empires.cmd.err.unclaim.notInEmpire");
        }
        if (block.isPointIn(empire.getSpawn().getDim(), empire.getSpawn().getX(), empire.getSpawn().getZ())) {
            throw new EmpiresCommandException("Empires.cmd.err.unclaim.spawnPoint");
        }
        if (!checkNearby(block.getDim(), block.getX(), block.getZ(), empire) && empire.empireBlocksContainer.size() <= 1) {
            throw new EmpiresCommandException("Empires.cmd.err.unclaim.lastClaim");
        }

        getDatasource().deleteBlock(block);
        ChatManager.send(sender, "Empires.notification.block.removed", block.getX() << 4, block.getZ() << 4, block.getX() << 4 + 15, block.getZ() << 4 + 15, empire);
        
        if (Config.instance.toggleRefund.get() == false) { //Toggle Refund in Config
        makeBankRefund(player, empire, block.getPricePaid());
        }
        return CommandResponse.DONE;
    }
    
    @Command(
            name = "overclaim",
            permission = "Empires.cmd.officer.overclaim",
            parentName = "Empires.cmd",
            syntax = "/empire overclaim")
    public static CommandResponse overclaimCommand(ICommandSender sender, List<String> args) {
        EntityPlayer player = (EntityPlayer) sender;
        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        EmpireBlock block = getBlockAtCitizen(res);
        Empire empire = block.getEmpire();
        
      //  try {
        Empire CitizensEmpire = CommandsEMP.getEmpireFromCitizen(res);

        if (CitizensEmpire == null) { //If the player doesn't have an empire, then don't work.
        	throw new EmpiresCommandException("Empires.cmd.err.overclaim.userNotInEmpire");
        }

        if (CitizensEmpire == block.getEmpire()) {  //If the player is trying to overclaim his own empire, then don't work.
    	    throw new EmpiresCommandException("Empires.cmd.err.overclaim.presentEmpire");
        }
        
        
        if (block.getEmpire().empireBlocksContainer.size() < block.getEmpire().getPower()) {
            throw new EmpiresCommandException("Empires.cmd.err.overclaim.powerException");
        }
        
        if (res.getPower() < 7) { //Make configurable.
        	throw new EmpiresCommandException("Empires.cmd.err.overclaim.minPowerRequired");
        }

        				
        if (block.isPointIn(empire.getSpawn().getDim(), empire.getSpawn().getX(), empire.getSpawn().getZ())) {
        getDatasource().deleteEmpire(empire); 
        empire.notifyEveryone(getLocal().getLocalization("Empires.notification.empire.overclaimed.deleted", empire, res));
        }
        
        if (block.getEmpire().empireBlocksContainer.size() < block.getEmpire().getPower()) {

        
        				
        getDatasource().deleteBlock(block);
        ChatManager.send(sender, "Empires.notification.block.overclaimed", block.getX() << 4, block.getZ() << 4, block.getX() << 4 + 15, block.getZ() << 4 + 15, empire);
        }
        return CommandResponse.DONE;
        
    }
    

    @Command(
            name = "invite",
            permission = "Empires.cmd.officer.invite",
            parentName = "Empires.cmd",
            syntax = "/empire invite <citizen>",
            completionKeys = {"citizenCompletion"})
    public static CommandResponse inviteCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1) {
            return CommandResponse.SEND_SYNTAX;
        }

        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        Empire empire = getEmpireFromCitizen(res);

        Citizen target = getCitizenFromName(args.get(0));
        if (empire.citizensMap.contains(args.get(0))) {
            throw new EmpiresCommandException("Empires.cmd.err.invite.already", target, empire);
        }

        getDatasource().saveEmpireInvite(target, empire);
        ChatManager.send(target.getPlayer(), "Empires.notification.empire.invited", empire);
        ChatManager.send(sender, "Empires.notification.empire.invite.sent");
        return CommandResponse.DONE;
    }
    
    
    
    @Command(
            name = "ban",
            permission = "Empires.cmd.officer.ban",
            parentName = "Empires.cmd",
            syntax = "/empire ban <citizen>",
            completionKeys = {"citizenCompletion"})
    public static CommandResponse banCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1) {
            return CommandResponse.SEND_SYNTAX;
        }

        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        Empire empire = getEmpireFromCitizen(res);
        Citizen target = getCitizenFromName(args.get(0));

        if (target.getBanned() == true) {
        	throw new EmpiresCommandException("Empires.cmd.err.ban.existing");
        }

        if (target == res) {
            throw new EmpiresCommandException("Empires.cmd.err.ban.self");
        }
        
        if (empire.citizensMap.get(target) == empire.ranksContainer.getLeaderRank()) {
            throw new EmpiresCommandException("Empires.cmd.err.ban.leader");
        }
       
       if (empire.citizensMap.contains(target.getPlayerName())) {
    	   getDatasource().unlinkCitizenFromEmpire(target, empire);
       }
       target.empireBansContainer.add(empire);
        ChatManager.send(sender, "Empires.notification.empire.banned.sender", target);
        
        ChatManager.send(target.getPlayer(), "Empires.notification.empire.receiver", empire);
        
        empire.notifyEveryone(getLocal().getLocalization("Empires.notification.empire.banned.tellAll", target, res));
        return CommandResponse.DONE;
    }

    @Command(
            name = "unban",
            permission = "Empires.cmd.officer.unban",
            parentName = "Empires.cmd",
            syntax = "/empire unban <citizen>",
            completionKeys = {"citizenCompletion"})
    public static CommandResponse unbanCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1) {
            return CommandResponse.SEND_SYNTAX;
        }

        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        Empire empire = getEmpireFromCitizen(res);
        Citizen target = getCitizenFromName(args.get(0));

        if (!target.empireBansContainer.contains(empire)) {
        	throw new EmpiresCommandException("Empires.cmd.err.ban.notBanned");
        }
                 
        target.empireBansContainer.remove(empire);
        
        ChatManager.send(sender, "Empires.notification.empire.unbanned.sender", target);
        
        ChatManager.send(target.getPlayer(), "Empires.notification.empire.unbanned.receiver", empire);
        
        empire.notifyEveryone(getLocal().getLocalization("Empires.notification.empire.unbanned.tellAll", target, res));
      
        return CommandResponse.DONE;
    }
    
    
    
    
    
    
    
    
    
    @Command(
            name = "set",
            permission = "Empires.cmd.officer.perm.set",
            parentName = "Empires.cmd.everyone.perm",
            syntax = "/empire perm set <flag> <value>",
            completionKeys = "flagCompletion")
    public static CommandResponse permSetCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 2) {
            return CommandResponse.SEND_SYNTAX;
        }

        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        Empire empire = getEmpireFromCitizen(res);
        Flag flag = getFlagFromName(empire.flagsContainer, args.get(0));

        if (!flag.flagType.configurable) {
            throw new EmpiresCommandException("Empires.cmd.err.flag.unconfigurable");
        
        } 
        
        if (flag.flagType.name == "ENTER") {
        	throw new EmpiresCommandException("Empires.cmd.err.flag.unconfigurable");
        }
        
        if (flag.flagType.name == "PVP") {
        	throw new EmpiresCommandException("Empires.cmd.err.flag.unconfigurable");
        }
        
        else {
            if (flag.setValue(args.get(1))) {
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
            permission = "Empires.cmd.officer.perm.toggle",
            parentName = "Empires.cmd.everyone.perm",
            syntax = "/empire perm toggle <flag>",
            completionKeys = "flagCompletion")
    public static CommandResponse permToggleCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1) {
            return CommandResponse.SEND_SYNTAX;
        }

        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        Empire empire = getEmpireFromCitizen(res);
        Flag flag = getFlagFromName(empire.flagsContainer, args.get(0));

        if (!flag.flagType.configurable) {
            throw new EmpiresCommandException("Empires.cmd.err.flag.unconfigurable");
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
            permission = "Empires.cmd.officer.perm.whitelist",
            parentName = "Empires.cmd.everyone.perm",
            syntax = "/empire perm whitelist")
    public static CommandResponse permWhitelistCommand(ICommandSender sender, List<String> args) {
        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        ToolManager.instance.register(new WhitelisterTool(res));
        return CommandResponse.DONE;
    }

    @Command(
            name = "promote",
            permission = "Empires.cmd.officer.promote",
            parentName = "Empires.cmd",
            syntax = "/empire promote <citizen> <rank>",
            completionKeys = {"citizenCompletion", "rankCompletion"})
    public static CommandResponse promoteCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 2) {
            return CommandResponse.SEND_SYNTAX;
        }

        Citizen resSender = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        Citizen resTarget = getCitizenFromName(args.get(0));
        Empire empire = getEmpireFromCitizen(resSender);

        if (!resTarget.empiresContainer.contains(empire)) {
            throw new EmpiresCommandException("Empires.cmd.err.citizen.notInEmpire", resTarget);
        }

        Rank leaderRank = empire.ranksContainer.getLeaderRank();
        if(empire.citizensMap.get(resTarget) == leaderRank) {
            throw new EmpiresCommandException("Empires.cmd.err.promote.leader");
        }
        if (args.get(1).equalsIgnoreCase(leaderRank.getName())) {
            throw new EmpiresCommandException("Empires.cmd.err.promote.notLeader");
        }
        Rank rank = getRankFromEmpire(empire, args.get(1));
        if (getDatasource().updateCitizenToEmpireLink(resTarget, empire, rank)) {
            ChatManager.send(sender, "Empires.cmd.promote.success.sender", resTarget, rank);
            ChatManager.send(resTarget.getPlayer(), "Empires.cmd.promote.success.target", rank);
        }
        return CommandResponse.DONE;
    }

    public static class ModifyRanks {

        private ModifyRanks() {
        }

        @Command(
                name = "add",
                permission = "Empires.cmd.officer.ranks.add",
                parentName = "Empires.cmd.everyone.ranks",
                syntax = "/empire ranks add <name> [templateRank]",
                completionKeys = {"-", "ranksCompletion"})
        public static CommandResponse ranksAddCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 1) {
                return CommandResponse.SEND_SYNTAX;
            }

            Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
            Empire empire = getEmpireFromCitizen(res);

            if (empire.ranksContainer.contains(args.get(0))) {
                throw new EmpiresCommandException("Empires.cmd.err.ranks.add.already", empire.ranksContainer.get(args.get(0)));
            }

            Rank rank = new Rank(args.get(0), empire, Rank.Type.OFFICER);
            if(args.size() == 2) {
                Rank template = getRankFromEmpire(empire, args.get(1));
                rank.permissionsContainer.addAll(template.permissionsContainer);
            }

            getDatasource().saveRank(rank);

            ChatManager.send(sender, "Empires.notification.empire.ranks.add", rank, empire);
            return CommandResponse.DONE;
        }

        @Command(
                name = "remove",
                permission = "Empires.cmd.officer.ranks.remove",
                parentName = "Empires.cmd.everyone.ranks",
                syntax = "/empire ranks remove <rank>",
                completionKeys = {"rankCompletion"})
        public static CommandResponse ranksRemoveCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 1) {
                return CommandResponse.SEND_SYNTAX;
            }

            Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
            Empire empire = getEmpireFromCitizen(res);
            Rank rank = getRankFromEmpire(empire, args.get(0));

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
                permission = "Empires.cmd.officer.ranks.set",
                parentName = "Empires.cmd.everyone.ranks",
                syntax = "/empire ranks set <rank> <type>",
                completionKeys = {"rankCompletion"})
        public static CommandResponse ranksSetCommand(ICommandSender sender, List<String> args) {
            if(args.size() < 2) {
                return CommandResponse.SEND_SYNTAX;
            }

            Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
            Empire empire = getEmpireFromCitizen(res);
            Rank rank = getRankFromEmpire(empire, args.get(0));
            Rank.Type type = getRankTypeFromString(args.get(1));

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
                if(rank.getType().unique) {
                    throw new EmpiresCommandException("Empires.cmd.err.ranks.set.unique", rank);
                }

                rank.setType(type);
                getDatasource().saveRank(rank);
            }

            ChatManager.send(sender, "Empires.notification.ranks.set.successful", rank, type);
            return CommandResponse.DONE;
        }


        @Command(
                name = "add",
                permission = "Empires.cmd.officer.ranks.perm.add",
                parentName = "Empires.cmd.officer.ranks.perm",
                syntax = "/empire ranks perm add <rank> <perm>",
                completionKeys = {"rankCompletion"})
        public static CommandResponse ranksPermAddCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 2) {
                return CommandResponse.SEND_SYNTAX;
            }

            Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
            Empire empire = getEmpireFromCitizen(res);
            Rank rank = getRankFromEmpire(empire, args.get(0));

            getDatasource().saveRankPermission(rank, args.get(1));
            ChatManager.send(sender, "Empires.notification.empire.ranks.perm.add");

            return CommandResponse.DONE;
        }

        @Command(
                name = "remove",
                permission = "Empires.cmd.officer.ranks.perm.remove",
                parentName = "Empires.cmd.officer.ranks.perm",
                syntax = "/empire ranks perm remove <rank> <perm>",
                completionKeys = {"rankCompletion"})
        public static CommandResponse ranksPermRemoveCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 2) {
                return CommandResponse.SEND_SYNTAX;
            }

            Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
            Empire empire = getEmpireFromCitizen(res);
            Rank rank = getRankFromEmpire(empire, args.get(0));

            getDatasource().deleteRankPermission(rank, args.get(1));
            ChatManager.send(sender, "Empires.notification.empire.ranks.perm.remove");

            return CommandResponse.DONE;
        }

        @Command(
                name = "reset",
                permission = "Empires.cmd.officer.ranks.reset",
                parentName = "Empires.cmd.everyone.ranks",
                syntax = "/empire ranks reset")
        public static CommandResponse ranksResetCommand(ICommandSender sender, List<String> args) {
            Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
            Empire empire = getEmpireFromCitizen(res);

            for(Rank defaultRank : Rank.defaultRanks) {
                Rank rank = empire.ranksContainer.get(defaultRank.getName());

                rank.permissionsContainer.clear();
                rank.permissionsContainer.addAll(defaultRank.permissionsContainer);
                rank.setType(defaultRank.getType());

                getDatasource().saveRank(rank);
            }

            for(int i = 0; i < empire.ranksContainer.size(); i++) {
                Rank rank = empire.ranksContainer.get(i);
                if(!Rank.defaultRanks.contains(rank.getName())) {
                    getDatasource().deleteRank(rank);
                    i--;
                }
            }

            ChatManager.send(sender, "Empires.notification.ranks.reset");

            return CommandResponse.DONE;
        }
    }

    @Command(
            name = "perm",
            permission = "Empires.cmd.officer.ranks.perm",
            parentName = "Empires.cmd.everyone.ranks",
            syntax = "/empire ranks perm <command>")
    public static CommandResponse ranksPermCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "list",
            permission = "Empires.cmd.officer.ranks.perm.list",
            parentName = "Empires.cmd.officer.ranks.perm",
            syntax = "/empire ranks perm list [rank]")
    public static CommandResponse ranksPermListCommand(ICommandSender sender, List<String> args) {
        Rank rank;
        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        Empire empire = getEmpireFromCitizen(res);
        if (args.isEmpty()) {
            rank = getRankFromCitizen(res);
        } else {
            rank = getRankFromEmpire(empire, args.get(0));
        }

        ChatManager.send(sender, rank.permissionsContainer.toChatMessage());
        return CommandResponse.DONE;
    }

    @Command(
            name = "abdicate",
            permission = "Empires.cmd.leader.pass",
            parentName = "Empires.cmd",
            syntax = "/empire abdicate <citizen>",
            completionKeys = {"citizenCompletion"})
    public static CommandResponse passCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1) {
            return CommandResponse.SEND_SYNTAX;
        }

        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        Citizen target = getCitizenFromName(args.get(0));

        if (res == target) {
            throw new EmpiresCommandException("Empires.cmd.err.citizen.same");
        }

        Empire empire = getEmpireFromCitizen(res);

        if (!empire.citizensMap.containsKey(target)) {
            throw new EmpiresCommandException("Empires.cmd.err.citizen.notInEmpire", target, empire);
        }
        if (empire.citizensMap.get(res).getType() == Rank.Type.LEADER) {
            getDatasource().updateCitizenToEmpireLink(target, empire, empire.ranksContainer.getLeaderRank());
            ChatManager.send(target.getPlayer(), "Empires.notification.empire.leaderShip.passed");
            getDatasource().updateCitizenToEmpireLink(res, empire, empire.ranksContainer.getDefaultRank());
            ChatManager.send(sender, "Empires.notification.empire.leaderShip.taken");
        }
        return CommandResponse.DONE;
    }

    public static class Plots {
        @Command(
                name = "limit",
                permission = "Empires.cmd.officer.plot.limit",
                parentName = "Empires.cmd.everyone.plot",
                syntax = "/empire plot limit <command>")
        public static CommandResponse plotLimitCommand(ICommandSender sender, List<String> args) {
            return CommandResponse.SEND_HELP_MESSAGE;
        }

        @Command(
                name = "show",
                permission = "Empires.cmd.officer.plot.limit.show",
                parentName = "Empires.cmd.officer.plot.limit",
                syntax = "/empire plot limit show")
        public static CommandResponse plotLimitShowCommand(ICommandSender sender, List<String> args) {
            Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
            Empire empire = getEmpireFromCitizen(res);
            ChatManager.send(sender, "Empires.notification.plot.limit", empire.plotsContainer.getMaxPlots());
            return CommandResponse.DONE;
        }


        @Command(
                name = "set",
                permission = "Empires.cmd.officer.plot.limit.set",
                parentName = "Empires.cmd.officer.plot.limit",
                syntax = "/empire plot limit set <limit>")
        public static CommandResponse plotLimitSetCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 1) {
                return CommandResponse.SEND_SYNTAX;
            }

            checkPositiveInteger(args.get(0));

            int limit = Integer.parseInt(args.get(0));
            Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
            Empire empire = getEmpireFromCitizen(res);
            empire.plotsContainer.setMaxPlots(limit);
            getDatasource().saveEmpire(empire);
            ChatManager.send(sender, "Empires.notification.plot.limit.set", empire.plotsContainer.getMaxPlots());
            return CommandResponse.DONE;
        }
    }

    @Command(
            name = "kick",
            permission = "Empires.cmd.officer.kick",
            parentName = "Empires.cmd",
            syntax = "/empire kick <citizen>",
            completionKeys = {"citizenCompletion"})
    public static CommandResponse kickCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1) {
            return CommandResponse.SEND_SYNTAX;
        }

        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        Citizen target = getCitizenFromName(args.get(0));
        Empire empire = getEmpireFromCitizen(res);
        if (!target.empiresContainer.contains(empire)) {
            throw new EmpiresCommandException("Empires.cmd.err.citizen.notInEmpire", target);
        }
        if (target == res) {
            throw new EmpiresCommandException("Empires.cmd.err.kick.self");
        }

        if(empire.citizensMap.get(target) == empire.ranksContainer.getLeaderRank()) {
            throw new EmpiresCommandException("Empires.cmd.err.kick.leader");
        }

        getDatasource().unlinkCitizenFromEmpire(target, empire);
        ChatManager.send(target.getPlayer(), "Empires.notification.empire.kicked", empire);
        empire.notifyEveryone(getLocal().getLocalization("Empires.notification.empire.left", target, empire));
        return CommandResponse.DONE;
    }

    @Command(
            name = "delete",
            permission = "Empires.cmd.leader.leave.delete",
            parentName = "Empires.cmd.everyone.leave",
            syntax = "/empire leave delete")
    public static CommandResponse leaveDeleteCommand(ICommandSender sender, List<String> args) {
        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        Empire empire = getEmpireFromCitizen(res);
        EntityPlayer player = (EntityPlayer) sender;

        if (empire.citizensMap.get(res).getType() == Rank.Type.LEADER) {
            for(Plot plot: empire.plotsContainer) {
                if(!plot.ownersContainer.contains(res)) {
                    throw new EmpiresCommandException("Empires.cmd.err.empire.delete.withPlots");
                }
            }

            empire.notifyEveryone(getLocal().getLocalization("Empires.notification.empire.deleted", empire, res));
            
            if (Config.instance.toggleRefund.get() == false) { //toggleRefund in config, if true then don't refund.
            int refund = 0;
            for (EmpireBlock block : empire.empireBlocksContainer.values()) {
                refund += block.getPricePaid();
            }
            refund += empire.bank.getAmount();
            makeRefund(player, refund);
            }
            getDatasource().deleteEmpire(empire);
        }
        return CommandResponse.DONE;
    }

    @Command(
            name = "rename",
            permission = "Empires.cmd.officer.rename",
            parentName = "Empires.cmd",
            syntax = "/empire rename <name>")
    public static CommandResponse renameCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1) {
            return CommandResponse.SEND_SYNTAX;
        }

        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        Empire empire = getEmpireFromCitizen(res);

        if (getUniverse().empires.contains(args.get(0))) {
            throw new EmpiresCommandException("Empires.cmd.err.new.nameUsed", args.get(0));
        }

        empire.rename(args.get(0));
        getDatasource().saveEmpire(empire);
        ChatManager.send(sender, "Empires.notification.empire.renamed");
        return CommandResponse.DONE;
    }

    @Command(
            name = "withdraw",
            permission = "Empires.cmd.officer.bank.withdraw",
            parentName = "Empires.cmd.everyone.bank",
            syntax = "/empire bank withdraw <amount>")
    public static CommandResponse bankWithdrawCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 1) {
            return CommandResponse.SEND_SYNTAX;
        }

        checkPositiveInteger(args.get(0));

        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        Empire empire = getEmpireFromCitizen(res);

        if(empire instanceof AdminEmpire) {
            throw new EmpiresCommandException("Empires.cmd.err.adminEmpire", empire);
        }

        int amount = Integer.parseInt(args.get(0));
        if(empire.bank.getAmount() < amount) {
            throw new EmpiresCommandException("Empires.cmd.err.bank.withdraw", EconomyProxy.getCurrency(empire.bank.getAmount()));
        }

        makeRefund(res.getPlayer(), amount);
        empire.bank.addAmount(-amount);
        getDatasource().saveEmpireBank(empire.bank);
        return CommandResponse.DONE;
    }

    @Command(
            name = "chunkload",
            permission = "Empires.cmd.officer.claim.chunkload",
            parentName = "Empires.cmd.officer.claim",
            syntax = "/empire claim chunkload")
    public static CommandResponse claimChunkloadCommand(ICommandSender sender, List<String> args) {

        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        EmpireBlock block = getBlockAtCitizen(res);

        if (!block.getEmpire().citizensMap.containsKey(res)) {
            throw new EmpiresCommandException("Empires.cmd.err.notInEmpire", block.getEmpire().getName());
        }

        if (block.isChunkloaded()) {
            throw new EmpiresCommandException("Empires.cmd.err.claim.chunkload.already");
        }

        makeBankPayment(res.getPlayer(), block.getEmpire(), Config.instance.costAmountChunkloadedClaim.get());
        block.getEmpire().ticketMap.chunkLoad(block);

        ChatManager.send(sender, "Empires.notification.claim.chunkload");

        return CommandResponse.DONE;
    }

    @Command(
            name = "chunkunload",
            permission = "Empires.cmd.officer.claim.chunkunload",
            parentName = "Empires.cmd.officer.claim",
            syntax = "/empire claim chunkunload")
    public static CommandResponse claimUnchunkloadCommand(ICommandSender sender, List<String> args) {

        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        EmpireBlock block = getBlockAtCitizen(res);

        if (!block.getEmpire().citizensMap.containsKey(res)) {
            throw new EmpiresCommandException("Empires.cmd.err.notInEmpire", block.getEmpire().getName());
        }

        if (!block.isChunkloaded()) {
            throw new EmpiresCommandException("Empires.cmd.err.claim.unchunkload.missing");
        }

        makeBankRefund(res.getPlayer(), block.getEmpire(), Config.instance.costAmountChunkloadedClaim.get());
        block.getEmpire().ticketMap.chunkUnload(block);

        ChatManager.send(sender, "Empires.notification.claim.chunkunload");

        return CommandResponse.DONE;
    }

    @Command(
            name = "all",
            permission = "Empires.cmd.officer.claim.chunkload.all",
            parentName = "Empires.cmd.officer.claim.chunkload",
            syntax = "/empire claim chunkload all")
    public static CommandResponse claimChunkloadAllCommand(ICommandSender sender, List<String> args) {

        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        Empire empire = getEmpireFromCitizen(res);

        int chunksToLoad = empire.empireBlocksContainer.size() - empire.ticketMap.getChunkloadedAmount();

        makeBankPayment(sender, empire, Config.instance.costAmountChunkloadedClaim.get() * chunksToLoad);
        empire.ticketMap.chunkLoadAll();

        ChatManager.send(sender, "Empires.notification.claim.chunkload.all", chunksToLoad);

        return CommandResponse.DONE;
    }

    @Command(
            name = "all",
            permission = "Empires.cmd.officer.claim.chunkunload.all",
            parentName = "Empires.cmd.officer.claim.chunkunload",
            syntax = "/empire claim chunkunload all")
    public static CommandResponse claimChunkunloadAllCommand(ICommandSender sender, List<String> args) {

        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        Empire empire = getEmpireFromCitizen(res);
        int chunksToUnload = empire.ticketMap.getChunkloadedAmount();

        makeBankRefund(sender, empire, Config.instance.costAmountChunkloadedClaim.get() * chunksToUnload);
        empire.ticketMap.chunkUnloadAll();

        ChatManager.send(sender, "Empires.notification.claim.chunkunload.all", chunksToUnload);

        return CommandResponse.DONE;
    }

   //public to private
    public static boolean checkNearby(int dim, int x, int z, Empire empire) {
        int[] dx = {1, 0, -1, 0};
        int[] dz = {0, 1, 0, -1};

        for (int i = 0; i < 4; i++) {
            EmpireBlock block = getUniverse().blocks.get(dim, x + dx[i], z + dz[i]);
            if (block != null && block.getEmpire() == empire) {
                return true;
            }
        }
        return false;
    }
}