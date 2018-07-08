package EmpiresMod.entities.Managers;

import java.util.HashMap;
import java.util.Map;

import EmpiresMod.Transformers.SignClassTransformer;
import EmpiresMod.entities.Misc.Sign;
import EmpiresMod.entities.Misc.SignType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;

/**
 * If a sign is created, registering it here will route all the needed event
 * interactions to it.
 */
public class SignManager {

	public static final SignManager instance = new SignManager();

	public final Map<String, SignType> signTypes = new HashMap<String, SignType>(1);

	public Sign loadSign(World world, int x, int y, int z) {
		BlockPos pos = new BlockPos(x,y,z);
		TileEntity tileEntity = world.getTileEntity(pos);
		if (!(tileEntity instanceof TileEntitySign))
			return null;

		NBTTagCompound tagCompound = SignClassTransformer.getEmpiresDataValue(tileEntity); //error here
		if (tagCompound == null)
			return null;

		SignType signType = signTypes.get(tagCompound.getString("Type"));
		if (signType == null)
			return null;

		return signType.loadData((TileEntitySign) tileEntity, tagCompound.getTag("Value"));
	}
	

	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent ev) {
		if (!(ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)) {
			return;
		}

		Sign sign = loadSign(ev.world, ev.pos.getX(), ev.pos.getY(), ev.pos.getZ());
		if (sign == null)
			return;

		if (ev.entityPlayer.isSneaking()) {
			sign.onShiftRightClick(ev.entityPlayer);
		} else {
			sign.onRightClick(ev.entityPlayer);
		}
	}

	@SubscribeEvent
	public void onPlayerBreaksBlock(BlockEvent.BreakEvent ev) {
		Sign sign = loadSign(ev.world, ev.pos.getX(), ev.pos.getY(), ev.pos.getZ());

		if (sign != null) {
			sign.onShiftRightClick(ev.getPlayer());
			ev.setCanceled(true);
		}
	}
}