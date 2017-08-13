package com.EmpireMod.Empires.entities.Empire;


import java.util.ArrayList;
import java.util.Iterator;

import com.EmpireMod.Empires.API.Chat.IChatFormat;
import com.EmpireMod.Empires.API.Chat.Component.ChatComponentFormatted;
import com.EmpireMod.Empires.API.Chat.Component.ChatManager;
import com.EmpireMod.Empires.API.Container.CitizenRankMap;
import com.EmpireMod.Empires.API.Container.TicketMap;
import com.EmpireMod.Empires.API.permissions.PermissionProxy;
import com.EmpireMod.Empires.Configuration.Config;
import com.EmpireMod.Empires.Localization.LocalizationManager;
import com.EmpireMod.Empires.Misc.Teleport.Teleport;
import com.EmpireMod.Empires.Utilities.PlayerUtils;
import com.EmpireMod.Empires.entities.Flags.Flag;
import com.EmpireMod.Empires.entities.Flags.FlagType;
import com.EmpireMod.Empires.entities.Permissions.PermissionLevel;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

/**
 * Defines a Empire. A Empire is made up of Citizens, Ranks, Blocks, and Plots.
 */
public class Empire implements Comparable<Empire>, IChatFormat {
	
    private double CitizensPower = 0.00;
	
	private double maxPower = 0.00;
	
    private String name, oldName = null;

    protected int maxFarClaims = Config.instance.maxFarClaims.get();

    private Alliance alliance;
    private Teleport spawn;

    public final TicketMap ticketMap = new TicketMap(this);
    public final CitizenRankMap citizensMap = new CitizenRankMap();
    public final Rank.Container ranksContainer = new Rank.Container();
    public final Plot.Container plotsContainer = new Plot.Container(Config.instance.defaultMaxPlots.get());
    public final Flag.Container flagsContainer = new Flag.Container();
    public final EmpireBlock.Container empireBlocksContainer = new EmpireBlock.Container();
    public final BlockWhitelist.Container blockWhitelistsContainer = new BlockWhitelist.Container();

    public final Bank bank = new Bank(this);

    public Empire(String name) {
        this.name = name;
    }

    /**
     * Notifies every citizen in this empire sending a message.
     */
    public void notifyEveryone(IChatComponent message) {
        for (Citizen r : citizensMap.keySet()) {
            ChatManager.send(r.getPlayer(), message);
        }
    }

    public IChatComponent getOwnerComponent() {
        Citizen leader = citizensMap.getLeader();
        return leader == null ? LocalizationManager.get("Empires.notification.empire.owners.admins") : leader.toChatMessage();
    }

    /**
     * Checks if the Citizen is allowed to do the action specified by the FlagType at the coordinates given.
     * This method will go through all the plots and prioritize the plot's flags over empire flags.
     */
    public boolean hasPermission(Citizen res, FlagType<Boolean> flagType, int dim, int x, int y, int z) {
        Plot plot = plotsContainer.get(dim, x, y, z);

        if (plot == null) {
            return hasPermission(res, flagType);
        } else {
        	return plot.hasPermission(res, flagType);
        }
    }

    /**
     * Checks if the Citizen is allowed to do the action specified by the FlagType in this empire.
     */
    public boolean hasPermission(Citizen res, FlagType<Boolean> flagType) {
        if(flagType.configurable ? flagsContainer.getValue(flagType) : flagType.defaultValue) {
            return true;
        }

        if (res == null || res.getFakePlayer()) {
            return false;
        }

        boolean rankBypass;
        boolean permissionBypass;

        if (citizensMap.containsKey(res)) {
            if (flagsContainer.getValue(FlagType.RESTRICTIONS)) {
                rankBypass = hasPermission(res, FlagType.RESTRICTIONS.getBypassPermission());
                permissionBypass = PermissionProxy.getPermissionManager().hasPermission(res.getUUID(), FlagType.RESTRICTIONS.getBypassPermission());

                if (!rankBypass && !permissionBypass) {
                    ChatManager.send(res.getPlayer(), flagType.getDenialKey());
                    ChatManager.send(res.getPlayer(), "Empires.notification.empire.owners", getOwnerComponent());
                    return false;
                }
            }

            rankBypass = hasPermission(res, flagType.getBypassPermission());
            permissionBypass = PermissionProxy.getPermissionManager().hasPermission(res.getUUID(), flagType.getBypassPermission());

            if (!rankBypass && !permissionBypass) {
                ChatManager.send(res.getPlayer(), flagType.getDenialKey());
                ChatManager.send(res.getPlayer(), "Empires.notification.empire.owners", getOwnerComponent());
                return false;
            }

        } else {
            permissionBypass = PermissionProxy.getPermissionManager().hasPermission(res.getUUID(), flagType.getBypassPermission());

            if (!permissionBypass) {
                ChatManager.send(res.getPlayer(), flagType.getDenialKey());
                ChatManager.send(res.getPlayer(), "Empires.notification.empire.owners", getOwnerComponent());
                return false;
            }
        }

        return true;
    }

