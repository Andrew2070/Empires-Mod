package EmpiresMod.protection;

import java.util.HashMap;
import java.util.Map;

import EmpiresMod.Empires;
import EmpiresMod.Configuration.Config;
import EmpiresMod.Datasource.EmpiresUniverse;
import EmpiresMod.Events.AE2PartPlaceEvent;
import EmpiresMod.Events.BlockTrampleEvent;
import EmpiresMod.Events.ModifyBiomeEvent;
import EmpiresMod.Events.ModifyBlockEvent;
import EmpiresMod.Events.ProjectileImpactEvent;
import EmpiresMod.Thread.ThreadPlacementCheck;
import EmpiresMod.Utilities.EmpireUtils;
import EmpiresMod.Utilities.PvPUtils;
import EmpiresMod.entities.Empire.BlockWhitelist;
import EmpiresMod.entities.Empire.Citizen;
import EmpiresMod.entities.Empire.Empire;
import EmpiresMod.entities.Flags.FlagType;
import EmpiresMod.entities.Misc.Volume;
import EmpiresMod.entities.Position.BlockPosition;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;




import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.world.BlockEvent;

/**
 * Handles all the protections
 */
public class ProtectionHandlers {

	public static final ProtectionHandlers instance = new ProtectionHandlers();

	public Map<TileEntity, Citizen> ownedTileEntities = new HashMap<TileEntity, Citizen>();

	public int activePlacementThreads = 0;
	public int maximalRange = 0;

	// ---- All the counters/tickers for preventing check every tick ----
	private int tickerTilesChecks = 20;
	private int tickerTilesChecksStart = 20;
	private int itemPickupCounter = 0;

	public Citizen getOwnerForTileEntity(TileEntity te) {
		return this.ownedTileEntities.get(te);
	}

	// ---- Main ticking method ----

	@SubscribeEvent
	public void serverTick(TickEvent.ServerTickEvent ev) {
		// TODO: Add a command to clean up the block whitelist table
		// periodically
		if (MinecraftServer.getServer().getTickCounter() % 600 == 0) {
			for (Empire empire : EmpiresUniverse.instance.empires)
				// Changed to EmpiresUniverse.instance from Empires.instance in
				// above lines^
				for (int i = 0; i < empire.blockWhitelistsContainer.size(); i++) {
					BlockWhitelist bw = empire.blockWhitelistsContainer.get(i);
					if (!ProtectionManager.isBlockWhitelistValid(bw)) {
						Empires.instance.datasource.deleteBlockWhitelist(bw, empire);
					}
				}
		}
	}

