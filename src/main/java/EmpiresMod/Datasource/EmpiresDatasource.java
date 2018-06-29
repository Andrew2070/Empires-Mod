
package EmpiresMod.Datasource;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonSyntaxException;

import EmpiresMod.Empires;
import EmpiresMod.API.Commands.Command.CommandsEMP;
import EmpiresMod.Configuration.Config;
import EmpiresMod.Datasource.Schematics.DatasourceSQL;
import EmpiresMod.Datasource.Schematics.EmpiresSchematic;
import EmpiresMod.Handlers.EmpiresLoadingCallback;
import EmpiresMod.Misc.Teleport.Teleport;
import EmpiresMod.entities.Empire.AdminEmpire;
import EmpiresMod.entities.Empire.Bank;
import EmpiresMod.entities.Empire.BlockWhitelist;
import EmpiresMod.entities.Empire.Citizen;
import EmpiresMod.entities.Empire.Empire;
import EmpiresMod.entities.Empire.EmpireBlock;
import EmpiresMod.entities.Empire.Plot;
import EmpiresMod.entities.Empire.Rank;
import EmpiresMod.entities.Flags.Flag;
import EmpiresMod.entities.Flags.FlagType;
import EmpiresMod.exceptions.Command.CommandException;
import EmpiresMod.protection.ProtectionManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;

public class EmpiresDatasource extends DatasourceSQL {

  //  public static final EmpiresDatasource instance = new EmpiresDatasource();

    public EmpiresDatasource() {
        super(Empires.instance.LOG, Config.instance, new EmpiresSchematic());
    }

    @Override
    public boolean loadAll() {
        return loadWorlds() && loadEmpires() && loadRanks() && loadBlocks() && loadCitizens() &&
                loadPlots() && loadWarps() && /*loadNations() &&*/ loadEmpireFlags() && loadPlotFlags() &&
                loadBlockWhitelists() &&  loadEmpireInvites() && loadBlockOwners() && loadEmpireBanks() &&
                loadRankPermissions() && loadCitizensToEmpires() && /*loadEmpiresToNations() &&*/
                loadCitizensToPlots() && loadSelectedEmpires();
    }

    @Override
    public boolean checkAll() {
        return checkFlags() && checkEmpires();
    }
    
    protected boolean loadWorlds() {
        try {
            PreparedStatement s = prepare("SELECT * FROM " + prefix + "Worlds", true);
            ResultSet rs = s.executeQuery();

            while (rs.next()) {
                EmpiresUniverse.instance.addWorld(rs.getInt("dim"));
            }

        } catch (SQLException e) {
            LOG.error("Failed to load worlds from the database!");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }

        for (World world : MinecraftServer.getServer().worldServers) {
            if (!EmpiresUniverse.instance.worlds.contains(world.provider.dimensionId)) {
                saveWorld(world.provider.dimensionId);
            }
        }

        return true;
    }

    
    protected boolean loadEmpires() {
        try {
            PreparedStatement loadEmpiresStatement = prepare("SELECT * FROM " + prefix + "Empires", true);
            ResultSet rs = loadEmpiresStatement.executeQuery();
            while (rs.next()) {
                Empire empire;
                if (rs.getBoolean("isAdminEmpire")) {
                    empire = new AdminEmpire(rs.getString("name"));
                } else {
                    empire = new Empire(rs.getString("name"));
                }
                empire.setSpawn(new Teleport("Spawn", empire, rs.getInt("spawnDim"), rs.getFloat("spawnX"), rs.getFloat("spawnY"), rs.getFloat("spawnZ"), rs.getFloat("cameraYaw"), rs.getFloat("cameraPitch")));
                empire.empireBlocksContainer.setExtraBlocks(rs.getInt("extraBlocks"));
                empire.empireBlocksContainer.setExtraFarClaims(rs.getInt("extraFarClaims"));
                empire.plotsContainer.setMaxPlots(rs.getInt("maxPlots"));
                empire.setPower(rs.getDouble("currentPower"));
                empire.setDesc(rs.getString("desc"));
                System.out.println("SQL: Empire: " + empire + " Desc: " + rs.getString("desc"));

                for (ForgeChunkManager.Ticket ticket : EmpiresLoadingCallback.tickets) {
                    if (ticket.getModData().getString("empireName").equals(empire.getName())) {
                        empire.ticketMap.put(ticket.world.provider.dimensionId, ticket);
                    }
                }
                System.out.println("Empire: " + empire + " Desc: " + rs.getString("desc"));

                EmpiresUniverse.instance.addEmpire(empire);
                System.out.println("DescDebug: After datasource load: " + empire.getDesc());
            }
        } catch (SQLException e) {
            LOG.error("Failed to load Empires!");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }

        return true;
    }

    
    protected boolean loadBlocks() {
        try {
            PreparedStatement loadBlocksStatement = prepare("SELECT * FROM " + prefix + "Blocks", true);
            ResultSet rs = loadBlocksStatement.executeQuery();

            while (rs.next()) {
                Empire empire = getUniverse().empires.get(rs.getString("empireName"));
                EmpireBlock block = new EmpireBlock(rs.getInt("dim"), rs.getInt("x"), rs.getInt("z"), rs.getBoolean("isFarClaim"), rs.getInt("pricePaid"), empire);

                empire.empireBlocksContainer.add(block);

                EmpiresUniverse.instance.addEmpireBlock(block);
            }
        } catch (SQLException e) {
            LOG.error("Failed to load blocks!");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }

        return true;
    }
    
   protected boolean loadWarps() {
        try {
            PreparedStatement loadWarpStatement = prepare("SELECT * FROM " + prefix + "Warps", true);
            ResultSet rs = loadWarpStatement.executeQuery();

            while (rs.next()) {
                Empire empire = getUniverse().empires.get(rs.getString("empireName"));
                String warpname = rs.getString("name");
                int dimension = rs.getInt("dim");
                String empirename = empire.getName();
                float posX = rs.getFloat("x");
                float posY = rs.getFloat("y");
                float posZ = rs.getFloat("z");
                float yaw = rs.getFloat("yaw");
                float pitch = rs.getFloat("pitch");
                int id = rs.getInt("ID");
                
                Teleport Warp = new Teleport((String) warpname,(Empire) empire, dimension, (float) posX, (float) posY, (float) posZ, (float) yaw, (float) pitch);        
                Warp.setDim(dimension).setPosition((float) posX, (float) posY, (float) posZ).setRotation(yaw, pitch);
              //  System.out.println("SQL: line 164 :DEBUG: loadwarps(): " + " NAME: "+ warpname + " SelectedSQLEmpire: "+ empire.getName() +" WarpsEmpire: " + empirename + " Dim: "+ dimension + " X: " + posX + " Y: " + posY + " Z: "+ posZ + " YAW: " + yaw + " PITCH: " + pitch);
                Warp.setDbID(id);
                
                empire.setWarps(Warp);
             //   System.out.println("DEBUG SQL: load warp-- > Name: " + Warp.getName() + " DbID: " + Warp.getDbID()+ " Key: " + Warp.getKey()+ " Empire: " + Warp.getEmpire()+ " X: " + Warp.getX()+ " Z: " + Warp.getZ());
                
            }
            
        } catch (SQLException e) {
            LOG.error("Failed to load warps!");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }

        return true;
  }