    /**
     * Permission node check for Citizens
     */
    public boolean hasPermission(Citizen res, String permission) {
        if(!citizensMap.containsKey(res)) {
            return false;
        }

        Rank rank = citizensMap.get(res);
        return rank.permissionsContainer.hasPermission(permission) == PermissionLevel.ALLOWED;
    }

    public <T> T getValueAtCoords(int dim, int x, int y, int z, FlagType<T> flagType) {
        Plot plot = plotsContainer.get(dim, x, y, z);
        if(plot == null || !flagType.isPlotPerm) {
            return flagsContainer.getValue(flagType);
        } else {
            return plot.flagsContainer.getValue(flagType);
        }
    }

    /**
     * Used to get the owners of a plot (or a empire) at the position given
     * Returns null if position is not in empire
     */
    public Citizen.Container getOwnersAtPosition(int dim, int x, int y, int z) {
        Citizen.Container result = new Citizen.Container();
        Plot plot = plotsContainer.get(dim, x, y, z);
        if (plot == null) {
            if (isPointInEmpire(dim, x, z) && !(this instanceof AdminEmpire) && !citizensMap.isEmpty()) {
            	Citizen leader = citizensMap.getLeader();
                if (leader != null) {
                	result.add(leader);
                }
            }
        } else {
            for (Citizen res : plot.ownersContainer) {
                result.add(res);
            }
        }
        return result;
    }

    public void sendToSpawn(Citizen res) {
        EntityPlayer pl = res.getPlayer();
        if (pl != null) {
            PlayerUtils.teleport((EntityPlayerMP)pl, spawn.getDim(), spawn.getX(), spawn.getY(), spawn.getZ());
            res.setTeleportCooldown(Config.instance.teleportCooldown.get());
        }
    }

    public int getMaxFarClaims() {
        return maxFarClaims + empireBlocksContainer.getExtraFarClaims();
    }

    public int getMaxBlocks() {
        int leaderBlocks = Config.instance.blocksLeader.get();
        int citizensBlocks = Config.instance.blocksCitizen.get() * (citizensMap.size() - 1);
        int citizensExtra = 0;
        for(Citizen res : citizensMap.keySet()) {
            citizensExtra += res.getExtraBlocks();
        }
        int empireExtra = empireBlocksContainer.getExtraBlocks();

        return leaderBlocks + citizensBlocks + citizensExtra + empireExtra;
    }
    
    
    public double getMaxPower() {
    	double maxPower = 0.00 + citizensMap.size() * Config.instance.defaultMaxPower.get();
    	
	return maxPower;
    }
    
    public double getMaxPowerLocal(Empire empire) {
    	double maxPower = 0.00 + empire.citizensMap.size() * Config.instance.defaultMaxPower.get();
    	return maxPower;
    }
    
         
    public int getExtraBlocks() {
        int citizensExtra = 0;
        for(Citizen res : citizensMap.keySet()) {
            citizensExtra += res.getExtraBlocks();
        }
        return citizensExtra + empireBlocksContainer.getExtraBlocks();
    }

    /* ----- Comparable ----- */

