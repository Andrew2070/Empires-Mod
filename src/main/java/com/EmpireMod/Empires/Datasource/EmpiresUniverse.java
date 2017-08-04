package com.EmpireMod.Empires.Datasource;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.EmpireMod.Empires.Empires;
import com.EmpireMod.Empires.API.Commands.Command.CommandCompletion;
import com.EmpireMod.Empires.Configuration.Config;
import com.EmpireMod.Empires.Events.AllianceEvent;
import com.EmpireMod.Empires.Events.CitizenEvent;
import com.EmpireMod.Empires.Events.EmpireBlockEvent;
import com.EmpireMod.Empires.Events.EmpireEvent;
import com.EmpireMod.Empires.Events.PlotEvent;
import com.EmpireMod.Empires.Events.RankEvent;
import com.EmpireMod.Empires.Handlers.VisualsHandler;
import com.EmpireMod.Empires.Misc.Teleport.Teleport;
import com.EmpireMod.Empires.Utilities.PlayerUtils;
import com.EmpireMod.Empires.entities.Empire.AdminEmpire;
import com.EmpireMod.Empires.entities.Empire.Alliance;
import com.EmpireMod.Empires.entities.Empire.Bank;
import com.EmpireMod.Empires.entities.Empire.Citizen;
import com.EmpireMod.Empires.entities.Empire.Empire;
import com.EmpireMod.Empires.entities.Empire.EmpireBlock;
import com.EmpireMod.Empires.entities.Empire.Plot;
import com.EmpireMod.Empires.entities.Empire.Rank;
import com.EmpireMod.Empires.entities.Flags.Flag;
import com.EmpireMod.Empires.entities.Flags.FlagType;
import com.mojang.authlib.GameProfile;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

public class EmpiresUniverse { // TODO Allow migrating between different Datasources

    public static final EmpiresUniverse instance = new EmpiresUniverse();

    public final Citizen.Container citizens = new Citizen.Container();
    public final Empire.Container empires = new Empire.Container();
   // public final Map<String, Alliance> alliances = new HashMap<String, Alliance>();
    public final EmpireBlock.Container blocks = new EmpireBlock.Container();
    public final Plot.Container plots = new Plot.Container();
    public final Rank.Container ranks = new Rank.Container();
    public final Bank.Container banks = new Bank.Container();
    public final List<Integer> worlds = new ArrayList<Integer>();

    public EmpiresUniverse() {

    }

    /* ----- Create Entity ----- */

    /**
     * Creates and returns a new Empire with basic entities saved to db, or null if it couldn't be created
     */
    public final Empire newEmpire(String name, Citizen creator) {
        Empire empire = new Empire(name);
        configureEmpire(empire, creator);
        return empire;
    }

    /**
     * Creates and returns a new AdminEmpire and fires event
     */
    public final AdminEmpire newAdminEmpire(String name, Citizen creator) {
        AdminEmpire empire = new AdminEmpire(name);
        configureEmpire(empire, creator);
        return empire;
    }

    /**
     * Common method for creating any type of empire
     */
    @SuppressWarnings("unchecked")
    private void configureEmpire(Empire empire, Citizen creator) {
        for (World world : MinecraftServer.getServer().worldServers) {
            if (!EmpiresUniverse.instance.worlds.contains(world.provider.dimensionId)) {
                getDatasource().saveWorld(world.provider.dimensionId);
            }
        }
        /*
        for (int dim : EmpiresUniverse.instance.getWorldsList()) {
            if (MinecraftServer.getServer().worldServerForDimension(dim) == null) {
                deleteWorld(dim);
            }
        }
        */

        // Setting spawn before saving
        empire.setSpawn(new Teleport(creator.getPlayer().dimension, (float) creator.getPlayer().posX, (float) creator.getPlayer().posY, (float) creator.getPlayer().posZ, creator.getPlayer().cameraYaw, creator.getPlayer().cameraPitch));

        // Saving empire to database
        if (!getDatasource().saveEmpire(empire))
            throw new CommandException("Failed to save Empire");

        int chunkX = ((int)creator.getPlayer().posX) >> 4;
        int chunkZ = ((int)creator.getPlayer().posZ) >> 4;
        int dim = creator.getPlayer().dimension;

        //Claiming first block
        EmpireBlock block = newBlock(dim, chunkX, chunkZ, false, Config.instance.costAmountClaim.get(), empire);

        // Saving block to db and empire
        if(EmpiresUniverse.instance.blocks.contains(dim, chunkX, chunkZ)) {
            getDatasource().deleteEmpire(empire);
            throw new CommandException("Chunk at (" + dim + "," + chunkX + "," + chunkZ + ") is already claimed");
           // throw new EmpiresCommandException("Empires.cmd.err.claim.already");
        }

        getDatasource().saveBlock(block);

        // Saving and adding all flags to the database
        for (FlagType type : FlagType.values()) {
            if (type.isEmpirePerm) {
                getDatasource().saveFlag(new Flag(type, type.defaultValue), empire);
            }
        }

        if (!(empire instanceof AdminEmpire)) {
            // Saving all ranks to database and empire
            for (Rank template : Rank.defaultRanks) {
                Rank rank = new Rank(template.getName(), empire, template.getType());
                rank.permissionsContainer.addAll(template.permissionsContainer);

                getDatasource().saveRank(rank);
            }
            // Linking citizen to empire
            if (!getDatasource().linkCitizenToEmpire(creator, empire, empire.ranksContainer.getLeaderRank())) {
                Empires.instance.LOG.error("Problem linking citizen {} to empire {}", creator.getPlayerName(), empire.getName());
            }

            getDatasource().saveEmpireBank(empire.bank);
        }

        EmpireEvent.fire(new EmpireEvent.EmpireCreateEvent(empire));
    }
    