    protected boolean loadRanks() {
        try {
            PreparedStatement loadRanksStatement = prepare("SELECT * FROM " + prefix + "Ranks", true);
            ResultSet rs = loadRanksStatement.executeQuery();
            while (rs.next()) {
                Empire empire = getUniverse().empires.get(rs.getString("empireName"));
                if (empire == null) {
                    LOG.error("A rank from the database does not belong to any empire. Deleting...");
                    PreparedStatement ds = prepare("DELETE FROM Ranks WHERE name=? AND empireName=?", false);
                    ds.executeUpdate();
                    continue;
                }

                Rank rank = new Rank(rs.getString("name"), empire, Rank.Type.valueOf(rs.getString("type")));

                LOG.debug("Loading Rank %s for Empire {}", rank.getName(), empire.getName());

                empire.ranksContainer.add(rank);
                EmpiresUniverse.instance.addRank(rank);
            }
        } catch (SQLException e) {
            LOG.error("Failed to load a rank!");
            LOG.error(ExceptionUtils.getStackTrace(e));
        }
        return true;
    }

    
    protected boolean loadRankPermissions() {
        try {
            PreparedStatement loadRankPermsStatement = prepare("SELECT * FROM " + prefix + "RankPermissions", true);
            ResultSet rs = loadRankPermsStatement.executeQuery();
            while(rs.next()) {
                Empire empire = getUniverse().empires.get(rs.getString("empireName"));
                Rank rank = empire.ranksContainer.get(rs.getString("rank"));

                rank.permissionsContainer.add(rs.getString("node"));
            }
        } catch (SQLException e) {
            LOG.error("Failed to load Rank Permissions!");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    
    public boolean loadCitizens() { //was protected boolean
        try {
            PreparedStatement loadCitizensStatement = prepare("SELECT * FROM " + prefix + "Citizens", true);
            ResultSet rs = loadCitizensStatement.executeQuery();

            while (rs.next()) {
                Citizen res = new Citizen(UUID.fromString(rs.getString("uuid")), rs.getString("name"), rs.getLong("joined"), rs.getLong("lastOnline"));
                res.setExtraBlocks(rs.getInt("extraBlocks"));
                res.setPower(rs.getDouble("power"));
                res.setFakePlayer(rs.getBoolean("fakePlayer"));
                res.setLoadPowerTime(rs.getLong("lastPowerUpdateTime"));
                res.setBanned(rs.getBoolean("isBanned"));

                EmpiresUniverse.instance.addCitizen(res);
            }
        } catch (SQLException e) {
            LOG.error("Failed to load Citizens!");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }

        return true;
    }

    
    protected boolean loadPlots() {
        try {
            PreparedStatement loadPlotsStatement = prepare("SELECT * FROM " + prefix + "Plots", true);
            ResultSet rs = loadPlotsStatement.executeQuery();

            while (rs.next()) {
                Empire empire = getUniverse().empires.get(rs.getString("empireName"));
                Plot plot = new Plot(rs.getString("name"), empire, rs.getInt("dim"), rs.getInt("x1"), rs.getInt("y1"), rs.getInt("z1"), rs.getInt("x2"), rs.getInt("y2"), rs.getInt("z2"));
                plot.setDbID(rs.getInt("ID"));

                empire.plotsContainer.add(plot);

                EmpiresUniverse.instance.addPlot(plot);
            }
        } catch (SQLException e) {
            LOG.error("Failed to load Plots!");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }

        return true;
    }



    @SuppressWarnings("unchecked")
    
    protected boolean loadEmpireFlags() {
        try {
            PreparedStatement loadFlagsStatement = prepare("SELECT * FROM " + prefix + "EmpireFlags", true);
            ResultSet rs = loadFlagsStatement.executeQuery();

            while (rs.next()) {
                String empireName = rs.getString("empireName");
                String flagName = rs.getString("name");
                String serializedValue = rs.getString("serializedValue");

                FlagType flagType;
                try {
                    flagType = FlagType.valueOf(flagName);
                } catch (IllegalArgumentException ex) {
                    LOG.error("Flag {} does no longer exist... will be deleted shortly from the database.", flagName);
                    LOG.error(ExceptionUtils.getStackTrace(ex));
                    PreparedStatement removeFlag = prepare("DELETE FROM " + prefix + "EmpireFlags WHERE empireName=? AND name=?", true);
                    removeFlag.setString(1, empireName);
                    removeFlag.setString(2, flagName);
                    removeFlag.executeUpdate();
                    continue;
                }

                Flag flag;
                try {
                    flag = new Flag(flagType, serializedValue);
                }  catch (JsonSyntaxException ex) {
                    LOG.error("Flag {} has an invalid value... reverting to default", flagName);
                    LOG.error(ExceptionUtils.getStackTrace(ex));
                    PreparedStatement updateFlag = prepare("UPDATE " + prefix + "EmpireFlags SET serializedValue=? WHERE empireName=? AND name=?", false);
                    updateFlag.setString(1, flagType.serializeValue(flagType.defaultValue));
                    updateFlag.setString(2, empireName);
                    updateFlag.setString(3, flagName);
                    updateFlag.executeUpdate();

                    flag = new Flag(flagType, flagType.defaultValue);
                }

                Empire empire = getUniverse().empires.get(empireName);
                empire.flagsContainer.add(flag);
            }
        } catch (SQLException e) {
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    
    public boolean loadPlotFlags() {
        try {
            PreparedStatement loadFlagsStatement = prepare("SELECT * FROM " + prefix + "PlotFlags", true);
            ResultSet rs = loadFlagsStatement.executeQuery();

            while (rs.next()) {
                int plotID = rs.getInt("plotID");
                String flagName = rs.getString("name");
                String serializedValue = rs.getString("serializedValue");

                FlagType flagType;
                try {
                    flagType = FlagType.valueOf(flagName);
                } catch (IllegalArgumentException ex) {
                    LOG.error("Flag {} does no longer exist... will be deleted shortly from the database.", flagName);
                    LOG.error(ExceptionUtils.getStackTrace(ex));
                    PreparedStatement removeFlag = prepare("DELETE FROM " + prefix + "PlotFlags WHERE plotID=? AND name=?", true);
                    removeFlag.setInt(1, plotID);
                    removeFlag.setString(2, flagName);
                    removeFlag.executeUpdate();
                    continue;
                }

                Flag flag;
                try {
                    flag = new Flag(flagType, serializedValue);
                }  catch (JsonSyntaxException ex) {
                    LOG.error("Flag {} has an invalid value... reverting to default", flagName);
                    LOG.error(ExceptionUtils.getStackTrace(ex));
                    PreparedStatement updateFlag = prepare("UPDATE " + prefix + "PlotFlags SET serializedValue=? WHERE plotID=? AND name=?", false);
                    updateFlag.setString(1, flagType.serializeValue(flagType.defaultValue));
                    updateFlag.setInt(2, plotID);
                    updateFlag.setString(3, flagName);
                    continue;
                }

                Plot plot = getUniverse().plots.get(plotID);
                plot.flagsContainer.add(flag);
            }
        } catch (SQLException e) {
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }

        return true;
    }

    
    protected boolean loadCitizensToEmpires() {
        try {
            PreparedStatement statement = prepare("SELECT * FROM " + prefix + "CitizensToEmpires", true);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Citizen res = getUniverse().citizens.get(UUID.fromString(rs.getString("citizen")));
                Empire empire = getUniverse().empires.get(rs.getString("empire"));
                Rank rank = empire.ranksContainer.get(rs.getString("rank"));

                empire.citizensMap.put(res, rank);
                res.empiresContainer.add(empire);
            }
        } catch (SQLException e) {
            LOG.error("Failed to link Citizens to Empires!");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }

        return true;
    }

    
    protected boolean loadBlockWhitelists() {
        try {
            PreparedStatement statement = prepare("SELECT * FROM " + prefix + "BlockWhitelists", true);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                // plotID will be 0 if it's a empire's whitelist
                BlockWhitelist bw = new BlockWhitelist(rs.getInt("dim"), rs.getInt("x"), rs.getInt("y"), rs.getInt("z"), FlagType.valueOf(rs.getString("flagName")));
                bw.setDbID(rs.getInt("ID"));
                Empire empire = getUniverse().empires.get(rs.getString("empireName"));
                /*
                empire.addBlockWhitelist(bw);
                */
            }

        } catch (SQLException e) {
            LOG.error("Failed to load a Block whitelist");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    
    protected boolean loadCitizensToPlots() {
        try {
            PreparedStatement loadStatement = prepare("SELECT * FROM " + prefix + "CitizensToPlots", true);
            ResultSet rs = loadStatement.executeQuery();

            while (rs.next()) {
                Plot plot = getUniverse().plots.get(rs.getInt("plotID"));
                Citizen res = getUniverse().citizens.get(UUID.fromString(rs.getString("citizen")));

                if (rs.getBoolean("isOwner")) {
                    plot.ownersContainer.add(res);
                } else {
                    plot.membersContainer.add(res);
                }
            }
        } catch (SQLException e) {
            LOG.error("Failed to link Citizens to Plots");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    
    protected boolean loadEmpireInvites() {
        try {
            PreparedStatement s = prepare("SELECT * FROM " + prefix + "EmpireInvites", true);
            ResultSet rs = s.executeQuery();
            while (rs.next()) {
                Citizen res = getUniverse().citizens.get(UUID.fromString(rs.getString("citizen")));
                Empire empire = getUniverse().empires.get(rs.getString("empireName"));

                res.empireInvitesContainer.add(empire);
            }
        } catch (SQLException e) {
            LOG.error("Failed to load empire invites.");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }

        return true;
    }
    
    protected boolean loadEmpireBans() {
        try {
            PreparedStatement s = prepare("SELECT * FROM " + prefix + "EmpireBans", true);
            ResultSet rs = s.executeQuery();
            while (rs.next()) {
                Citizen res = getUniverse().citizens.get(UUID.fromString(rs.getString("citizen")));
                Empire empire = getUniverse().empires.get(rs.getString("empireName"));
                res.empireBansContainer.add(empire);
            }
        } catch (SQLException e) {
            LOG.error("Failed to load empire bans.");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }

        return true;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    
    protected boolean loadBlockOwners() {
        try {
            PreparedStatement s = prepare("SELECT * FROM " + prefix + "BlockOwners", true);
            ResultSet rs = s.executeQuery();
            while(rs.next()) {
                Citizen res = getUniverse().citizens.get(UUID.fromString(rs.getString("citizen")));
                int dim = rs.getInt("dim");
                int x = rs.getInt("x");
                int y = rs.getInt("y");
                int z = rs.getInt("z");

                World world = MinecraftServer.getServer().worldServerForDimension(dim);
                if(world == null) {
                    LOG.error("Failed to find a TileEntity at position ({}, {}, {}| DIM: {})", x, y, z, dim);
                    continue;
                }

                TileEntity te = world.getTileEntity(x, y, z);
                if(te == null) {
                    LOG.error("Failed to find a TileEntity at position ({}, {}, {}| DIM: {})", x, y, z, dim);
                    continue;
                }
                ProtectionManager.addTileEntity(te, res);
            }
        } catch (SQLException e) {
            LOG.error("Failed to load block owners.");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    
    protected boolean loadEmpireBanks() {
        try {
            PreparedStatement s = prepare("SELECT * FROM " + prefix + "EmpireBanks", true);
            ResultSet rs = s.executeQuery();
            while(rs.next()) {
                Empire empire = getUniverse().empires.get(rs.getString("empireName"));

                empire.bank.setAmount(rs.getInt("amount"));
                empire.bank.setDaysNotPaid(rs.getInt("daysNotPaid"));

                getUniverse().addBank(empire.bank);
            }
        } catch (SQLException e) {
            LOG.error("Failed to load empire banks.");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    
    protected boolean loadSelectedEmpires() {
        try {
            PreparedStatement statement = prepare("SELECT * FROM " + prefix + "SelectedEmpire", true);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Citizen res = getUniverse().citizens.get(UUID.fromString(rs.getString("citizen")));
                Empire empire = getUniverse().empires.get(rs.getString("empireName"));
                res.empiresContainer.isSelectedEmpireSaved = true;
                res.empiresContainer.setMainEmpire(empire);
            }
        } catch (SQLException e) {
            LOG.error("Failed to load a empire selection.");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    /* ----- Save ----- */

    
    public boolean saveEmpire(Empire empire) {
        LOG.debug("Saving Empire {}", empire.getName());
        try {
            if (getUniverse().empires.contains(empire)) { // Update
                PreparedStatement updateStatement = prepare("UPDATE " + prefix + "Empires SET name=?, desc=?, spawnDim=?, spawnX=?, spawnY=?, spawnZ=?, cameraYaw=?, cameraPitch=?, extraBlocks=?, maxPlots=?, extraFarClaims=?, currentPower=? WHERE name=?", true);
                updateStatement.setString(1, empire.getName());
                updateStatement.setString(2, empire.getDesc());
                updateStatement.setInt(3, empire.getSpawn().getDim());
                updateStatement.setFloat(4, empire.getSpawn().getX());
                updateStatement.setFloat(5, empire.getSpawn().getY());
                updateStatement.setFloat(6, empire.getSpawn().getZ());
                updateStatement.setFloat(7, empire.getSpawn().getYaw());
                updateStatement.setFloat(8, empire.getSpawn().getPitch());
                updateStatement.setInt(9, empire.empireBlocksContainer.getExtraBlocks());
                updateStatement.setInt(10, empire.plotsContainer.getMaxPlots());
                updateStatement.setInt(11, empire.empireBlocksContainer.getExtraFarClaims());
                updateStatement.setDouble(12, empire.getPower());
                System.out.println("DescDebug: After datasource save: " + empire.getDesc());
                	
               // LOG.info(empire.getName() + " " + empire.getSpawn().getDim() + " " + empire.getSpawn().getX() + " " + empire.getSpawn().getY() + " " + empire.getSpawn().getZ() + " " + empire.getSpawn().getYaw() + " " + empire.getSpawn().getPitch() + " " + empire.empireBlocksContainer.getExtraBlocks() + " " + empire.plotsContainer.getMaxPlots() + " " + empire.empireBlocksContainer.getExtraFarClaims() + " " + empire.getPower() + " " + empire.getMaxPower());
                

                if (empire.getOldName() == null)
                    updateStatement.setString(13, empire.getName());
                else
                    updateStatement.setString(13, empire.getOldName());

                updateStatement.executeUpdate();

                // Need to move the Empire in the map from the old name to the new
                if (empire.getOldName() != null) {
                    EmpiresUniverse.instance.removeEmpire(empire);
                    // This updates the name
                    EmpiresUniverse.instance.addEmpire(empire);
                }
                empire.resetOldName();
            } else { // Insert
                PreparedStatement insertStatement = prepare("INSERT INTO " + prefix + "Empires (name, desc, spawnDim, spawnX, spawnY, spawnZ, cameraYaw, cameraPitch, isAdminEmpire, extraBlocks, maxPlots, extraFarClaims, currentPower) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", true);
                insertStatement.setString(1, empire.getName());
                insertStatement.setString(2, empire.getDesc());
                insertStatement.setInt(3, empire.getSpawn().getDim());
                insertStatement.setFloat(4, empire.getSpawn().getX());
                insertStatement.setFloat(5, empire.getSpawn().getY());
                insertStatement.setFloat(6, empire.getSpawn().getZ());
                insertStatement.setFloat(7, empire.getSpawn().getYaw());
                insertStatement.setFloat(8, empire.getSpawn().getPitch());
                insertStatement.setBoolean(9, empire instanceof AdminEmpire);
                insertStatement.setInt(10, empire.empireBlocksContainer.getExtraBlocks());
                insertStatement.setInt(11, empire.plotsContainer.getMaxPlots());
                insertStatement.setInt(12, empire.empireBlocksContainer.getExtraFarClaims());
                insertStatement.setDouble(13, empire.getPower());
                LOG.info("New Empire" + empire.getName() + " Created " + " World: " + empire.getSpawn().getDim() + " X Coord: " + empire.getSpawn().getX() + " Y Coord: " + empire.getSpawn().getY() + " Z Coord: " + empire.getSpawn().getZ() + " YAW: " + empire.getSpawn().getYaw() + " PITCH: " + empire.getSpawn().getPitch() + " EXTRA CLAIMS: " + empire.empireBlocksContainer.getExtraBlocks() + " PLOTS: " + empire.plotsContainer.getMaxPlots() + " FAR CLAIMS: " + empire.empireBlocksContainer.getExtraFarClaims() + " POWER: " + empire.getPower() + " MAX POWER: " + empire.getMaxPower() + " DESC: " + empire.getDesc());
                System.out.println("DescDebug: After datasource save: " + empire.getDesc());
                insertStatement.executeUpdate();

                // Put the Empire in the Map
                EmpiresUniverse.instance.addEmpire(empire);
            }
        } catch (SQLException e) {
            LOG.error("Failed to save Empire {}!", empire.getName());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }

        return true;
    }
    
    
    public boolean saveBlock(EmpireBlock block) {
        LOG.debug("Saving EmpireBlock {}", block.getKey());
        try {
            if (getUniverse().blocks.contains(block)) { // Update
                // TODO Update Block (If needed?)
            } else { // Insert
                PreparedStatement insertStatement = prepare("INSERT INTO " + prefix + "Blocks (dim, x, z, isFarClaim, pricePaid, empireName) VALUES (?, ?, ?, ?, ?, ?)", true);
                insertStatement.setInt(1, block.getDim());
                insertStatement.setInt(2, block.getX());
                insertStatement.setInt(3, block.getZ());
                insertStatement.setBoolean(4, block.isFarClaim());
                insertStatement.setInt(5, block.getPricePaid());
                insertStatement.setString(6, block.getEmpire().getName());
                insertStatement.executeUpdate();

                // Put the Block in the Map
                EmpiresUniverse.instance.addEmpireBlock(block);

                block.getEmpire().empireBlocksContainer.add(block);
            }
        } catch (SQLException e) {
            LOG.error("Failed to save Block {}!", block.getKey());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    
    public boolean saveRank(Rank rank) { // TODO Insert any new permissions to the RankPermission table
        LOG.debug("Saving Rank {} in empire {}", rank.getName(), rank.getEmpire().getName());
        try {
            if (getUniverse().ranks.contains(rank)) { // Update
                try {
                    bridge.getConnection().setAutoCommit(false);

                    PreparedStatement s = prepare("UPDATE " + prefix + "Ranks SET type=?, name=? WHERE name=? AND empireName=?", true);
                    s.setString(1, rank.getType().toString());
                    if (rank.getNewName() == null) {
                        s.setString(2, rank.getName());
                    } else {
                        s.setString(2, rank.getNewName());
                        rank.resetNewName();
                    }
                    s.setString(3, rank.getName());
                    s.setString(4, rank.getEmpire().getName());
                    s.executeUpdate();

                    s = prepare("DELETE FROM " + prefix + "RankPermissions WHERE rank=? AND empireName=?", true);
                    s.setString(1, rank.getName());
                    s.setString(2, rank.getEmpire().getName());
                    s.executeUpdate();

                    if (!rank.permissionsContainer.isEmpty()) {
                        s = prepare("INSERT INTO " + prefix + "RankPermissions(node, rank, empireName) VALUES(?, ?, ?)", true);
                        for (String perm : rank.permissionsContainer) {
                            s.setString(1, perm);
                            s.setString(2, rank.getName());
                            s.setString(3, rank.getEmpire().getName());
                            s.addBatch();
                        }
                        s.executeBatch();
                    }
                } catch (SQLException e) {
                    LOG.error("Failed to update Rank {} in empire {}", rank.getName(), rank.getEmpire().getName());
                    LOG.error(ExceptionUtils.getStackTrace(e));
                    bridge.getConnection().rollback();
                    return false;
                } finally {
                    bridge.getConnection().setAutoCommit(true);
                }
            } else { // Insert
                try {
                    bridge.getConnection().setAutoCommit(false);

                    PreparedStatement insertRankStatement = prepare("INSERT INTO " + prefix + "Ranks (name, empireName, type) VALUES(?, ?, ?)", true);
                    insertRankStatement.setString(1, rank.getName());
                    insertRankStatement.setString(2, rank.getEmpire().getName());
                    insertRankStatement.setString(3, rank.getType().toString());
                    insertRankStatement.executeUpdate();

                    if (!rank.permissionsContainer.isEmpty()) {
                        PreparedStatement insertRankPermStatement = prepare("INSERT INTO " + prefix + "RankPermissions(node, rank, empireName) VALUES(?, ?, ?)", true);
                        for (String perm : rank.permissionsContainer) {
                            insertRankPermStatement.setString(1, perm);
                            insertRankPermStatement.setString(2, rank.getName());
                            insertRankPermStatement.setString(3, rank.getEmpire().getName());
                            insertRankPermStatement.addBatch();
                        }
                        insertRankPermStatement.executeBatch();
                    }

                    // Put the Rank in the Map
                    EmpiresUniverse.instance.addRank(rank);
                    rank.getEmpire().ranksContainer.add(rank);
                } catch (SQLException e) {
                    LOG.error("Failed to insert Rank {} in empire {}", rank.getName(), rank.getEmpire().getName());
                    LOG.error(ExceptionUtils.getStackTrace(e));
                    bridge.getConnection().rollback();
                    return false;
                } finally {
                    bridge.getConnection().setAutoCommit(true);
                }
            }
        } catch (SQLException e) {
            LOG.error("Failed to save Rank {} in Empire {}", rank.getName(), rank.getEmpire().getName());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    
    public boolean saveRankPermission(Rank rank, String perm) {
        LOG.debug("Saving RankPermission {} for Rank {} in Empire {}", perm, rank.getName(), rank.getEmpire().getName());
        try {
            PreparedStatement s = prepare("INSERT INTO " + prefix + "RankPermissions (node, rank, empireName) VALUES(?, ?, ?)", true);
            s.setString(1, perm);
            s.setString(2, rank.getName());
            s.setString(3, rank.getEmpire().getName());
            s.execute();

            rank.permissionsContainer.add(perm);
        } catch (SQLException e) {
            LOG.error("Failed to add permission ({}) to Rank ({})", perm, rank.getName());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    
    public boolean saveCitizen(Citizen citizen) {
        LOG.debug("Saving Citizen {} ({})", citizen.getUUID(), citizen.getPlayerName());
        try {
            if (getUniverse().citizens.contains(citizen.getUUID())) { // Update
                PreparedStatement updateStatement = prepare("UPDATE " + prefix + "Citizens SET name=?, lastOnline=?, extraBlocks=?, power=?, lastPowerUpdateTime=?, isBanned=?, fakePlayer=? WHERE uuid=?", true);
                updateStatement.setString(1, citizen.getPlayerName());
                updateStatement.setLong(2, citizen.getLastOnline().getTime() / 1000L); 
                updateStatement.setInt(3, citizen.getExtraBlocks());
                updateStatement.setBoolean(7, citizen.getFakePlayer());
                updateStatement.setBoolean(6, citizen.getBanned());
                updateStatement.setString(8, citizen.getUUID().toString());
                updateStatement.setDouble(4, citizen.getPower());
                updateStatement.setLong(5, citizen.getLastPowerUpdateTime());
                updateStatement.executeUpdate();
                
                double power7 = citizen.getPower();
                EmpiresUniverse.instance.addCitizen(citizen);
                
                //LOG.info("Power is: " + power7);

            } else { // Insert
                PreparedStatement insertStatement = prepare("INSERT INTO " + prefix + "Citizens (uuid, name, joined, lastOnline, extraBlocks, power, lastPowerUpdateTime, fakePlayer, isBanned) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)", true);
                insertStatement.setString(1, citizen.getUUID().toString());
                insertStatement.setString(2, citizen.getPlayerName());
                insertStatement.setLong(3, citizen.getJoinDate().getTime() / 1000L);
                insertStatement.setLong(4, citizen.getLastOnline().getTime() / 1000L); 
                insertStatement.setInt(5, citizen.getExtraBlocks());
                insertStatement.setBoolean(8, citizen.getFakePlayer());
                insertStatement.setBoolean(9, citizen.getBanned());
                insertStatement.setDouble(6, citizen.getPower());
                insertStatement.setLong(7, citizen.getLastPowerUpdateTime());
                insertStatement.executeUpdate();

                // Put the Citizen in the Map
                double power6 = citizen.getPower();
                EmpiresUniverse.instance.addCitizen(citizen);
                
             //   LOG.info("Power is: " + power6);
                // This is a test to see if the DB saves correctly or not.
            }
        } catch (SQLException e) {
            LOG.error("Failed to save citizen {}!", citizen.getUUID());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    
    public boolean savePlot(Plot plot) {
        LOG.debug("Saving Plot {} for Empire {}", plot.getKey(), plot.getEmpire().getName());
        try {
            if (getUniverse().plots.contains(plot)) { // Update
                PreparedStatement statement = prepare("UPDATE " + prefix + "Plots SET name=?, dim=?, x1=?, y1=?, z1=?, x2=?, y2=?, z2=? WHERE ID=?", true);
                statement.setString(1, plot.getName());
                statement.setInt(2, plot.getDim());
                statement.setInt(3, plot.getStartX());
                statement.setInt(4, plot.getStartY());
                statement.setInt(5, plot.getStartZ());
                statement.setInt(6, plot.getEndX());
                statement.setInt(7, plot.getEndY());
                statement.setInt(8, plot.getEndZ());
                statement.setInt(9, plot.getDbID());
                statement.executeUpdate();
            } else { // Insert
                PreparedStatement insertStatement = prepare("INSERT INTO " + prefix + "Plots (name, dim, x1, y1, z1, x2, y2, z2, empireName) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)", true);
                insertStatement.setString(1, plot.getName());
                insertStatement.setInt(2, plot.getDim());
                insertStatement.setInt(3, plot.getStartX());
                insertStatement.setInt(4, plot.getStartY());
                insertStatement.setInt(5, plot.getStartZ());
                insertStatement.setInt(6, plot.getEndX());
                insertStatement.setInt(7, plot.getEndY());
                insertStatement.setInt(8, plot.getEndZ());
                insertStatement.setString(9, plot.getEmpire().getName());
                insertStatement.executeUpdate();

                ResultSet generatedKeys = insertStatement.getGeneratedKeys();
                if (generatedKeys.next())
                    plot.setDbID(generatedKeys.getInt(1));

                for (Flag flag : plot.getEmpire().flagsContainer) {
                    if(flag.flagType.isPlotPerm) {
                        saveFlag(new Flag(flag.flagType, flag.value), plot);
                    }
                }

                EmpiresUniverse.instance.addPlot(plot);
                plot.getEmpire().plotsContainer.add(plot);
            }
        } catch (SQLException e) {
            LOG.error("Failed to save Plot {}!", plot.getKey());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    public boolean saveWarps(Teleport Warp) {
        LOG.debug("Saving Empire Warps ");
        try {
        	if (getUniverse().Warps.contains(Warp)) {

        		 PreparedStatement updateStatement = prepare("UPDATE " + prefix + "Warps SET name=?, dim=?, x=?, y=?, z=?, yaw=?, pitch=? WHERE ID=? AND empireName=?", true);
                 updateStatement.setString(1, Warp.getName());
        		 updateStatement.setInt(2, Warp.getDim());
                 updateStatement.setFloat(3, Warp.getX());
                 updateStatement.setFloat(4, Warp.getY());
                 updateStatement.setFloat(5, Warp.getZ());
                 updateStatement.setFloat(6, Warp.getYaw());
                 updateStatement.setFloat(7, Warp.getPitch());
                 updateStatement.setInt(8, Warp.getDbID());
                 updateStatement.setString(9, Warp.getEmpire().getName());
                 updateStatement.executeUpdate();
                 //System.out.println("DEBUG SQL saveWarp:update: save command -- > Name: " + Warp.getName() + " DbID: " + Warp.getDbID()+ " Key: " + Warp.getKey()+ " Empire: " + Warp.getEmpire()+ " X: " + Warp.getX()+ " Z: " + Warp.getZ());
              
        	} else {
        		  PreparedStatement insertStatement = prepare("INSERT INTO " + prefix + "Warps(name, dim, x, y, z, yaw, pitch, ID, empireName) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", true);
                  insertStatement.setString(1, Warp.getName());
                  insertStatement.setInt(2, Warp.getDim());
                  insertStatement.setFloat(3, Warp.getX());
                  insertStatement.setFloat(4, Warp.getY());
                  insertStatement.setFloat(5, Warp.getZ());
                  insertStatement.setFloat(6, Warp.getYaw());
                  insertStatement.setFloat(7, Warp.getPitch());
                  insertStatement.setInt(8, Warp.getDbID());
                  insertStatement.setString(9, Warp.getEmpire().getName());
                  insertStatement.executeUpdate();
                  ResultSet generatedKeys = insertStatement.getGeneratedKeys();
                  if (generatedKeys.next()) {
                	  Warp.setDbID(generatedKeys.getInt(1));
                  	 Warp.getEmpire().setWarps(Warp);
                  	  //System.out.println("DEBUG SQL saveWarp:insert: save command -- > Name: " + Warp.getName() + " DbID: " + Warp.getDbID()+ " Key: " + Warp.getKey()+ " Empire: " + Warp.getEmpire()+ " X: " + Warp.getX()+ " Z: " + Warp.getZ());
                  }
                  
        	}
      
        } catch (SQLException e) {
            LOG.error("Failed to save warps for empire {}!", Warp.getEmpire().getName());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }
    public boolean saveFlag(Flag flag, Plot plot) {
        LOG.debug("Saving Flag {} for Plot {}", flag.flagType.name, plot.getKey());
        try {
            if (plot.flagsContainer.contains(flag.flagType)) {
                // Update
                PreparedStatement s = prepare("UPDATE " + prefix + "PlotFlags SET serializedValue=? WHERE plotID=? AND name=?", true);
                s.setString(1, flag.flagType.serializeValue(flag.value));
                s.setInt(2, plot.getDbID());
                s.setString(3, flag.flagType.name);
                s.executeUpdate();


            } else {
                // Insert
                PreparedStatement s = prepare("INSERT INTO " + prefix + "PlotFlags(name, serializedValue, plotID) VALUES(?, ?, ?)", true);
                s.setString(1, flag.flagType.name);
                s.setString(2, flag.flagType.serializeValue(flag.value));
                s.setInt(3, plot.getDbID());
                s.executeUpdate();

                plot.flagsContainer.add(flag);
            }
        } catch (SQLException e) {
            LOG.error("Failed to save Flag {}!", flag.flagType.name);
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    
    public boolean saveFlag(Flag flag, Empire empire) {
        LOG.debug("Saving Flag {} for Empire {}", flag.flagType.name, empire.getName());
        try {
            if (empire.flagsContainer.contains(flag.flagType)) {
                // Update
                PreparedStatement updateStatement = prepare("UPDATE " + prefix + "EmpireFlags SET serializedValue=? WHERE empireName=? AND name=?", true);
                updateStatement.setString(1, flag.flagType.serializeValue(flag.value));
                updateStatement.setString(2, empire.getName());
                updateStatement.setString(3, flag.flagType.name);
                updateStatement.executeUpdate();

            } else {
                // Insert
                PreparedStatement insertStatement = prepare("INSERT INTO " + prefix + "EmpireFlags(name,  serializedValue, empireName) VALUES(?, ?, ?)", true);
                insertStatement.setString(1, flag.flagType.name);
                insertStatement.setString(2, flag.flagType.serializeValue(flag.value));
                insertStatement.setString(3, empire.getName());
                insertStatement.executeUpdate();

                empire.flagsContainer.add(flag);
            }
        } catch (SQLException e) {
            LOG.error("Failed to save Flag {}!", flag.flagType.name);
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }


    @SuppressWarnings("SuspiciousNameCombination")
    
    public boolean saveBlockWhitelist(BlockWhitelist bw, Empire empire) {
        try {
            if (!empire.blockWhitelistsContainer.contains(bw)) {
                PreparedStatement insertStatement = prepare("INSERT INTO " + prefix + "BlockWhitelists(dim, x, y, z, flagName, empireName) VALUES(?, ?, ?, ?, ?, ?)", true);
                insertStatement.setInt(1, bw.getDim());
                insertStatement.setInt(2, bw.getX());
                insertStatement.setInt(3, bw.getY());
                insertStatement.setInt(4, bw.getZ());
                insertStatement.setString(5, bw.getFlagType().toString());
                insertStatement.setString(6, empire.getName());

                insertStatement.executeUpdate();

                ResultSet keys = insertStatement.getGeneratedKeys();
                if (keys.next())
                    bw.setDbID(keys.getInt(1));

                empire.blockWhitelistsContainer.add(bw);
            }
            // NO update since ID can't change
        } catch (SQLException e) {
            LOG.error("Failed to save a Block Whitelist!");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    
    public boolean saveSelectedEmpire(Citizen res, Empire empire) {
        try {
            if (res.empiresContainer.isSelectedEmpireSaved) {
                PreparedStatement statement = prepare("UPDATE " + prefix + "SelectedEmpire SET empireName=? WHERE citizen=?", true);
                statement.setString(1, empire.getName());
                statement.setString(2, res.getUUID().toString());
                statement.executeUpdate();
            } else {
                PreparedStatement statement = prepare("INSERT INTO " + prefix + "SelectedEmpire(citizen, empireName) VALUES(?, ?)", true);
                statement.setString(1, res.getUUID().toString());
                statement.setString(2, empire.getName());
                statement.executeUpdate();
                res.empiresContainer.isSelectedEmpireSaved = true;
            }
            res.empiresContainer.setMainEmpire(empire);

        } catch (SQLException e) {
            LOG.error("Failed to save a empire selection!");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    
    public boolean saveEmpireInvite(Citizen res, Empire empire) {
        try {
            if (!res.empireInvitesContainer.contains(empire)) {
                PreparedStatement s = prepare("INSERT INTO " + prefix + "EmpireInvites(citizen, empireName) VALUES(?, ?)", true);
                s.setString(1, res.getUUID().toString());
                s.setString(2, empire.getName());
                s.executeUpdate();

                res.empireInvitesContainer.add(empire);
            } else {
                return false;
            }
        } catch (SQLException e) {
            LOG.error("Failed to save empire invite: {} for empire {}", res.getPlayerName(), empire.getName());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }
    
    public boolean saveEmpireBans(Citizen res, Empire empire) {
        try {
            if (!res.empireInvitesContainer.contains(empire)) {
                PreparedStatement s = prepare("INSERT INTO " + prefix + "EmpireBans(citizen, empireName) VALUES(?, ?)", true);
                s.setString(1, res.getUUID().toString());
                s.setString(2, empire.getName());
                s.executeUpdate();

                res.empireBansContainer.add(empire);
            } else {
                return false;
            }
        } catch (SQLException e) {
            LOG.error("Failed to save empire bans for citizen: {} for empire {}", res.getPlayerName(), empire.getName());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    
    public boolean saveWorld(int dim) {
        LOG.debug("Saving World {}", dim);
        try {
            PreparedStatement s = prepare("INSERT INTO " + prefix + "Worlds(dim) VALUES(?)", true);
            s.setInt(1, dim);
            s.executeUpdate();

            EmpiresUniverse.instance.addWorld(dim);
        } catch (SQLException e) {
            LOG.error("Failed to save world with dimension id {}", dim);
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    
    public boolean saveBlockOwner(Citizen res, int dim, int x, int y, int z) {
        try {
            PreparedStatement s = prepare("INSERT INTO " + prefix + "BlockOwners(citizen, dim, x, y, z) VALUES(?, ?, ?, ?, ?)", false);
            s.setString(1, res.getUUID().toString());
            s.setInt(2, dim);
            s.setInt(3, x);
            s.setInt(4, y);
            s.setInt(5, z);
            s.executeUpdate();
        } catch (SQLException e) {
            LOG.error("Failed to save block owner.");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    
    public boolean saveEmpireBank(Bank bank) {
        try {
            if(getUniverse().banks.contains(bank)) {
                PreparedStatement s = prepare("UPDATE " + prefix + "EmpireBanks SET amount=?, daysNotPaid=? WHERE empireName=?", false);
                s.setInt(1, bank.getAmount());
                s.setInt(2, bank.getDaysNotPaid());
                s.setString(3, bank.getEmpire().getName());
                s.executeUpdate();
            } else {
                bank.setAmount(Config.instance.defaultBankAmount.get());
                bank.setDaysNotPaid(0);

                PreparedStatement s = prepare("INSERT INTO " + prefix + "EmpireBanks VALUES(?, ?, ?)", false);
                s.setString(1, bank.getEmpire().getName());
                s.setInt(2, bank.getAmount());
                s.setInt(3, bank.getDaysNotPaid());
                s.executeUpdate();

                getUniverse().addBank(bank);
            }

        } catch (SQLException e) {
            LOG.error("Failed to save a empire's bank.");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    /* ----- Link ----- */

    
    public boolean linkCitizenToEmpire(Citizen res, Empire empire, Rank rank) {
        try {
            PreparedStatement s = prepare("INSERT INTO " + prefix + "CitizensToEmpires (citizen, empire, rank) VALUES(?, ?, ?)", true);
            s.setString(1, res.getUUID().toString());
            s.setString(2, empire.getName());
            // You need rank since this method is the one that adds the citizen to the empire and vice-versa
            s.setString(3, rank.getName());
            s.execute();
            
            res.empiresContainer.add(empire);
            empire.citizensMap.put(res, rank);
        } catch (SQLException e) {
            LOG.error("Failed to link Citizen {} ({}) with Empire {}", res.getPlayerName(), res.getUUID(), empire.getName());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    
    public boolean unlinkCitizenFromEmpire(Citizen res, Empire empire) {
        try {
            PreparedStatement s = prepare("DELETE FROM " + prefix + "CitizensToEmpires WHERE citizen = ? AND empire = ?", true);
            s.setString(1, res.getUUID().toString());
            s.setString(2, empire.getName());
            s.execute();

            res.empiresContainer.remove(empire);
            empire.citizensMap.remove(res);
        } catch (SQLException e) {
            LOG.error("Failed to unlink Citizen {} ({}) with Empire {}", e, res.getPlayerName(), res.getUUID(), empire.getName());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    
    public boolean updateCitizenToEmpireLink(Citizen res, Empire empire, Rank rank) {
        try {
            PreparedStatement s = prepare("UPDATE " + prefix + "CitizensToEmpires SET rank = ? WHERE citizen = ? AND empire = ?", true);
            s.setString(1, rank.getName());
            s.setString(2, res.getUUID().toString());
            s.setString(3, empire.getName());
            s.executeUpdate();

            empire.citizensMap.put(res, rank);
        } catch (SQLException e) {
            LOG.error("Failed to update link between Citizen {} ({}) with Empire {}", e, res.getPlayerName(), res.getUUID(), empire.getName());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    

    
    public boolean linkCitizenToPlot(Citizen res, Plot plot, boolean isOwner) {
        try {
            PreparedStatement s = prepare("INSERT INTO " + prefix + "CitizensToPlots(citizen, plotID, isOwner) VALUES(?, ?, ?)", true);
            s.setString(1, res.getUUID().toString());
            s.setInt(2, plot.getDbID());
            s.setBoolean(3, isOwner);
            s.executeUpdate();

            if (isOwner) {
                plot.ownersContainer.add(res);
            } else {
                plot.membersContainer.add(res);
            }

        } catch (SQLException e) {
            LOG.error("Failed to link {} to plot {} in empire {}", res.getPlayerName(), plot.getName(), plot.getEmpire().getName());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    
    public boolean unlinkCitizenFromPlot(Citizen res, Plot plot) {
        try {
            PreparedStatement s = prepare("DELETE FROM " + prefix + "CitizensToPlots WHERE citizen=? AND plotID=?", true);
            s.setString(1, res.getUUID().toString());
            s.setInt(2, plot.getDbID());
            s.executeUpdate();

            plot.ownersContainer.remove(res);
            plot.membersContainer.remove(res);

        } catch (SQLException e) {
            LOG.error("Failed to unlink {} to plot {} in empire {}", res.getPlayerName(), plot.getName(), plot.getEmpire().getName());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    
    public boolean updateCitizenToPlotLink(Citizen res, Plot plot, boolean isOwner) {
        try {
            PreparedStatement s = prepare("UPDATE " + prefix + "CitizensToPlots SET isOwner=? WHERE citizen=? AND plotID=?", true);
            s.setBoolean(1, isOwner);
            s.setString(2, res.getUUID().toString());
            s.setInt(3, plot.getDbID());
            s.executeUpdate();

        } catch (SQLException e) {
            LOG.error("Failed to update link {} to plot {} in empire {}", res.getPlayerName(), plot.getName(), plot.getEmpire().getName());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    /* ----- Delete ----- */

    
    public boolean deleteEmpire(Empire empire) {
        try {
            // Delete Empire from Datasource
            PreparedStatement deleteEmpireStatement = prepare("DELETE FROM " + prefix + "Empires WHERE name=?", true);
            deleteEmpireStatement.setString(1, empire.getName());
            deleteEmpireStatement.execute();

            // Release all chunkloading tickets
            empire.ticketMap.releaseTickets();

            // Remove all Blocks owned by the Empire
            for (EmpireBlock b : empire.empireBlocksContainer.values()) {
                EmpiresUniverse.instance.removeEmpireBlock(b);
            }
            // Remove all Plots owned by the Empire
            for (Plot p : empire.plotsContainer) {
                EmpiresUniverse.instance.removePlot(p);
            }
            // Remove all Ranks owned by this Empire
            for (Rank r : empire.ranksContainer) {
                EmpiresUniverse.instance.removeRank(r);
            }
            for (Citizen res : empire.citizensMap.keySet()) {
                if (res.empiresContainer.getMainEmpire() == empire)
                    deleteSelectedEmpire(res);
                res.empiresContainer.remove(empire);
            }
            
            for (int i=1; i< empire.Warps.size(); i++ ) {
            	Teleport warp = empire.Warps.get(i);
            	empire.delWarp(warp);
            }
            // Remove the Empire from the Map
            EmpiresUniverse.instance.removeEmpire(empire);
        } catch (SQLException e) {
            LOG.error("Failed to delete Empire {}", empire.getName());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }

        return true;
    }

    public boolean deleteBlock(EmpireBlock block) {
        try {
            // Delete Block from Datasource
            PreparedStatement deleteBlockStatement = prepare("DELETE FROM " + prefix + "Blocks WHERE dim=? AND x=? AND z=?", true);
            deleteBlockStatement.setInt(1, block.getDim());
            deleteBlockStatement.setInt(2, block.getX());
            deleteBlockStatement.setInt(3, block.getZ());
            deleteBlockStatement.execute();

            if (block.isChunkloaded()) {
                block.getEmpire().ticketMap.chunkUnload(block);
            }

            // Delete Block from Empire
            block.getEmpire().empireBlocksContainer.remove(block);

            // Delete Plots contained in the Block
            for (Plot p : ImmutableList.copyOf(block.plotsContainer)) {
                deletePlot(p);
            }
            // Remove Block from Map
            EmpiresUniverse.instance.removeEmpireBlock(block);
        } catch (SQLException e) {
            LOG.error("Failed to delete Block {}!", block.getKey());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    
    public boolean deleteRank(Rank rank) {
        try {
            // Delete Rank from Datasource
            PreparedStatement deleteRankStatement = prepare("DELETE FROM " + prefix + "Ranks WHERE name=? AND empireName=?", true);
            deleteRankStatement.setString(1, rank.getName());
            deleteRankStatement.setString(2, rank.getEmpire().getName());
            deleteRankStatement.execute();

            // Remove Rank from Map
            EmpiresUniverse.instance.removeRank(rank);
            rank.getEmpire().ranksContainer.remove(rank);
        } catch (SQLException e) {
            LOG.error("Failed to delete Rank {} in Empire {}", rank.getName(), rank.getEmpire().getName());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }

        return true;
    }

    
    public boolean deleteCitizen(Citizen citizen) {
        try {
            // Delete Citizen from Datasource
            PreparedStatement deleteCitizenStatement = prepare("DELETE FROM " + prefix + "Citizens WHERE uuid=?", true);
            deleteCitizenStatement.setString(1, citizen.getUUID().toString());
            deleteCitizenStatement.execute();

            // Remove Citizen from Map
            EmpiresUniverse.instance.removeCitizen(citizen);
        } catch (SQLException e) {
            LOG.error("Failed to delete Citizen {}!", citizen.getUUID());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    
    public boolean deletePlot(Plot plot) {
        try {
            // Delete Plot from Datasource
            PreparedStatement deletePlotStatement = prepare("DELETE FROM " + prefix + "Plots WHERE ID=?", true);
            deletePlotStatement.setInt(1, plot.getDbID());
            deletePlotStatement.execute();

            // Remove Plot from Map
            EmpiresUniverse.instance.removePlot(plot);
            plot.getEmpire().plotsContainer.remove(plot);
        } catch (SQLException e) {
            LOG.error("Failed to delete Plot {}!", plot.getKey());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }
    
    public boolean deleteWarp(Teleport Warp) {
        try {
            // Delete Plot from Datasource
            PreparedStatement deleteWarpStatement = prepare("DELETE FROM " + prefix + "Warps WHERE ID=?", true);
            deleteWarpStatement.setInt(1, Warp.getDbID());
            deleteWarpStatement.execute();

            // Remove Plot from Map
            EmpiresUniverse.instance.Warps.remove(Warp);
            Warp.getEmpire().Warps.remove(Warp);
        } catch (SQLException e) {
            LOG.error("Failed to delete Plot {}!", Warp.getKey());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }
 
    public boolean deleteBlockWhitelist(BlockWhitelist bw, Empire empire) {
        try {
            PreparedStatement deleteStatement = prepare("DELETE FROM " + prefix + "BlockWhitelists WHERE ID=?", false);
            deleteStatement.setInt(1, bw.getDbID());
            deleteStatement.executeUpdate();

            empire.blockWhitelistsContainer.remove(bw);
        } catch (SQLException e) {
            LOG.error("Failed to delete BlockWhitelist!");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    
    public boolean deleteSelectedEmpire(Citizen res) {
        try {
            PreparedStatement statement = prepare("DELETE FROM " + prefix + "SelectedEmpire WHERE citizen=?", true);
            statement.setString(1, res.getUUID().toString());
            statement.executeUpdate();
        } catch (Exception e) {
            LOG.error("Failed to delete a empire selection!");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        res.empiresContainer.isSelectedEmpireSaved = false;
        return true;
    }

    
    public boolean deleteEmpireInvite(Citizen res, Empire empire, boolean response) {
        try {
            PreparedStatement s = prepare("DELETE FROM " + prefix + "EmpireInvites WHERE citizen=? AND empireName=?", true);
            s.setString(1, res.getUUID().toString());
            s.setString(2, empire.getName());
            s.executeUpdate();
            if (response) {
                linkCitizenToEmpire(res, empire, empire.ranksContainer.getDefaultRank());
            }
            res.empireInvitesContainer.remove(empire);
        } catch (SQLException e) {
            LOG.error("Failed to delete empire invite for {} to empire {}", res.getPlayerName(), empire.getName());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }


    
    public boolean deleteFlag(Flag flag, Empire empire) {
        try {
            PreparedStatement deleteFlagStatement = prepare("DELETE FROM " + prefix + "EmpireFlags WHERE name=? AND empireName=?", true);
            deleteFlagStatement.setString(1, flag.flagType.toString());
            deleteFlagStatement.setString(2, empire.getName());
            deleteFlagStatement.execute();

            empire.flagsContainer.remove(flag.flagType);
        } catch (SQLException e) {
            LOG.error("Failed to delete flag {}!", flag.flagType.toString());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    
    public boolean deleteFlag(Flag flag, Plot plot) {
        try {
            PreparedStatement deleteFlagStatement = prepare("DELETE FROM " + prefix + "PlotFlags WHERE name=? AND plotID=?", true);
            deleteFlagStatement.setString(1, flag.flagType.toString());
            deleteFlagStatement.setInt(2, plot.getDbID());
            deleteFlagStatement.execute();

            plot.flagsContainer.remove(flag.flagType);
        } catch (SQLException e) {
            LOG.error("Failed to delete flag {}!", flag.flagType.toString());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    
    public boolean deleteWorld(int dim) {
        try {
            PreparedStatement s = prepare("DELETE FROM " + prefix + "Worlds WHERE dim=?", true);
            s.setInt(1, dim);
            s.executeUpdate();

            EmpiresUniverse.instance.removeWorld(dim);
        } catch (SQLException e) {
            LOG.error("Failed to delete world with dimension id {}", dim);
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    
    public boolean deleteRankPermission(Rank rank, String perm) {
        try {
            PreparedStatement s = prepare("DELETE FROM " + prefix + "RankPermissions WHERE node = ? AND rank = ? AND empireName = ?", true);
            s.setString(1, perm);
            s.setString(2, rank.getName());
            s.setString(3, rank.getEmpire().getName());
            s.execute();

            rank.permissionsContainer.remove(perm);
        } catch (SQLException e) {
            LOG.error("Failed to add permission ({}) to Rank ({}) in Empire ({})", perm, rank.getName(), rank.getEmpire().getName());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    
    public boolean deleteAllBlockOwners() {
        try {
            PreparedStatement s = prepare("DELETE FROM " + prefix + "BlockOwners", false);
            s.execute();
        } catch (SQLException e) {
            LOG.error("Failed to delete BlockOwners table!");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    /* ----- Checks ------ */

    @SuppressWarnings("unchecked")
    
    protected boolean checkFlags() {
        for (Empire empire : getUniverse().empires) {
            for (FlagType type : FlagType.values()) {
                if (type.isEmpirePerm && !empire.flagsContainer.contains(type)) {
                    saveFlag(new Flag(type, type.defaultValue), empire);
                    LOG.info("Flag {} in empire {} got created because of the settings.", type.name.toLowerCase(), empire.getName());
                }
            }
        }

        for (Plot plot : getUniverse().plots) {
            for (FlagType type : FlagType.values()) {
                if (type.isPlotPerm && !plot.flagsContainer.contains(type)) {
                    saveFlag(new Flag(type, type.defaultValue), plot);
                    LOG.info("Flag {} in plot {} of empire {} got created because of the settings.", type.name.toLowerCase(), plot.getName(), plot.getEmpire().getName());
                }
            }
        }

        return true;
    }

    
    protected boolean checkEmpires() {
        for(Empire empire : getUniverse().empires) {
            if(empire.ranksContainer.getDefaultRank() == null) {
                LOG.error("Empire {} does not have a default rank set.", empire.getName());
                Rank rank = empire.ranksContainer.get("Citizen");
                if(rank == null) {
                    rank = new Rank("Citizen", empire, Rank.Type.DEFAULT);
                    rank.permissionsContainer.addAll(Rank.defaultRanks.get(Rank.Type.DEFAULT).permissionsContainer);
                    LOG.info("Adding default rank for empire.");
                } else {
                    rank.setType(Rank.Type.DEFAULT);
                    LOG.info("Set 'Citizen' as current default rank.");
                }
                saveRank(rank);
            }

            if(empire.ranksContainer.getLeaderRank() == null) {
                LOG.error("Empire {} does not have a leader rank set.", empire.getName());
                Rank rank = empire.ranksContainer.get("Leader");
                if(rank == null) {
                    rank = new Rank("Leader", empire, Rank.Type.LEADER);
                    rank.permissionsContainer.addAll(Rank.defaultRanks.get(Rank.Type.LEADER).permissionsContainer);
                    LOG.info("Adding leader rank for empire.");
                } else {
                    rank.setType(Rank.Type.LEADER);
                    LOG.info("Set 'Leader' as current default rank.");
                }
                saveRank(rank);
            }

            if(!(empire instanceof AdminEmpire)) {
                if(!getUniverse().banks.contains(empire)) {
                    saveEmpireBank(empire.bank);
                    LOG.info("Added bank entry for {}", empire.getName());
                }
            }
        }
        return true;
    }

    /* ----- Reset ----- */

    public boolean resetRanks(Empire empire) {

        for(Rank defaultRank : Rank.defaultRanks) {
            Rank rank = empire.ranksContainer.get(defaultRank.getName());
            if(rank == null) {
                LOG.info("Adding rank {} to empire {}", defaultRank.getName(), empire.getName());
                rank = new Rank(defaultRank.getName(), empire, defaultRank.getType());
            } else  {
                rank.permissionsContainer.clear();
                if(rank.getType() != defaultRank.getType()) {
                    LOG.info("Changing type of rank {} to {}", rank.getName(), defaultRank.getType());
                    rank.setType(defaultRank.getType());
                }
            }
            rank.permissionsContainer.addAll(defaultRank.permissionsContainer);

            saveRank(rank);
        }

        for(int i = 0; i < empire.ranksContainer.size(); i++) {
            Rank rank = empire.ranksContainer.get(i);
            if(!Rank.defaultRanks.contains(rank.getName())) {
                LOG.info("Deleting rank {} from empire {}", rank.getName(), empire.getName());
                deleteRank(rank);
                i--;
            }
        }
        return true;
    }

    private EmpiresUniverse getUniverse() {
        return EmpiresUniverse.instance;
    }
}
