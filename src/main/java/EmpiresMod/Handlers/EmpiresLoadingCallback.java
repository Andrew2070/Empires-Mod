package EmpiresMod.Handlers;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.util.Constants;

public class EmpiresLoadingCallback implements ForgeChunkManager.LoadingCallback {

	public static final List<ForgeChunkManager.Ticket> tickets = new ArrayList<ForgeChunkManager.Ticket>();

	@Override
	public void ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world) {
		for (ForgeChunkManager.Ticket ticket : tickets) {
			NBTTagList list = ticket.getModData().getTagList("chunkCoords", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound chunkNBT = list.getCompoundTagAt(i);
				ForgeChunkManager.forceChunk(ticket,
						new ChunkCoordIntPair(chunkNBT.getInteger("x"), chunkNBT.getInteger("z")));
			}
		}
		EmpiresLoadingCallback.tickets.addAll(tickets);
	}
}