	@SuppressWarnings("unchecked")
	@SubscribeEvent
	public void worldTick(TickEvent.WorldTickEvent ev) {
		if (ev.side == Side.CLIENT)
			return;
		if (ev.phase == TickEvent.Phase.END) {
			return;
		}

		// Empires.instance.LOG.info("Tick number: " +
		// MinecraftServer.getServer().getTickCounter());

		// Entity check
		// TODO: Rethink this system a couple million times before you come up
		// with the best algorithm :P
		int loadedEntityListSize = ev.world.loadedEntityList.size();
		for (int i = 0; i < loadedEntityListSize; i++) {
			if (i >= ev.world.loadedEntityList.size())
				continue;
			Object item = ev.world.loadedEntityList.get(i);
			if (item == null)
				continue;
			Entity entity = (Entity) item;
			Empire empire = EmpireUtils.getEmpireAtPosition(entity.dimension, (int) Math.floor(entity.posX) >> 4,
					(int) Math.floor(entity.posZ) >> 4);
			// Empires.instance.log.info("Checking player...");
			// Player check, every tick
			if (entity instanceof EntityPlayerMP && !(entity instanceof FakePlayer)) {
				ProtectionManager.check((EntityPlayerMP) entity);
			} else {
				// Other entity checks
				if (MinecraftServer.getServer().getTickCounter() % 20 == 5) {
					ProtectionManager.checkExist(entity, false);
				}
			}
		}

		// TileEntity check
		if (MinecraftServer.getServer().getTickCounter() % 20 == 15) {
			if (activePlacementThreads == 0) {
				int loadedTileEntityListSize = ev.world.loadedTileEntityList.size();
				for (int i = 0; i < loadedTileEntityListSize; i++) {
					if (i >= ev.world.loadedTileEntityList.size())
						continue;
					Object item = ev.world.loadedTileEntityList.get(i);
					if (item == null)
						continue;
					TileEntity te = (TileEntity) item;
					ProtectionManager.check(te);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerAttackEntityEvent(AttackEntityEvent ev) {
		if (ev.entity.worldObj.isRemote || ev.isCanceled()) {
			return;
		}

		Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(ev.entityPlayer);
		// Changed to EmpiresUniverse.instance from Empires.instance in above
		// lines^
		ProtectionManager.checkInteraction(ev.target, res, ev);
	}
	
	@SubscribeEvent
	public void onBlockPlacement(BlockEvent.PlaceEvent ev) {
		onAnyBlockPlacement(ev.player, ev);
		
	}

	@SubscribeEvent
	public void onMultiBlockPlacement(BlockEvent.MultiPlaceEvent ev) {
		onAnyBlockPlacement(ev.player, ev);
	}

	public void onAnyBlockPlacement(EntityPlayer player, BlockEvent.PlaceEvent ev) {
		if (ev.world.isRemote || ev.isCanceled()) {
			return;
		}

		if (player instanceof FakePlayer) {
			if (!ProtectionManager.getFlagValueAtLocation(FlagType.FAKERS, ev.world.provider.getDimensionId(), ev.pos.getX(), ev.pos.getY(),
					ev.pos.getZ())) {
				ev.setCanceled(true);
			}
		} else {
			Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(player);
			// Changed to EmpiresUniverse.instance from Empires.instance in
			// above lines^
			if (!EmpiresUniverse.instance.blocks.contains(ev.world.provider.getDimensionId(), ev.pos.getX() >> 4, ev.pos.getZ() >> 4)) {
				// Changed to EmpiresUniverse.instance from Empires.instance in
				// above lines^
				int range = Config.instance.placeProtectionRange.get();
				Volume placeBox = new Volume(ev.pos.getX() - range, ev.pos.getY() - range, ev.pos.getZ() - range, ev.pos.getX() + range, ev.pos.getY() + range,
						ev.pos.getZ() + range);

				if (!ProtectionManager.hasPermission(res, FlagType.MODIFY, ev.world.provider.getDimensionId(), placeBox)) {
					ev.setCanceled(true);
					return;
				}
			} else {
				if (!ProtectionManager.hasPermission(res, FlagType.MODIFY, ev.world.provider.getDimensionId(), ev.pos.getX(), ev.pos.getY(),
						ev.pos.getZ())) {
					ev.setCanceled(true);
					return;
				}
			}

			if (ev.placedBlock instanceof ITileEntityProvider && ev.itemInHand != null) {
				TileEntity te = ((ITileEntityProvider) ev.placedBlock).createNewTileEntity(
						MinecraftServer.getServer().worldServerForDimension(ev.world.provider.getDimensionId()),
						ev.itemInHand.getItemDamage());
				if (te != null && ProtectionManager.isOwnable(te.getClass())) {
					ThreadPlacementCheck thread = new ThreadPlacementCheck(res, ev.pos.getX(), ev.pos.getY(), ev.pos.getZ(),
							ev.world.provider.getDimensionId());
					activePlacementThreads++;
					thread.start();
				}
			}
		}
	}

	@SubscribeEvent
	public void onEntityInteract(EntityInteractEvent ev) {
		if (ev.entity.worldObj.isRemote || ev.isCanceled()) {
			return;
		}
		int x = (int) Math.floor(ev.target.posX);
		int y = (int) Math.floor(ev.target.posY);
		int z = (int) Math.floor(ev.target.posZ);

		Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(ev.entityPlayer);
		// Changed to EmpiresUniverse.instance from Empires.instance in above
		// lines^
		ProtectionManager.checkInteraction(ev.target, res, ev);
		if (!ev.isCanceled() && ev.entityPlayer.getHeldItem() != null) {
			BlockPosition bp = new BlockPosition(x, y, z, ev.target.dimension);
			ProtectionManager.checkUsage(ev.entityPlayer.getHeldItem(), res, PlayerInteractEvent.Action.RIGHT_CLICK_AIR,
					bp, -1, ev);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onProjectileImpact(ProjectileImpactEvent ev) {
		if (ev.entity.worldObj.isRemote || ev.isCanceled()) {
			return;
		}

		EntityLivingBase firingEntity = ev.firingEntity;
		Citizen owner = null;
		if (firingEntity instanceof EntityPlayerMP && !(firingEntity instanceof FakePlayer)) {
			owner = EmpiresUniverse.instance.getOrMakeCitizen(firingEntity);
		} // Changed to EmpiresUniverse.instance from Empires.instance in above
			// lines^
		ProtectionManager.checkImpact(ev.entity, owner, ev.movingObjectPosition, ev);
	}

	@SuppressWarnings("unchecked")
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent ev) {
		if (ev.entityPlayer.worldObj.isRemote || ev.isCanceled()) {
			return;
		}

		Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(ev.entityPlayer);
		// Changed to EmpiresUniverse.instance from Empires.instance in above
		// lines^
		if (ev.entityPlayer.getHeldItem() != null) {
			ProtectionManager.checkUsage(ev.entityPlayer.getHeldItem(), res, ev.action, createBlockPos(ev), ev.face,
					ev);
		}
		if (!ev.isCanceled()) {
			ProtectionManager.checkBlockInteraction(res, new BlockPosition(ev.pos.getX(), ev.pos.getY(), ev.pos.getZ(), ev.world.provider.getDimensionId()),
					ev.action, ev);
		}

		// Some things (Autonomous Activator) only care about these. So always
		// deny them if the event is canceled.
		if (ev.isCanceled()) {
			ev.useBlock = Event.Result.DENY;
			ev.useItem = Event.Result.DENY;
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onAE2PartPlace(AE2PartPlaceEvent ev) {
		if (ev.world.isRemote || ev.isCanceled()) {
			return;
		}

		Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(ev.player);
		if (ev.player.getHeldItem() != null) {
			ProtectionManager.checkUsage(ev.player.getHeldItem(), res, Action.RIGHT_CLICK_BLOCK,
					new BlockPosition(ev.x, ev.y, ev.z, ev.world.provider.getDimensionId()), ev.face, ev);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onModifyBlock(ModifyBlockEvent ev) {
		if (ev.world.isRemote || ev.isCanceled() || Config.instance.fireSpreadInEmpires.get()) {
			return;
		}

		if (!ProtectionManager.getFlagValueAtLocation(FlagType.MODIFY, ev.world.provider.getDimensionId(), ev.x, ev.y,
				ev.z)) {
			ev.setCanceled(true);
			return;
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onBlockTrample(BlockTrampleEvent ev) {
		if (ev.world.isRemote || ev.isCanceled())

			return;

		Entity entity = ev.entity;
		Citizen res = null;

		if (!(entity instanceof EntityPlayer)) {
			// Protect from players ridding any entity
			if (entity.riddenByEntity != null && (entity.riddenByEntity instanceof EntityPlayer))
				entity = entity.riddenByEntity;
			// Protect from players jumping and leaving the horse in mid-air
			else
				res = ProtectionManager.getOwner(entity);
		}

		// Fake players are special
		if (entity instanceof FakePlayer) {
			if (!ProtectionManager.getFlagValueAtLocation(FlagType.FAKERS, ev.world.provider.getDimensionId(), ev.pos.getX(), ev.pos.getY(),
					ev.pos.getZ())) {
				ev.setCanceled(true);
			}
		} else {
			// Will be null if we didn't find the player responsible for this
			// trampling
			if (res == null) {
				res = EmpiresUniverse.instance.getOrMakeCitizen(entity);
				// Changed to EmpiresUniverse.instance from Empires.instance in
				// above lines^

				if (res == null)
					return;
			}

			// Trampling crops will break them and will modify the terrain
			if (!ProtectionManager.checkBlockBreak(ev.state.getBlock())) {
				if (!ProtectionManager.hasPermission(res, FlagType.MODIFY, ev.world.provider.getDimensionId(), ev.pos.getX(), ev.pos.getY(),
						ev.pos.getZ())) {
					ev.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onModifyBiome(ModifyBiomeEvent ev) {
		if (ev.world.isRemote || ev.isCanceled() || Config.instance.taintSpreadInEmpires.get()) {
			return;
		}

		if (EmpiresUniverse.instance.blocks.contains(ev.world.provider.getDimensionId(), ev.x >> 4, ev.z >> 4)) {
			ev.setCanceled(true);
		} // Changed to EmpiresUniverse.instance from Empires.instance in above
			// lines^
	}

	@SuppressWarnings("unchecked")
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerBreaksBlock(BlockEvent.BreakEvent ev) {
		if (ev.world.isRemote || ev.isCanceled()) {
			return;
		}

		if (ev.getPlayer() instanceof FakePlayer) {
			if (!ProtectionManager.getFlagValueAtLocation(FlagType.FAKERS, ev.world.provider.getDimensionId(), ev.pos.getX(), ev.pos.getY(),
					ev.pos.getZ())) {
				ev.setCanceled(true);
			}
		} else {
			Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(ev.getPlayer());
			// Changed to EmpiresUniverse.instance from Empires.instance in
			// above lines^
			if (!EmpiresUniverse.instance.blocks.contains(ev.world.provider.getDimensionId(), ev.pos.getX() >> 4, ev.pos.getZ() >> 4)) {
				int range = Config.instance.placeProtectionRange.get();
				Volume breakBox = new Volume(ev.pos.getX() - range, ev.pos.getY() - range, ev.z - range, ev.pos.getX() + range, ev.pos.getY() + range,
						ev.pos.getZ() + range);
				// Changed to EmpiresUniverse.instance from Empires.instance in
				// above lines^
				if (!ProtectionManager.checkBlockBreak(ev.state.getBlock())) {
					if (!ProtectionManager.hasPermission(res, FlagType.MODIFY, ev.world.provider.getDimensionId(),
							breakBox)) {
						ev.setCanceled(true);
						return;
					}
				}

			} else {
				if (!ProtectionManager.checkBlockBreak(ev.state.getBlock())) {
					if (!ProtectionManager.hasPermission(res, FlagType.MODIFY, ev.world.provider.getDimensionId(), ev.pos.getX(),
							ev.pos.getY(), ev.pos.getZ())) {
						ev.setCanceled(true);
						return;
					}
				}
			}

			if (ev.getPlayer().getHeldItem() != null) {
				ProtectionManager.checkBreakWithItem(ev.getPlayer().getHeldItem(), res,
						new BlockPosition(ev.pos.getX(), ev.pos.getY(), ev.pos.getZ(), ev.world.provider.getDimensionId()), ev);
			}
		}

		if (!ev.isCanceled() && ev.state.getBlock() instanceof ITileEntityProvider) {
			TileEntity te = ((ITileEntityProvider) ev.state.getBlock()).createNewTileEntity(ev.world, ev.state.getBlock());
			if (te != null && ProtectionManager.isOwnable(te.getClass())) {
				te = ev.world.getTileEntity(ev.pos.getX(), ev.pos.getY(), ev.pos.getZ());
				ownedTileEntities.remove(te);
				Empires.instance.LOG.info("Removed te {}", te.toString());
			}
		}
	}
	// Changed to EmpiresUniverse.instance from Empires.instance in above lines^

	@SuppressWarnings("unchecked")
	@SubscribeEvent
	public void onItemPickup(EntityItemPickupEvent ev) {
		if (ev.entity.worldObj.isRemote || ev.isCanceled()) {
			return;
		}

		Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(ev.entityPlayer);
		if (!ProtectionManager.hasPermission(res, FlagType.PICKUP, ev.item.dimension, (int) Math.floor(ev.item.posX),
				(int) Math.floor(ev.item.posY), (int) Math.floor(ev.item.posZ))) {
			ev.setCanceled(true);
		}
	}
	// Changed to EmpiresUniverse.instance from Empires.instance in above lines^

	@SubscribeEvent
	public void onLivingAttack(LivingAttackEvent ev) {
		if (ev.entity.worldObj.isRemote || ev.isCanceled()) {
			return;
		}

		if (ev.source.getEntity() != null) {
			if (ev.entity instanceof EntityPlayer) {
				if (ev.source.getEntity() instanceof EntityPlayer) {
					// Player vs Player
					int x = (int) Math.floor(ev.entityLiving.posX);
					int y = (int) Math.floor(ev.entityLiving.posY);
					int z = (int) Math.floor(ev.entityLiving.posZ);
					PvPUtils.deathProcess(ev.source.getEntity(), ev.entity, ev.ammount);
					if (!ProtectionManager.getFlagValueAtLocation(FlagType.PVP, ev.entityLiving.dimension, x, y, z)) {
						ev.setCanceled(true);
					}
				} else {
					// Entity vs Player (Check for Player owned Entity)
					Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(ev.entity);
					ProtectionManager.checkPVP(ev.source.getEntity(), res, ev);
					PvPUtils.deathProcess(null, ev.entity, ev.ammount);
				}

				// Changed to EmpiresUniverse.instance from Empires.instance in
				// above lines^
			} else {
				if (ev.source.getEntity() instanceof EntityPlayer) {
					// Player vs Living Entity
					Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(ev.source.getEntity());
					ProtectionManager.checkInteraction(ev.entity, res, ev);
					PvPUtils.deathProcess(null, ev.entity, ev.ammount);
				} else {
					// Entity vs Living Entity
				}
			}
		} else {
			// Non-Entity Damage
			PvPUtils.deathProcess(null, ev.entity, ev.ammount);
		}
	}

	// Changed to EmpiresUniverse.instance from Empires.instance in above lines^
	@SubscribeEvent
	public void onBucketFill(FillBucketEvent ev) {
		if (ev.entity.worldObj.isRemote || ev.isCanceled()) {
			return;
		}

		int x = (int) Math.floor(ev.target.getBlockPos().getX());
		int y = (int) Math.floor(ev.target.getBlockPos().getY());
		int z = (int) Math.floor(ev.target.getBlockPos().getZ());

		if (ev.entityPlayer instanceof FakePlayer) {
			if (!ProtectionManager.getFlagValueAtLocation(FlagType.FAKERS, ev.world.provider.getDimensionId(), x, y, z)) {
				ev.setCanceled(true);
			}
		} else {
			Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(ev.entityPlayer);
			if (!ProtectionManager.hasPermission(res, FlagType.USAGE, ev.world.provider.getDimensionId(), x, y, z)) {
				ev.setCanceled(true);
			}
		}
	}

	// Changed to EmpiresUniverse.instance from Empires.instance in above lines^
	@SubscribeEvent
	public void entityJoinWorld(EntityJoinWorldEvent ev) {
		if (Empires.instance.datasource == null) {
			return;
		}

		if (!(ev.entity instanceof EntityLiving)) {
			return;
		}

		ProtectionManager.checkExist(ev.entity, true);
	}

	@SubscribeEvent
	public void specialSpawn(LivingSpawnEvent.SpecialSpawn ev) {
		if (ev.isCanceled())
			return;

		ProtectionManager.checkExist(ev.entity, true);
	}

	@SubscribeEvent
	public void checkSpawn(LivingSpawnEvent.CheckSpawn ev) {
		if (ev.getResult() == Event.Result.DENY) {
			return;
		}

		if (ProtectionManager.checkExist(ev.entity, true)) {
			ev.setResult(Event.Result.DENY);
		}
	}

	// Fired AFTER the teleport
	@SubscribeEvent
	public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent ev) {
		Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(ev.player);
		if (!ProtectionManager.hasPermission(res, FlagType.ENTER, ev.player.dimension, (int) Math.floor(ev.player.posX),
				(int) Math.floor(ev.player.posY), (int) Math.floor(ev.player.posZ))) {
			// Because of badly written teleportation code by Mojang we can only
			// send the player back to spawn. :I
			res.respawnPlayer();
		}
	}

	// Changed to EmpiresUniverse.instance from Empires.instance in above lines^

	private BlockPosition createBlockPos(PlayerInteractEvent ev) {
		int x, y, z;

		if (ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
			x = (int) Math.floor(ev.entityPlayer.posX);
			y = (int) Math.floor(ev.entityPlayer.posY);
			z = (int) Math.floor(ev.entityPlayer.posZ);
		} else {
			x = ev.pos.getX();
			y = ev.pos.getY();
			z = ev.pos.getZ();
		}
		return new BlockPosition(x, y, z, ev.world.provider.getDimensionId());
	}
}