package EmpiresMod.API.container;

import java.util.HashMap;

import EmpiresMod.Empires;
import EmpiresMod.entities.Empire.Empire;
import EmpiresMod.entities.Empire.EmpireBlock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.util.Constants;

public class TicketMap extends HashMap<Integer, ForgeChunkManager.Ticket> {

	private final Empire empire;

	public TicketMap(Empire empire) {
		this.empire = empire;
	}

	@Override
	public ForgeChunkManager.Ticket get(Object key) {
		if (key instanceof Integer) {
			if (super.get(key) == null) {
				World world = DimensionManager.getWorld((Integer) key);
				if (world == null) {
					return null;
				}

				ForgeChunkManager.Ticket ticket = ForgeChunkManager.requestTicket(Empires.instance, world,
						ForgeChunkManager.Type.NORMAL);
				ticket.getModData().setString("empireName", empire.getName());
				ticket.getModData().setTag("chunkCoords", new NBTTagList());
				put((Integer) key, ticket);
				return ticket;
			} else {
				return super.get(key);
			}
		}
		return null;
	}

	public void chunkLoad(EmpireBlock block) {
		ForgeChunkManager.Ticket ticket = get(block.getDim());
		NBTTagList list = ticket.getModData().getTagList("chunkCoords", Constants.NBT.TAG_COMPOUND);
		list.appendTag(block.toChunkPos().toNBTTagCompound());

		ForgeChunkManager.forceChunk(ticket, block.toChunkCoords());
	}

	public void chunkUnload(EmpireBlock block) {
		ForgeChunkManager.Ticket ticket = get(block.getDim());
		ForgeChunkManager.unforceChunk(ticket, block.toChunkCoords());

		NBTTagList list = ticket.getModData().getTagList("chunkCoords", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound chunkNBT = list.getCompoundTagAt(i);
			int x = chunkNBT.getInteger("x");
			int z = chunkNBT.getInteger("z");

			if (x == block.getX() && z == block.getZ()) {
				list.removeTag(i);
				break;
			}
		}
	}

	public void releaseTickets() {
		for (ForgeChunkManager.Ticket ticket : values()) {
			ForgeChunkManager.releaseTicket(ticket);
		}
	}

	public int getChunkloadedAmount() {
		int size = 0;
		for (ForgeChunkManager.Ticket ticket : values()) {
			size += ticket.getChunkList().size();
		}
		return size;
	}

	public void chunkLoadAll() {
		for (EmpireBlock block : empire.empireBlocksContainer.values()) {
			if (!block.isChunkloaded()) {
				chunkLoad(block);
			}
		}
	}

	public void chunkUnloadAll() {
		for (EmpireBlock block : empire.empireBlocksContainer.values()) {
			if (block.isChunkloaded()) {
				chunkUnload(block);
			}
		}
	}
}