    /**
     * Creates and returns a new Block, or null if it couldn't be created
     */
    public final EmpireBlock newBlock(int dim, int x, int z, boolean isFarClaim, int pricePaid, Empire empire) {
        if(!worlds.contains(dim)) {
            getDatasource().saveWorld(dim);
        }

        EmpireBlock block = new EmpireBlock(dim, x, z, isFarClaim, pricePaid, empire);
        if (EmpireBlockEvent.fire(new EmpireBlockEvent.BlockCreateEvent(block)))
            return null;
        return block;
    }

    /**
     * Creates and returns a new Rank, or null if it couldn't be created
     */
    public final Rank newRank(String name, Empire empire, Rank.Type type) {
        Rank rank = new Rank(name, empire, type);
        if (RankEvent.fire(new RankEvent.RankCreateEvent(rank)))
            return null;
        return rank;
    }

    /**
     * Creates and returns a new Citizen, or null if it couldn't be created
     */
    public final Citizen newCitizen(UUID uuid, String playerName, boolean isFakePlayer) {
        Citizen citizen = new Citizen(uuid, playerName, isFakePlayer);

        if (CitizenEvent.fire(new CitizenEvent.CitizenCreateEvent(citizen)))
            return null;
        return citizen;
    }

    /**
     * Creates and returns a new Plot, or null if it couldn't be created
     */
    public final Plot newPlot(String name, Empire empire, int dim, int x1, int y1, int z1, int x2, int y2, int z2) {
        Plot plot = new Plot(name, empire, dim, x1, y1, z1, x2, y2, z2);
        if (PlotEvent.fire(new PlotEvent.PlotCreateEvent(plot)))
            return null;
        return plot;
    }

    /**
     * Creates and returns a new Alliance, or null if it couldn't be created
     */
    public final Alliance newAlliance(String name) {
        Alliance alliance = new Alliance(name);
        if (AllianceEvent.fire(new AllianceEvent.AllianceCreateEvent(alliance)))
            return null;
        return alliance;
    }

    /**
     * Creates and returns a new EmpireFlag or null if it couldn't be created
     */
    @SuppressWarnings("UnnecessaryLocalVariable")
    public final Flag newFlag(FlagType type, boolean value) {
        Flag flag = new Flag(type, value);
        //TODO: Fire event
        return flag;
    }

    public Citizen getOrMakeCitizen(UUID uuid, String playerName, boolean isFakePlayer) {
        Citizen res = instance.citizens.get(uuid);
        if (res == null) {
            res = instance.newCitizen(uuid, playerName, isFakePlayer);
            if (res != null && !getDatasource().saveCitizen(res)) { // Only save if a new Citizen
                return null;
            }
        }
        return res;
    }

    public Citizen getOrMakeCitizen(UUID uuid, String playerName) {
        return getOrMakeCitizen(uuid, playerName, true);
    }

    public Citizen getOrMakeCitizen(UUID uuid) {
        return getOrMakeCitizen(uuid, PlayerUtils.getUsernameFromUUID(uuid));
    }

    public Citizen getOrMakeCitizen(EntityPlayer player) {
        return getOrMakeCitizen(player.getPersistentID(), player.getDisplayName(), player instanceof FakePlayer);
    }