    @Override
    public int compareTo(Empire t) { // TODO Flesh this out more for ranking empires?
        int thisNumberOfCitizens = citizensMap.size(),
                thatNumberOfCitizens = t.citizensMap.size();
        if (thisNumberOfCitizens > thatNumberOfCitizens)
            return -1;
        else if (thisNumberOfCitizens == thatNumberOfCitizens)
            return 0;
        else if (thisNumberOfCitizens < thatNumberOfCitizens)
            return 1;

        return -1;
    }

    public String getName() {
        return name;
    }

    public String getOldName() {
        return oldName;
    }

    /**
     * Renames this current Empire setting oldName to the previous name. You MUST set oldName to null after saving it in the Datasource
     */
    public void rename(String newName) {
        oldName = name;
        name = newName;
    }

    /**
     * Resets the oldName to null. You MUST call this after a name change in the Datasource!
     */
    public void resetOldName() {
        oldName = null;
    }

    public Alliance getAlliance() {
        return alliance;
    }

    public void setAlliance(Alliance alliance) {
        this.alliance = alliance;
    }

    public boolean hasSpawn() {
        return spawn != null;
    }

    public Teleport getSpawn() {
        return spawn;
    }

    public void setSpawn(Teleport spawn) {
        this.spawn = spawn;
    }
    

    /**
     * Checks if the given block in non-chunk coordinates is in this Empire
     */
    public boolean isPointInEmpire(int dim, int x, int z) {
        return isChunkInEmpire(dim, x >> 4, z >> 4);
    }

    public boolean isChunkInEmpire(int dim, int chunkX, int chunkZ) {
        return empireBlocksContainer.contains(dim, chunkX, chunkZ);
        }

    @Override
    public String toString() {
        return toChatMessage().getUnformattedText();
    }

    @Override
    public IChatComponent toChatMessage() {
        IChatComponent header = LocalizationManager.get("Empires.format.list.header", new ChatComponentFormatted("{9|%s}", getName()));
        IChatComponent hoverComponent = ((ChatComponentFormatted)LocalizationManager.get("Empires.format.empire.long", header, citizensMap.size(), empireBlocksContainer.size(), getMaxBlocks(), getPower(), getMaxPower(), citizensMap, ranksContainer)).applyDelimiter("\n");

        return LocalizationManager.get("Empires.format.empire.short", name, hoverComponent);
    }
    
    
    public void setPower(double Power) {
    	
    	Double target = Power;
    	
    	if (this.CitizensPower == Power) return;
    	
    	this.CitizensPower = target;
    	
   } 
    
    
    
    
    public double getPower() {
    	return CitizensPower;
    }

    public static class Container extends ArrayList<Empire> implements IChatFormat {

        private Empire mainEmpire;
        public boolean isSelectedEmpireSaved = false;

        @Override
        public boolean add(Empire empire) {
            if(mainEmpire == null) {
                mainEmpire = empire;
            }
            return super.add(empire);
        }

        public Empire get(String name) {
            for(Empire empire : this) {
                if(empire.getName().equals(name)) {
                    return empire;
                }
            }
            return null;
        }

        public void remove(String name) {
            for(Iterator<Empire> it = iterator(); it.hasNext(); ) {
                Empire empire = it.next();
                if(empire.getName().equals(name)) {
                    it.remove();
                }
            }
        }

        public boolean contains(String name) {
            for(Empire empire : this) {
                if(empire.getName().equals(name)) {
                    return true;
                }
            }
            return false;
        }

        public void setMainEmpire(Empire empire) {
            if(contains(empire)) {
                mainEmpire = empire;
            }
        }
        


		public Empire getMainEmpire() {
            if(!contains(mainEmpire) || mainEmpire == null) {
                if(size() == 0) {
                    return null;
                } else {
                    mainEmpire = get(0);
                }
            }

            return mainEmpire;
        }

        @Override
        public IChatComponent toChatMessage() {
            IChatComponent root = new ChatComponentText("");

            for (Empire empire : this) {
                if (root.getSiblings().size() > 0) {
                    root.appendSibling(new ChatComponentFormatted("{7|, }"));
                }
                root.appendSibling(empire.toChatMessage());
            }

            return root;
        }
    }
}
