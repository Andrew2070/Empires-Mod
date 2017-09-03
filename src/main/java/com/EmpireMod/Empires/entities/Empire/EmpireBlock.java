package com.EmpireMod.Empires.entities.Empire;

import java.util.HashMap;

import com.EmpireMod.Empires.API.Chat.IChatFormat;
import com.EmpireMod.Empires.Configuration.Config;
import com.EmpireMod.Empires.Handlers.VisualsHandler;
import com.EmpireMod.Empires.entities.Misc.Volume;
import com.EmpireMod.Empires.entities.Position.ChunkPos;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.ChunkCoordIntPair;

public class EmpireBlock implements IChatFormat {
	/**
	 * Used for storing in database
	 */
	public static final String KEY_FORMAT = "%s;%s;%s";

	private final int dim;
	private final int x;
	private final int z;
	private final Empire empire;
	private String key;

	private final boolean isFarClaim;
	private final int pricePaid;

	public final Plot.Container plotsContainer = new Plot.Container(Config.instance.defaultMaxPlots.get());

	public EmpireBlock(int dim, int x, int z, boolean isFarClaim, int pricePaid, Empire empire) {
		this.dim = dim;
		this.x = x;
		this.z = z;
		this.empire = empire;
		this.isFarClaim = isFarClaim;
		this.pricePaid = pricePaid;
		updateKey();
	}

	public int getDim() {
		return dim;
	}

	public int getX() {
		return x;
	}

	public int getZ() {
		return z;
	}

	public String getCoordString() {
		return String.format("%s, %s", x, z);
	}

	public Empire getEmpire() {
		return empire;
	}

	public String getKey() {
		return key;
	}

	private void updateKey() {
		key = String.format(KEY_FORMAT, dim, x, z);
	}

	public boolean isFarClaim() {
		return this.isFarClaim;
	}

	public int getPricePaid() {
		return this.pricePaid;
	}

	public boolean isChunkloaded() {
		return empire.ticketMap.get(dim).getChunkList().contains(this.toChunkCoords());
	}

	@Override
	public String toString() {
		return toChatMessage().getUnformattedText();
	}

	@Override
	public IChatComponent toChatMessage() {
		return toChunkPos().toChatMessage();
	}

	public Volume toVolume() {
		return new Volume(x << 4, 0, z << 4, (x << 4) + 15, 255, (z << 4) + 15);
	}

	public ChunkPos toChunkPos() {
		return new ChunkPos(this.dim, this.x, this.z);
	}

	public ChunkCoordIntPair toChunkCoords() {
		return new ChunkCoordIntPair(x, z);
	}

	/* ----- Helpers ----- */

	/**
	 * Checks if the point is inside this Block
	 */
	public boolean isPointIn(int dim, float x, float z) {
		return isChunkIn(dim, ((int) x) >> 4, ((int) z) >> 4);
	}

	/**
	 * Checks if the chunk is this Block
	 */
	public boolean isChunkIn(int dim, int cx, int cz) {
		return dim == this.dim && cx == x && cz == z;
	}

	public static class Container extends HashMap<String, EmpireBlock> implements IChatFormat {

		private int extraBlocks = 0;
		private int extraFarClaims = 0;

		public boolean add(EmpireBlock block) {
			super.put(block.getKey(), block);
			VisualsHandler.instance.updateEmpireBorders(this);
			return true;
		}

		public boolean remove(EmpireBlock block) {
			boolean result = super.remove(block.getKey()) != null;
			VisualsHandler.instance.updateEmpireBorders(this);
			return result;
		}

		public boolean contains(EmpireBlock block) {
			return this.contains(block.dim, block.x, block.z);
		}

		public boolean contains(int dim, int x, int z) {
			return super.containsKey(String.format(KEY_FORMAT, dim, x, z));
		}

		public EmpireBlock get(int dim, int x, int z) {
			return super.get(String.format(KEY_FORMAT, dim, x, z));
		}

		public int getExtraBlocks() {
			return extraBlocks;
		}

		public void setExtraBlocks(int extraBlocks) {
			this.extraBlocks = extraBlocks;
		}

		public int getExtraFarClaims() {
			return extraFarClaims;
		}

		public int getFarClaims() {
			int farClaims = 0;
			for (EmpireBlock block : this.values()) {
				if (block.isFarClaim()) {
					farClaims++;
				}
			}
			return farClaims;
		}

		public void setExtraFarClaims(int extraFarClaims) {
			this.extraFarClaims = extraFarClaims;
		}

		public void show(Citizen caller) {
			if (caller.getPlayer() instanceof EntityPlayerMP)
				VisualsHandler.instance.markEmpireBorders(this, (EntityPlayerMP) caller.getPlayer());
		}

		public void hide(Citizen caller) {
			if (caller.getPlayer() instanceof EntityPlayerMP)
				VisualsHandler.instance.unmarkBlocks((EntityPlayerMP) caller.getPlayer(), this);
		}

		@Override
		public IChatComponent toChatMessage() {
			IChatComponent root = new ChatComponentText("");

			for (EmpireBlock block : values()) {
				root.appendSibling(block.toChatMessage());
				root.appendSibling(new ChatComponentText(" "));
			}

			return root;
		}
	}
}