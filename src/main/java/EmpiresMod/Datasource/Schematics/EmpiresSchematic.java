
package EmpiresMod.Datasource.Schematics;


import EmpiresMod.Configuration.Config;
import EmpiresMod.Datasource.Bridge.BridgeSQL;
import EmpiresMod.entities.Empire.Rank;

public class EmpiresSchematic extends BaseSchematic {
    @Override
    public void initializeUpdates(BridgeSQL bridge) {
        updates.add(new DBUpdate("07.25.2014.1", "Add Updates Table", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "Updates (" +
                "id VARCHAR(20) NOT NULL," +
                "description VARCHAR(50) NOT NULL," +
                "PRIMARY KEY(id)" +
                ");"));
        updates.add(new DBUpdate("07.25.2014.2", "Add Citizens Table", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "Citizens (" +
                "uuid CHAR(36) NOT NULL," +
                "name VARCHAR(240) NOT NULL," +
                "joined BIGINT NOT NULL," +
                "lastOnline BIGINT NOT NULL," +
                "PRIMARY KEY(uuid)" +
                ");"));
        updates.add(new DBUpdate("10.05.2014", "Add Worlds", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "Worlds(" +
                "dim INT," +
                "PRIMARY KEY(dim))"));
        updates.add(new DBUpdate("06.14.2018.1", "Add Empires Table", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "Empires (" +
                "name VARCHAR(32) NOT NULL,"+    
                "desc VARCHAR(54) NOT NULL, "+
                "isAdminEmpire BOOLEAN, " +
                "spawnDim INT NOT NULL, " +
                "spawnX FLOAT NOT NULL, " +
                "spawnY FLOAT NOT NULL, " +
                "spawnZ FLOAT NOT NULL, " +
                "cameraYaw FLOAT NOT NULL, " +
                "cameraPitch FLOAT NOT NULL, " +
                "PRIMARY KEY(name)," +
                "FOREIGN KEY(spawnDim) REFERENCES " + bridge.prefix + " Worlds(dim) ON DELETE CASCADE" +
                ");"));
        updates.add(new DBUpdate("07.25.2014.4", "Add Ranks Table", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "Ranks (" +
                "name VARCHAR(50) NOT NULL," +  // TODO Allow larger rank names?
                "empireName VARCHAR(32) NOT NULL," +
                "isDefault BOOLEAN, " +
                "PRIMARY KEY(name, empireName)," +
                "FOREIGN KEY(empireName) REFERENCES " + bridge.prefix + "Empires(name) ON DELETE CASCADE ON UPDATE CASCADE" +
                ");"));
        updates.add(new DBUpdate("07.25.2014.5", "Add RankPermissions Table", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "RankPermissions (" +
                "node VARCHAR(100) NOT NULL," +
                "rank VARCHAR(50) NOT NULL," +
                "empireName VARCHAR(32) NOT NULL," +
                "PRIMARY KEY(node, rank, empireName)," +
                "FOREIGN KEY(rank, empireName) REFERENCES " + bridge.prefix + "Ranks(name, empireName) ON DELETE CASCADE ON UPDATE CASCADE" +
                ");"));
        updates.add(new DBUpdate("07.25.2014.6", "Add Blocks Table", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "Blocks (" +
                "dim INT NOT NULL," +
                "x INT NOT NULL," +
                "z INT NOT NULL," +
                "empireName VARCHAR(32) NOT NULL," +
                "PRIMARY KEY(dim, x, z)," +
                "FOREIGN KEY(empireName) REFERENCES " + bridge.prefix + "Empires(name) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "FOREIGN KEY(dim) REFERENCES " + bridge.prefix + "Worlds(dim) ON DELETE CASCADE" +
                ");"));
        updates.add(new DBUpdate("07.25.2014.7", "Add Plots Table", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "Plots (" +
                "ID INTEGER NOT NULL " + bridge.getAutoIncrement() + "," + // Just because it's a pain with this many primary keys
                "name VARCHAR(50) NOT NULL," + // TODO Allow larger Plot names?
                "dim INT NOT NULL," +
                "x1 INT NOT NULL," +
                "y1 INT NOT NULL," +
                "z1 INT NOT NULL," +
                "x2 INT NOT NULL," +
                "y2 INT NOT NULL," +
                "z2 INT NOT NULL," +
                "empireName VARCHAR(32) NOT NULL," +
                "PRIMARY KEY(ID)," +
                "FOREIGN KEY(empireName) REFERENCES " + bridge.prefix + "Empires(name) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "FOREIGN KEY(dim) REFERENCES " + bridge.prefix + "Worlds(dim) ON DELETE CASCADE" +
                ");"));
        updates.add(new DBUpdate("07.25.2014.8", "Add Nations Table", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "Nations (" +
                "name VARCHAR(32) NOT NULL," + // TODO Allow larger nation names?
                "PRIMARY KEY(name)" +
                ");"));

        // Create "Join" Tables
        updates.add(new DBUpdate("08.07.2014.1", "Add CitizensToEmpires Table", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "CitizensToEmpires (" +
                "citizen CHAR(36) NOT NULL," +
                "empire VARCHAR(32) NOT NULL," +
                "rank VARCHAR(50) NOT NULL," +
                "PRIMARY KEY(citizen, empire)," +
                "FOREIGN KEY(citizen) REFERENCES " + bridge.prefix + "Citizens(uuid) ON DELETE CASCADE ON UPDATE CASCADE," +
                "FOREIGN KEY(rank, empire) REFERENCES " + bridge.prefix + "Ranks(name, empireName) ON DELETE CASCADE ON UPDATE CASCADE" +
                ");"));
        updates.add(new DBUpdate("08.07.2014.2", "Add EmpiresToNations Table", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "EmpiresToNations (" +
                "empire VARCHAR(50)," +
                "nation VARCHAR(50)," +
                "rank CHAR(1) DEFAULT 'T'," +
                "PRIMARY KEY(empire, nation)," +
                "FOREIGN KEY(empire) REFERENCES " + bridge.prefix + "Empires(name) ON DELETE CASCADE ON UPDATE CASCADE," +
                "FOREIGN KEY(nation) REFERENCES " + bridge.prefix + "Nations(name) ON DELETE CASCADE ON UPDATE CASCADE" +
                ");"));
        updates.add(new DBUpdate("08.26.2014.1", "Add EmpireFlags Table", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "EmpireFlags (" +
                "name VARCHAR(50) NOT NULL," +
                "serializedValue VARCHAR(400), " +
                "empireName VARCHAR(50) NOT NULL," +
                "PRIMARY KEY(name, empireName)," +
                "FOREIGN KEY(empireName) REFERENCES " + bridge.prefix + "Empires(name) ON DELETE CASCADE ON UPDATE CASCADE" +
                ");"));
        updates.add(new DBUpdate("08.30.2014.1", "Add PlotFlags Table", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "PlotFlags (" +
                "name VARCHAR(50) NOT NULL," +
                "serializedValue VARCHAR(400), " +
                "plotID INT NOT NULL," +
                "PRIMARY KEY(name, plotID)," +
                "FOREIGN KEY(plotID) REFERENCES " + bridge.prefix + "Plots(ID) ON DELETE CASCADE ON UPDATE CASCADE" +
                ");"));
        updates.add(new DBUpdate("08.31.2014.1", "Add CitizensToPlots", "CREATE TABLE IF NOT EXISTS " + bridge.prefix +
                "CitizensToPlots(" +
                "citizen varchar(36) NOT NULL, " +
                "plotID INT NOT NULL, " +
                "isOwner boolean, " + // false if it's ONLY whitelisted, if neither then shouldn't be in this list
                "PRIMARY KEY(citizen, plotID), " +
                "FOREIGN KEY(citizen) REFERENCES " + bridge.prefix + "Citizens(UUID) ON DELETE CASCADE, " +
                "FOREIGN KEY(plotID) REFERENCES " + bridge.prefix + "Plots(ID) ON DELETE CASCADE ON UPDATE CASCADE)"));
        updates.add(new DBUpdate("09.04.2014.1", "Add BlockWhitelists", "CREATE TABLE IF NOT EXISTS " + bridge.prefix +
                "BlockWhitelists(" +
                "ID INTEGER NOT NULL " + bridge.getAutoIncrement() + ", " +
                "dim INT NOT NULL, " +
                "x INT NOT NULL, " +
                "y INT NOT NULL, " +
                "z INT NOT NULL, " +
                "empireName VARCHAR(50), " +
                "flagName VARCHAR(50) NOT NULL, " +
                "PRIMARY KEY(ID), " +
                "FOREIGN KEY(flagName, empireName) REFERENCES " + bridge.prefix + "EmpireFlags(name, empireName) ON UPDATE CASCADE ON DELETE CASCADE, " +
                "FOREIGN KEY(dim) REFERENCES " + bridge.prefix + "Worlds(dim) ON DELETE CASCADE)"));
        updates.add(new DBUpdate("09.11.2014.1", "Add SelectedEmpire", "CREATE TABLE IF NOT EXISTS " + bridge.prefix +
                "SelectedEmpire(" +
                "citizen CHAR(36), " +
                "empireName VARCHAR(50)," +
                "PRIMARY KEY(citizen), " +
                "FOREIGN KEY(citizen) REFERENCES " + bridge.prefix + "Citizens(UUID) ON DELETE CASCADE," +
                "FOREIGN KEY(empireName) REFERENCES " + bridge.prefix + "Empires(name) ON DELETE CASCADE ON UPDATE CASCADE)"));
        updates.add(new DBUpdate("09.19.2014.1", "Add Friends", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "Friends(" +
                "citizen1 CHAR(36)," +
                "citizen2 CHAR(36)," +
                "PRIMARY KEY(citizen1, citizen2)," +
                "FOREIGN KEY(citizen1) REFERENCES " + bridge.prefix + "Citizens(UUID) ON DELETE CASCADE," +
                "FOREIGN KEY(citizen2) REFERENCES " + bridge.prefix + "Citizens(UUID) ON DELETE CASCADE)"));
        updates.add(new DBUpdate("09.19.2014.2", "Add FriendRequests", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "FriendRequests(" +
                "citizen CHAR(36)," +
                "citizenTarget CHAR(36)," +
                "PRIMARY KEY(citizen, citizenTarget)," +
                "FOREIGN KEY(citizen) REFERENCES " + bridge.prefix + "Citizens(UUID) ON DELETE CASCADE," +
                "FOREIGN KEY(citizenTarget) REFERENCES " + bridge.prefix + "Citizens(UUID) ON DELETE CASCADE)"));
        updates.add(new DBUpdate("10.02.2014", "Add EmpireInvites", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "EmpireInvites(" +
                "citizen CHAR(36)," +
                "empireName VARCHAR(50), " +
                "PRIMARY KEY(citizen, empireName)," +
                "FOREIGN KEY(citizen) REFERENCES " + bridge.prefix + "Citizens(UUID) ON DELETE CASCADE, " +
                "FOREIGN KEY(empireName) REFERENCES " + bridge.prefix + "Empires(name) ON DELETE CASCADE ON UPDATE CASCADE)"));
        
        updates.add(new DBUpdate("08.11.2017", "Add EmpireBans", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "EmpireBans(" +
                "citizen CHAR(36)," +
                "empireName VARCHAR(50), " +
                "PRIMARY KEY(citizen, empireName)," +
                "FOREIGN KEY(citizen) REFERENCES " + bridge.prefix + "Citizens(UUID) ON DELETE CASCADE, " +
                "FOREIGN KEY(empireName) REFERENCES " + bridge.prefix + "Empires(name) ON DELETE CASCADE ON UPDATE CASCADE)"));

        // Table Modifications
        updates.add(new DBUpdate("06.28.2017.1", "Add 'power' to citizens", "ALTER TABLE " + bridge.prefix +
        		"Citizens ADD power DOUBLE DEFAULT 0.00;"));
        
        updates.add(new DBUpdate("06.28.2017.2", "Add 'currentPower' to empires", "ALTER TABLE " + bridge.prefix +
        		"Empires ADD currentPower DOUBLE DEFAULT 0.00"));
        
        updates.add(new DBUpdate("10.18.2014.1", "Add 'extraBlocks' to empires", "ALTER TABLE " + bridge.prefix +
                "Empires ADD extraBlocks INTEGER DEFAULT 0;"));

        updates.add(new DBUpdate("10.23.2014.1", "Add 'maxPlots' to empires", "ALTER TABLE " + bridge.prefix +
                "Empires ADD maxPlots INTEGER DEFAULT " + Config.instance.defaultMaxPlots.get() + ""));
        
        updates.add(new DBUpdate("10.23.2017", "Add 'lastPowerUpdateTime' to citizens", "ALTER TABLE " + bridge.prefix +
                "Citizens ADD lastPowerUpdateTime LONG DEFAULT 0"));

        updates.add(new DBUpdate("11.4.2014.1", "Add 'extraBlocks to citizens", "ALTER TABLE " + bridge.prefix +
                "Citizens ADD extraBlocks INTEGER DEFAULT 0"));
        
        updates.add(new DBUpdate("3.22.2014.1", "Add 'BlockOwners' table", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "BlockOwners(" +
                "citizen CHAR(36), " +
                "dim INT NOT NULL, " +
                "x INT NOT NULL," +
                "y INT NOT NULL," +
                "z INT NOT NULL, " +
                "FOREIGN KEY(citizen) REFERENCES " + bridge.prefix + "Citizens(UUID) ON DELETE CASCADE)"));
        updates.add(new DBUpdate("6.8.2018.1", "Add 'Warps' table", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "Warps(" +
                "ID INTEGER NOT NULL " + bridge.getAutoIncrement() + "," +
        		"empireName VARCHAR(50), "+
        		"name VARCHAR(32) NOT NULL, " +
        		"dim INT NOT NULL, " +
                "x FLOAT NOT NULL, " +
                "y FLOAT NOT NULL, " +
                "z FLOAT NOT NULL, " +
                "yaw FLOAT NOT NULL, " +
                "pitch FLOAT NOT NULL, " +
                "PRIMARY KEY(ID, empireName), " +
                "FOREIGN KEY(empireName) REFERENCES " + bridge.prefix + "Empires(name) ON DELETE CASCADE ON UPDATE CASCADE)"));
        updates.add(new DBUpdate("3.27.2014.1", "Add 'EmpireBanks' table", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "EmpireBanks(" +
                "empireName VARCHAR(50), " +
                "amount INT NOT NULL, " +
                "PRIMARY KEY(empireName), " +
                "FOREIGN KEY(empireName) REFERENCES " + bridge.prefix + "Empires(name) ON DELETE CASCADE ON UPDATE CASCADE)"));
        updates.add(new DBUpdate("3.27.2014.2", "Add 'PlotBanks' table", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "PlotBanks(" +
                "plotID INT NOT NULL, " +
                "amount INT NOT NULL, " +
                "PRIMARY KEY(plotID), " +
                "FOREIGN KEY(plotID) REFERENCES " + bridge.prefix + "Plots(ID) ON DELETE CASCADE ON UPDATE CASCADE)"));
        updates.add(new DBUpdate("4.1.2015.1", "Add 'daysNotPaid' to EmpireBanks", "ALTER TABLE " + bridge.prefix +
                "EmpireBanks ADD daysNotPaid INTEGER DEFAULT 0"));
        updates.add(new DBUpdate("4.12.2015.1", "Add 'isFarClaim' to Blocks", "ALTER TABLE " + bridge.prefix +
                "Blocks ADD isFarClaim boolean DEFAULT false"));
        updates.add(new DBUpdate("4.12.2015.2", "Add 'maxFarClaims' to Empires", "ALTER TABLE " + bridge.prefix +
                "Empires ADD maxFarClaims INTEGER DEFAULT " + Config.instance.maxFarClaims.get()));
        updates.add(new DBUpdate("4.12.2015.3", "Add 'pricePaid' to Blocks", "ALTER TABLE " + bridge.prefix +
                "Blocks ADD pricePaid INTEGER DEFAULT " + Config.instance.costAmountClaim.get()));

        updates.add(new DBUpdate("8.21.2015.1", "Add 'type' to Ranks", "ALTER TABLE " + bridge.prefix +
                "Ranks ADD type VARCHAR(50) DEFAULT '" + Rank.Type.OFFICER + "'"));
        
        updates.add(new DBUpdate("11.11.2015.1", "Add 'extraFarClaims' to Empires", "ALTER TABLE " + bridge.prefix +
                "Empires ADD extraFarClaims INTEGER DEFAULT 0"));
        updates.add(new DBUpdate("12.16.2015.1", "Add 'fakePlayer to citizens", "ALTER TABLE " + bridge.prefix +
                "Citizens ADD fakePlayer BOOLEAN DEFAULT false"));
        
        updates.add(new DBUpdate("8.12.2017, ", "Add 'isBanned to citizens", "ALTER TABLE " + bridge.prefix +
        		"Citizens ADD isBanned BOOLEAN DEFAULT false"));
        //updates.add(new DBUpdate("6.16.2018, ", "Add 'desc' to Empires", "ALTER TABLE " + bridge.prefix +
        //		"Empires ADD desc VARCHAR(54) DEFAULT '" + Config.instance.defaultDesc.get() + "'"));
    }

}
