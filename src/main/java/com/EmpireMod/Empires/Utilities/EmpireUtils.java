package com.EmpireMod.Empires.Utilities;

import java.util.ArrayList;
import java.util.List;

import com.EmpireMod.Empires.Constants;
import com.EmpireMod.Empires.Empires;
import com.EmpireMod.Empires.Datasource.EmpiresDatasource;
import com.EmpireMod.Empires.Datasource.EmpiresUniverse;
import com.EmpireMod.Empires.entities.Empire.BlockWhitelist;
import com.EmpireMod.Empires.entities.Empire.Empire;
import com.EmpireMod.Empires.entities.Empire.EmpireBlock;
import com.EmpireMod.Empires.entities.Flags.FlagType;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

/**
 * Utils class for random useful things
 */
public class EmpireUtils {

	private EmpireUtils() {

	}

	/**
	 * Returns the empire at the specified position or null if nothing found.
	 */
	public static Empire getEmpireAtPosition(int dim, int x, int z) {
		EmpireBlock block = EmpiresUniverse.instance.blocks.get(dim, x, z);
		if (block == null)
			return null;
		return block.getEmpire();
	}

	/**
	 * Gets the empire at the entity's position
	 */
	protected static Empire getEmpireFromEntity(Entity entity) {
		return getEmpireAtPosition(entity.dimension, entity.chunkCoordX, entity.chunkCoordZ);
	}

	/**
	 * Gets the nearby tile entities of the specified tile entity and of the
	 * specified type
	 */
	public static List<TileEntity> getNearbyTileEntity(TileEntity te, Class<? extends TileEntity> type) {
		List<TileEntity> result = new ArrayList<TileEntity>();
		int[] dx = { 0, 1, 0, -1, 0, 0 };
		int[] dy = { 1, 0, -1, 0, 0, 0 };
		int[] dz = { 0, 0, 0, 0, 1, -1 };

		for (int i = 0; i < 6; i++) {
			TileEntity found = te.getWorldObj().getTileEntity(te.xCoord + dx[i], te.yCoord + dy[i], te.zCoord + dz[i]);
			if (found != null && type.isAssignableFrom(found.getClass())) {
				Empires.instance.LOG.info("Found tile entity {} for class {}", found, type.getName());
				result.add(found);
			}
		}
		return result;
	}

	/**
	 * Searches if the specified block is whitelisted in any empire
	 */
	public static boolean isBlockWhitelisted(int dim, int x, int y, int z, FlagType flagType) {
		Empire empire = getEmpireAtPosition(dim, x >> 4, z >> 4);
		if (empire == null)
			return false;
		BlockWhitelist bw = empire.blockWhitelistsContainer.get(dim, x, y, z, flagType);
		if (bw != null) {
			if (bw.isDeleted()) {
				getDatasource().deleteBlockWhitelist(bw, empire);
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * Gets all empires in a range
	 */
	public static List<Empire> getEmpiresInRange(int dim, int x, int z, int rangeX, int rangeZ) {
		List<Empire> list = new ArrayList<Empire>();
		for (int i = x - rangeX; i <= x + rangeX; i++) {
			for (int j = z - rangeZ; j <= z + rangeZ; j++) {
				Empire empire = getEmpireAtPosition(dim, i >> 4, j >> 4);
				if (empire != null)
					list.add(empire);
			}
		}
		return list;
	}

	/**
	 * Takes the selector tool (for plots) from the player.
	 */
	public static void takeSelectorToolFromPlayer(EntityPlayer player) {
		for (int i = 0; i < player.inventory.mainInventory.length; i++) {
			if (player.inventory.mainInventory[i] != null
					&& player.inventory.mainInventory[i].getDisplayName().equals(Constants.EDIT_TOOL_NAME)) {
				player.inventory.decrStackSize(i, 1);
				return;
			}
		}
	}

	public static EmpiresDatasource getDatasource() {
		return Empires.instance.datasource;
	}
}