    public Citizen getOrMakeCitizen(Entity e) {
        if (e instanceof EntityPlayer) {
            return getOrMakeCitizen((EntityPlayer) e);
        }
        return null;
    }

    public Citizen getOrMakeCitizen(ICommandSender sender) {
        if (sender instanceof EntityPlayer) {
            return getOrMakeCitizen((EntityPlayer) sender);
        }
        return null;
    }

    public Citizen getOrMakeCitizen(String username) {
        if(username == null || username.isEmpty()) return null;
        GameProfile profile;
        try {
            profile = MinecraftServer.getServer().func_152358_ax().func_152655_a(username);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return profile == null ? null : getOrMakeCitizen(profile.getId(), profile.getName());
    }

    /* ----- Modifying Entity */

    public final void renameEmpire(Empire empire, String newName) {
        String oldName = empire.getName();
        empire.rename(newName);
        getDatasource().saveEmpire(empire);

        CommandCompletion.removeCompletion("empireCompletion", oldName);
        CommandCompletion.removeCompletion("empireCompletionAndAll", oldName);

        CommandCompletion.addCompletion("empireCompletion", newName);
        CommandCompletion.addCompletion("empireCompletionAndAll", newName);
    }

    /* ----- Add Entity ----- */

    public final void addCitizen(Citizen res) {
        citizens.add(res);
        CommandCompletion.addCompletion("citizenCompletion", res.getPlayerName());
    }

    public final void addEmpire(Empire empire) {
        empires.add(empire);
        CommandCompletion.addCompletion("empireCompletionAndAll", empire.getName());
        CommandCompletion.addCompletion("empireCompletion", empire.getName());
    }

    /*
    public final void addAlliance(Alliance alliance) {
        alliances.put(alliance.getName(), alliance);
        return true;
    }
    */

    public final void addEmpireBlock(EmpireBlock block) {
        blocks.add(block);
    }

    public final void addRank(Rank rank) {
        ranks.add(rank);
        CommandCompletion.addCompletion("rankCompletion", rank.getName());
    }

    public final void addPlot(Plot plot) {
        for (int x = plot.getStartChunkX(); x <= plot.getEndChunkX(); x++) {
            for (int z = plot.getStartChunkZ(); z <= plot.getEndChunkZ(); z++) {
                EmpireBlock b = blocks.get(plot.getDim(), x, z);
                if (b != null) {
                    b.plotsContainer.add(plot);
                }
            }
        }
        plots.add(plot);
        CommandCompletion.addCompletion("plotCompletion", plot.getName());
    }

    public final void addBank(Bank bank) {
        banks.add(bank);
    }


    public final void addWorld(int dim) {
        worlds.add(dim);
    }

    /* ----- Remove Entity ----- */

    public final void removeCitizen(Citizen res) {
        citizens.remove(res);
        CommandCompletion.removeCompletion("citizenCompletion", res.getPlayerName());
    }

    public final void removeEmpire(Empire empire) {
        empires.remove(empire);
        VisualsHandler.instance.unmarkBlocks(empire);
        CommandCompletion.removeCompletion("empireCompletionAndAll", empire.getName());
        CommandCompletion.removeCompletion("empireCompletion", empire.getName());
    }

    /*
    public final void removeAlliance(Alliance alliance) {
        alliances.remove(alliance.getName());
    }
    */

    public final void removeEmpireBlock(EmpireBlock block) {
        blocks.remove(block);
    }

    public final void removeRank(Rank rank) {
        ranks.remove(rank);
        // TODO: Check properly, although it's gonna fix itself on restart
    }

    public final void removePlot(Plot plot) {
        plots.remove(plot);

        boolean removeFromCompletionMap = true;
        for(Plot p : plots) {
            if(p.getName().equals(plot.getName()))
                removeFromCompletionMap = false;
        }
        if(removeFromCompletionMap)
            CommandCompletion.removeCompletion("plotCompletion", plot.getName());

        VisualsHandler.instance.unmarkBlocks(plot);
    }

    public final void removeWorld(int dim) {
        worlds.remove((Integer) dim);
    }

    public final void clear() {
        banks.clear();
        empires.clear();
        plots.clear();
        citizens.clear();
        blocks.clear();
        worlds.clear();
        ranks.clear();
    }

    /* ----- Utils ----- */
    private EmpiresDatasource getDatasource() {
        return Empires.instance.datasource;
    }
}