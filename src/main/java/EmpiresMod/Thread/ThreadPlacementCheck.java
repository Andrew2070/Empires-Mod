package EmpiresMod.Thread;

import EmpiresMod.entities.Empire.Citizen;
import net.minecraft.util.BlockPos;
import EmpiresMod.entities.Position.BlockPosition;
import EmpiresMod.protection.ProtectionManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * A thread which checks if there's TileEntity on given block. If there isn't
 * found one in 1 second it exits.
 */
public class ThreadPlacementCheck extends Thread {
	private static final int TIMEOUT_IN_MS = 1000;

	private final Citizen res;
	private final BlockPosition position;

	public ThreadPlacementCheck(Citizen res, int x, int y, int z, int dim) {
		super();
		this.res = res;
		this.position = new BlockPosition(x, y, z, dim);
	}

	@Override
	public void run() {
		long startTime = System.currentTimeMillis();
		World world = MinecraftServer.getServer().worldServerForDimension(position.getDim());
		TileEntity te = null;
		while (te == null) {
			if (System.currentTimeMillis() - startTime >= TIMEOUT_IN_MS) {
				ProtectionManager.placementThreadTimeout();
				return;
			}

			te = world.getTileEntity(new BlockPos(position.getX(), position.getY(), position.getZ()));
		}
		ProtectionManager.addTileEntity(te, res);
	}
}