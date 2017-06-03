package com.EmpireMod.Empires.entities.Empire;



import com.EmpireMod.Empires.API.commands.ChatComponentFormatted;
import com.EmpireMod.Empires.API.commands.LocalManager;
import com.EmpireMod.Empires.API.commands.ChatManager;
import com.EmpireMod.Empires.API.commands.IChatFormat;

import com.EmpireMod.Empires.entities.Flags.FlagType;

import com.EmpireMod.Empires.Config.Config;
import com.EmpireMod.Empires.Datasource.EmpiresUniverse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;

import java.util.*;

public class Citizen implements IChatFormat {
    private EntityPlayer player;
    private UUID playerUUID;
    private String playerName;
    private Date joinDate = new Date();
    private Date lastOnline = new Date();

    private int teleportCooldown = 0;

    private int extraBlocks = 0;

    private boolean isFakePlayer = false;

    public final Plot.Container plotsContainer = new Plot.Container(Config.instance.defaultMaxPlots.get());
    public final Empire.Container empireInvitesContainer = new Empire.Container();
    public final Empire.Container empiresContainer = new Empire.Container();

    public Citizen(EntityPlayer pl) {
        setPlayer(pl);
        this.playerUUID = pl.getPersistentID();
    }

    public Citizen(UUID uuid, String playerName) {
        this.playerUUID = uuid;
        this.playerName = playerName;
        tryLoadPlayer();
    }

    public Citizen(UUID uuid, String playerName, boolean isFakePlayer) {
        this.playerUUID = uuid;
        this.playerName = playerName;
        this.isFakePlayer = isFakePlayer;
        if (!isFakePlayer) {
            tryLoadPlayer();
        }
    }

    public Citizen(UUID uuid, String playerName, long joinDate, long lastOnline) {
        this(uuid, playerName);
        this.joinDate.setTime(joinDate * 1000L);
        this.lastOnline.setTime(lastOnline * 1000L);
    }

    private void tryLoadPlayer() {
        for (EntityPlayer player : (List<EntityPlayer>) MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
            if(player.getPersistentID().equals(playerUUID)) {
                this.player = player;
            }
        }
    }

    /**
     * Tick function called every tick
     */
    public void tick() {
        if(teleportCooldown > 0)
            teleportCooldown--;
    }

    /* ----- Map ----- */

    /**
     * Called when a player changes location from a chunk to another
     */
    public void checkLocation(int oldChunkX, int oldChunkZ, int newChunkX, int newChunkZ, int dimension) {
        if (oldChunkX != newChunkX || oldChunkZ != newChunkZ && player != null) {
            EmpireBlock oldEmpireBlock, newEmpireBlock;

            oldEmpireBlock = EmpiresUniverse.instance.blocks.get(dimension, oldChunkX, oldChunkZ);
            newEmpireBlock = EmpiresUniverse.instance.blocks.get(dimension, newChunkX, newChunkZ);

            if (oldEmpireBlock == null && newEmpireBlock != null || oldEmpireBlock != null && newEmpireBlock != null && !oldEmpireBlock.getEmpire().getName().equals(newEmpireBlock.getEmpire().getName())) {
                if (empiresContainer.contains(newEmpireBlock.getEmpire())) {
                    ChatManager.send(player, "Empires.notification.enter.ownEmpire", newEmpireBlock.getEmpire());
                } else {
                    ChatManager.send(player, "Empires.notification.enter.empire", newEmpireBlock.getEmpire());
                }
            } else if (oldEmpireBlock != null && newEmpireBlock == null) {
                ChatManager.send(player, "Empires.notification.enter.wild");
            }
        }
    }

    /**
     * More simpler version of location check, without the need to know the old chunk's coords
     */
    public void checkLocationOnDimensionChanged(int newChunkX, int newChunkZ, int dimension) {
        EmpireBlock newEmpireBlock;

        newEmpireBlock = EmpiresUniverse.instance.blocks.get(dimension, newChunkX, newChunkZ);

        if (newEmpireBlock == null) {
            ChatManager.send(player, "Empires.notification.enter.wild");
        } else if (empiresContainer.contains(newEmpireBlock.getEmpire())) {
            ChatManager.send(player, "Empires.notification.enter.ownEmpire", newEmpireBlock.getEmpire());
        } else {
            ChatManager.send(player, "Empires.notification.enter.empire", newEmpireBlock.getEmpire());
        }
    }

    /* ----- Helpers ----- */

