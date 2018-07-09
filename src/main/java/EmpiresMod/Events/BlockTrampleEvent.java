package EmpiresMod.Events;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFarmland;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;

/**
 * Fired when an entity destroys tilled land and restores it to dirt.
 */
@Cancelable
public class BlockTrampleEvent extends BlockEvent {
	/**
	 * The entity that trampled the farm land
	 */
	public final Entity entity;

	public BlockTrampleEvent(Entity entity, int x, int y, int z, Block block, int blockMetadata) {
		super(entity.getEntityWorld(), new BlockPos(x, y, z), block.getDefaultState());
		this.entity = entity;
	}

	@SuppressWarnings("unused")
	public static boolean fireEvent(Entity entity, BlockFarmland block, int x, int y, int z) {
		return MinecraftForge.EVENT_BUS
				.post(new BlockTrampleEvent(entity, x, y, z, block, entity.worldObj.getBlockState(new BlockPos(x,y,z)).getBlock().getMetaFromState(entity.worldObj.getBlockState(new BlockPos(x,y,z)))));
	}
}
