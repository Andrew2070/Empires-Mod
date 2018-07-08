package EmpiresMod.protection.Segment;

import java.util.ArrayList;
import java.util.List;

import EmpiresMod.entities.Empire.Citizen;
import EmpiresMod.entities.Position.BlockPosition;
import EmpiresMod.protection.Segment.Enums.BlockType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/**
 * Offers protection for blocks
 */
public class SegmentBlock extends Segment {
	protected int meta = -1;
	protected ClientBlockUpdate clientUpdate;
	protected List<BlockType> types = new ArrayList<BlockType>();

	public boolean shouldInteract(Citizen res, BlockPosition bp, PlayerInteractEvent.Action action) {
		if (meta != -1 && meta != MinecraftServer.getServer().worldServerForDimension(bp.getDim()).getBlockState(
				new BlockPos(bp.getX(), bp.getY(), bp.getZ())).getBlock().getMetaFromState( MinecraftServer.getServer().worldServerForDimension(bp.getDim()).getBlockState(
				new BlockPos(bp.getX(), bp.getY(), bp.getZ())).getBlock().getDefaultState())) {
			return true;
		}

		if ((action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK && !types.contains(BlockType.LEFT_CLICK)
				|| action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && !types.contains(BlockType.RIGHT_CLICK))
				&& !types.contains(BlockType.ANY_CLICK)) {
			return true;
		}

		if (!hasPermissionAtLocation(res, bp.getDim(), bp.getX(), bp.getY(), bp.getZ())) {
			if (clientUpdate != null) {
				clientUpdate.send(bp, (EntityPlayerMP) res.getPlayer());
			}
			return false;
		}

		return true;
	}

	public int getMeta() {
		return meta;
	}
}