    /**
     * Respawns the player at empire's spawn point or, if that doesn't exist, at his own spawn point.
     */
    public void respawnPlayer() {
        if (empiresContainer.getMainEmpire() != null) {
            empiresContainer.getMainEmpire().sendToSpawn(this);
            return;
        }

        ChunkCoordinates spawn = player.getBedLocation(player.dimension);
        if (spawn == null)
            spawn = player.worldObj.getSpawnPoint();
        ((EntityPlayerMP) player).playerNetServerHandler.setPlayerLocation(spawn.posX, spawn.posY, spawn.posZ, player.rotationYaw, player.rotationPitch);
    }

    /**
     * Moves the player to the position he was last tick.
     */
    public void knockbackPlayer() {
        if(this.player != null) {
            player.setPositionAndUpdate(player.lastTickPosX, player.lastTickPosY, player.lastTickPosZ);
        }
    }

    /**
     * Moves the player to the nearest place (in the positive X direction) in which it has permission to enter.
     */
    public void knockbackPlayerToBorder(Empire empire) {
        if(this.player != null) {
            int x = (int) Math.floor(player.posX);
            int y = (int) Math.floor(player.posY);
            int z = (int) Math.floor(player.posZ);
            boolean ok = false;
            while(!ok) {
                while (!empire.hasPermission(this, FlagType.ENTER, player.dimension, x, y, z) && empire.isPointInEmpire(player.dimension, x, z))
                    x++;
                x += 3;

                while(player.worldObj.getBlock(x, y, z) != Blocks.air && player.worldObj.getBlock(x, y + 1, z) != Blocks.air && y < 256)
                    y++;

                if(empire.hasPermission(this, FlagType.ENTER, player.dimension, x, y, z) || !empire.isPointInEmpire(player.dimension, x, z))
                    ok = true;
            }
            player.setPositionAndUpdate(x, y, z);
        }
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public void setPlayer(EntityPlayer pl) {
        this.player = pl;
        this.playerName = pl.getDisplayName();
    }

    public UUID getUUID() {
        return playerUUID;
    }

    /**
     * Returns the name of the player for display purposes. <br/>
     * NEVER rely on this to store info against. The player name can change at any point, use the UUID instead.
     */
    public String getPlayerName() {
        return playerName;
    }

    public Date getJoinDate() {
        return joinDate;
    }

    public Date getLastOnline() {
        if (this.player != null) {
            lastOnline = new Date();
        }
        return lastOnline;
    }

    public void setLastOnline(Date date) {
        this.lastOnline = date;
    }

    public void setTeleportCooldown(int cooldownTicks) {
        this.teleportCooldown = cooldownTicks;
    }

    public int getTeleportCooldown() {
        return teleportCooldown;
    }

    public int getExtraBlocks() {
        return extraBlocks;
    }

    public void setExtraBlocks(int extraBlocks) {
        this.extraBlocks = extraBlocks;
    }

    @Override
    public IChatComponent toChatMessage() {
        return LocalManager.get("Empires.format.citizen.short", playerName);
    }

    public boolean getFakePlayer() {
        return isFakePlayer;
    }

    public void setFakePlayer(boolean isFakePlayer) {
        this.isFakePlayer = isFakePlayer;
    }

    public static class Container extends ArrayList<Citizen> implements IChatFormat {

        public Citizen get(UUID uuid) {
            for (Citizen res : this) {
                if (res.getUUID().equals(uuid)) {
                    return res;
                }
            }
            return null;
        }

        public Citizen get(String username) {
            for (Citizen res : this) {
                if (res.getPlayerName().equals(username)) {
                    return res;
                }
            }
            return null;
        }

        public void remove(Citizen res) {
            /*
            for (Iterator<Plot> it = res.getCurrentEmpire().plotsContainer.asList().iterator(); it.hasNext(); ) {
                Plot plot = it.next();
                if (plot.ownersContainer.contains(res) && plot.ownersContainer.size() <= 1) {
                    it.remove();
                }
            }
            */
            super.remove(res);
        }

        public void remove(UUID uuid) {
            for(Iterator<Citizen> it = iterator(); it.hasNext();) {
                Citizen res = it.next();
                if(res.getUUID().equals(uuid)) {
                    it.remove();
                }
            }
        }

        public boolean contains(String username) {
            for (Citizen res : this) {
                if (res.getPlayerName().equals(username)) {
                    return true;
                }
            }
            return false;
        }

        public boolean contains(UUID uuid) {
            for (Citizen res : this) {
                if (res.getUUID().equals(uuid)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public IChatComponent toChatMessage() {
            IChatComponent root = new ChatComponentText("");

            for (Citizen res : this) {
                if (root.getSiblings().size() > 0) {
                    root.appendSibling(new ChatComponentFormatted("{7|, }"));
                }
                root.appendSibling(res.toChatMessage());
            }
            return root;
        }
    }
}