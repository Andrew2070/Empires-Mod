package EmpiresMod.Misc.Teleport;

import EmpiresMod.API.Chat.Component.ChatComponentFormatted;
import EmpiresMod.API.Commands.Command.CommandsEMP;
import EmpiresMod.Datasource.EmpiresUniverse;
import EmpiresMod.Localization.LocalizationManager;
import EmpiresMod.entities.Empire.Empire;
import EmpiresMod.entities.Empire.Plot;
import EmpiresMod.entities.Empire.Relationship;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IChatComponent;

/**
 * Retains the needed information for the action of teleporting a player to a
 * certain position.
 */
public class Teleport {
	private int dbID;
	private String key;
	private int dim;
	private Empire empire;
	private String name;
	private float x, y, z, yaw, pitch;

	public Teleport(String name, Empire empire, int dim, float x, float y, float z, float yaw, float pitch) {
		setName(name);
		setEmpire(empire);
		setDim(dim);
		setPosition(x, y, z);
		setRotation(yaw, pitch);
		updateKey();
	}

	public Teleport(String name, Empire empire, int dim, float x, float y, float z) {
		this(name, empire, dim, x, y, z, 0, 0);
	}

	// Used when a player is riding an entity. eg pig, horse
	public void teleport(EntityPlayer pl, boolean canRide) {
		if (pl.dimension != dim) {
			MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension((EntityPlayerMP) pl, dim,
					new EssentialsTeleporter(MinecraftServer.getServer().worldServerForDimension(dim)));
		}
		if (pl.isRiding() && pl.ridingEntity != null && pl.ridingEntity.isEntityAlive() && canRide) {
			pl.ridingEntity.setPosition(x, y, z);
			pl.ridingEntity.setPositionAndRotation(x, y, z, yaw, pitch);
		}
		pl.setPositionAndUpdate(x, y, z);
		pl.setPositionAndRotation(x, y, z, yaw, pitch);
	}

	public void teleport(EntityPlayer pl) {
		teleport(pl, false);
	}
	
	public Teleport setDim(int dim) {
		this.dim = dim;
		return this;
	}
	
	public Teleport setEmpire(Empire empire) {
		this.empire = empire;
		return this;
	}

	public Empire getEmpire() {
		return empire;
	}
	public Teleport setPosition(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	public Teleport setRotation(float yaw, float pitch) {
		this.yaw = yaw;
		this.pitch = pitch;
		return this;
	}
	public Teleport setName(String name) {
		this.name = name;
		return this;
	}
	
	public String getName() {
		return name;
	}
	
	public int getDim() {
		return dim;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getZ() {
		return z;
	}

	public float getYaw() {
		return yaw;
	}

	public float getPitch() {
		return pitch;
	}
	private void updateKey() {
		key = String.format("%s;%s;%s;%s", dim, x, y, z);
	}
	
	public String getKey() {
		return key;
	}
	public Teleport get(int warpID) {
		for (Teleport warp : empire.Warps) {
			if (warp.getDbID() == warpID) {
				return warp;
			}
		}
		return null;
	}
	public void setDbID(int id) {
		this.dbID = id;
	}

	public int getDbID() {
		return this.dbID;
	}
}