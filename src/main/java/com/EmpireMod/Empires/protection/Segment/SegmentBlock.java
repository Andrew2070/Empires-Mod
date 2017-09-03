package com.EmpireMod.Empires.protection.Segment;

import java.util.ArrayList;
import java.util.List;

import com.EmpireMod.Empires.entities.Empire.Citizen;
import com.EmpireMod.Empires.entities.Position.BlockPos;
import com.EmpireMod.Empires.protection.Segment.Enums.BlockType;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/**
 * Offers protection for blocks
 */
public class SegmentBlock extends Segment {
	protected int meta = -1;
	protected ClientBlockUpdate clientUpdate;
	protected List<BlockType> types = new ArrayList<BlockType>();

	public boolean shouldInteract(Citizen res, BlockPos bp, PlayerInteractEvent.Action action) {
		if (meta != -1 && meta != MinecraftServer.getServer().worldServerForDimension(bp.getDim())
				.getBlockMetadata(bp.getX(), bp.getY(), bp.getZ())) {
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