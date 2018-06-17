package EmpiresMod.protection.Segment;

import java.util.ArrayList;
import java.util.List;

import EmpiresMod.entities.Empire.Citizen;
import EmpiresMod.entities.Flags.FlagType;
import EmpiresMod.entities.Misc.Volume;
import EmpiresMod.protection.ProtectionManager;
import EmpiresMod.protection.Segment.Enums.EntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;

/**
 * Segment that protects against an Entity
 */
public class SegmentEntity extends Segment {

	public final List<EntityType> types = new ArrayList<EntityType>();

	public boolean shouldExist(Entity entity) {
		if (!types.contains(EntityType.TRACKED)) {
			return true;
		}

		if (!shouldCheck(entity)) {
			return true;
		}
		

		Citizen owner = getOwner(entity);
		int range = getRange(entity);
		int dim = entity.dimension;
		int x = (int) Math.floor(entity.posX);
		int y = (int) Math.floor(entity.posY);
		int z = (int) Math.floor(entity.posZ);

		if (range == 0) {
			if (!hasPermissionAtLocation(owner, dim, x, y, z)) {
				return false;
			}
		} else {
			Volume rangeBox = new Volume(x - range, y - range, z - range, x + range, y + range, z + range);
			if (!hasPermissionAtLocation(owner, dim, rangeBox)) {
				return false;
			}
		}
		return true;
	}

	public boolean shouldImpact(Entity entity, Citizen owner, MovingObjectPosition mop) {
		if (!types.contains(EntityType.IMPACT)) {
			return true;
		}

		if (!shouldCheck(entity)) {
			return true;
		}

		int range = getRange(entity);
		int dim = entity.dimension;
		int x = (int) Math.floor(mop.hitVec.xCoord);
		int y = (int) Math.floor(mop.hitVec.yCoord);
		int z = (int) Math.floor(mop.hitVec.zCoord);

		if (range == 0) {
			if (!hasPermissionAtLocation(owner, dim, x, y, z)) {
				return false;
			}
		} else {
			Volume rangeBox = new Volume(x - range, y - range, z - range, x + range, y + range, z + range);
			if (!hasPermissionAtLocation(owner, dim, rangeBox)) {
				return false;
			}
		}
		return true;
	}

	public boolean shouldInteract(Entity entity, Citizen res) {
		if (!types.contains(EntityType.PROTECT)) {
			return true;
		}

		if (!shouldCheck(entity)) {
			return true;
		}

		Citizen owner = getOwner(entity);
		int dim = entity.dimension;
		int x = (int) Math.floor(entity.posX);
		int y = (int) Math.floor(entity.posY);
		int z = (int) Math.floor(entity.posZ);

		if (owner != null && res.getUUID().equals(owner.getUUID())) {
			return true;
		}

		if (!hasPermissionAtLocation(res, dim, x, y, z)) {
			return false;
		}

		return true;
	}

	public boolean shouldAttack(Entity entity, Citizen res) {
		if (!types.contains(EntityType.PVP)) {
			return true;
		}

		if (!shouldCheck(entity)) {
			return true;
		}

		Citizen owner = getOwner(entity);
		EntityPlayer attackedPlayer = res.getPlayer();
		int dim = attackedPlayer.dimension;
		int x = (int) Math.floor(attackedPlayer.posX);
		int y = (int) Math.floor(attackedPlayer.posY);
		int z = (int) Math.floor(attackedPlayer.posZ);

		if (owner != null && !ProtectionManager.getFlagValueAtLocation(FlagType.PVP, dim, x, y, z)) {
			return false;
		}

		return true;
	}
}