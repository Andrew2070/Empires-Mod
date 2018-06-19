package EmpiresMod.protection.Mods;
import com.builtbroken.mc.api.event.blast.BlastEventBlockEdit;
import com.builtbroken.mc.api.event.blast.BlastEventBlockRemoved;
import com.builtbroken.mc.api.event.blast.BlastEventBlockReplaced;
import com.builtbroken.mc.api.event.blast.BlastEventDestroyBlock;

import EmpiresMod.API.Chat.ForgeChatHandler;
import EmpiresMod.API.Commands.Command.CommandsEMP;
import EmpiresMod.Configuration.Config;
import EmpiresMod.entities.Empire.Empire;
import EmpiresMod.entities.Empire.EmpireBlock;
import EmpiresMod.entities.Flags.FlagType;
import EmpiresMod.exceptions.Command.CommandException;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
public class icbmClassic {
	public static final icbmClassic instance = new icbmClassic();

	@SubscribeEvent
	public void blockEditEvent (BlastEventBlockEdit event) {
		if (Config.instance.icbmMissilesExplosion.get() == true) {
		if (event.isCancelable()) {
			int dim = event.world.provider.dimensionId;
			float x = event.x;
			float y = event.y;
			float z = event.z;
			EmpireBlock block = CommandsEMP.getBlockFromPoint(dim, x, z); 
			try {
			Empire empire = block.getEmpire();
			if (empire.flagsContainer.getValue(FlagType.EXPLOSIONS) == false) {
			empire.notifyEveryone(CommandsEMP.getLocal().getLocalization("Empires.notification.ICBM.explosion.stopped"));
			event.setCanceled(true);
			}
			} catch (CommandException e) {
				if (block.getEmpire() == null) {
					x = x + 30;
					z = z + 75;
				}
				event.setCanceled(false);
			
		}
		}
		}

	}
	@SubscribeEvent
	public void blockEDestroyEventPre (BlastEventDestroyBlock.Pre event) {
		if (Config.instance.icbmMissilesExplosion.get() == true) {
		if (event.isCancelable()) {
			int dim = event.world.provider.dimensionId;
			float x = event.x;
			float y = event.y;
			float z = event.z;
			try {
			EmpireBlock block = CommandsEMP.getBlockFromPoint(dim, x, z); 
			Empire empire = block.getEmpire();
			if (empire.flagsContainer.getValue(FlagType.EXPLOSIONS) == false) {
			empire.notifyEveryone(CommandsEMP.getLocal().getLocalization("Empires.notification.ICBM.explosion.stopped"));
			event.setCanceled(true);
			}
			} catch (CommandException e) {
				event.setCanceled(false);
			}
		}
		}

	}
	@SubscribeEvent
	public void blockDestroyEventPre (BlastEventDestroyBlock.Pre event) {

		if (Config.instance.icbmMissilesExplosion.get() == true) {
		if (event.isCancelable()) {
			int dim = event.world.provider.dimensionId;
			float x = event.x;
			float y = event.y;
			float z = event.z;
			try {
			EmpireBlock block = CommandsEMP.getBlockFromPoint(dim, x, z); 
			Empire empire = block.getEmpire();
			if (empire.flagsContainer.getValue(FlagType.EXPLOSIONS) == false) {
			empire.notifyEveryone(CommandsEMP.getLocal().getLocalization("Empires.notification.ICBM.explosion.stopped"));
			event.setCanceled(true);
			}
			} catch (CommandException e) {
				event.setCanceled(false);
			}
		}
		}

	}
	
	@SubscribeEvent
	public void blockDestroyEvent (BlastEventDestroyBlock event) {
		if (Config.instance.icbmMissilesExplosion.get() == true) {
		if (event.isCancelable()) {
			int dim = event.world.provider.dimensionId;
			float x = event.x;
			float y = event.y;
			float z = event.z;
			System.out.println("ICBMDEBUG: Coords received:" + " " + x + " " + y + " " +z);
			try {
			EmpireBlock block = CommandsEMP.getBlockFromPoint(dim, x, z); 
			Empire empire = block.getEmpire();
			if (empire.flagsContainer.getValue(FlagType.EXPLOSIONS) == false) {
			empire.notifyEveryone(CommandsEMP.getLocal().getLocalization("Empires.notification.ICBM.explosion.stopped"));
			event.setCanceled(true);
			}
			} catch (CommandException e) {
				event.setCanceled(false);
			}
		}
		}

	}
	
	@SubscribeEvent
	public void blockRemoveEventPre (BlastEventBlockRemoved.Pre event) {
		if (Config.instance.icbmMissilesExplosion.get() == true) {
		if (event.isCancelable()) {
			int dim = event.world.provider.dimensionId;
			float x = event.x;
			float y = event.y;
			float z = event.z;
			try {
			EmpireBlock block = CommandsEMP.getBlockFromPoint(dim, x, z); 
			Empire empire = block.getEmpire();
			if (empire.flagsContainer.getValue(FlagType.EXPLOSIONS) == false) {
			empire.notifyEveryone(CommandsEMP.getLocal().getLocalization("Empires.notification.ICBM.explosion.stopped"));
			event.setCanceled(true);
			}
			} catch (CommandException e) {
				event.setCanceled(false);
			}
		}
		}

	}
	
	@SubscribeEvent
	public void blockRemoveEvent (BlastEventBlockRemoved event) {
		if (Config.instance.icbmMissilesExplosion.get() == true) {
		if (event.isCancelable()) {
			int dim = event.world.provider.dimensionId;
			float x = event.x;
			float y = event.y;
			float z = event.z;
			try {
			EmpireBlock block = CommandsEMP.getBlockFromPoint(dim, x, z); 
			Empire empire = block.getEmpire();
			if (empire.flagsContainer.getValue(FlagType.EXPLOSIONS) == false) {
			empire.notifyEveryone(CommandsEMP.getLocal().getLocalization("Empires.notification.ICBM.explosion.stopped"));
			event.setCanceled(true);
			}
			} catch (CommandException e) {
				event.setCanceled(false);
			}
		}
		}

	}
	
	@SubscribeEvent
	public void blockReplaceEventPre(BlastEventBlockReplaced.Pre event) {
		if (Config.instance.icbmMissilesExplosion.get() == true) {
		if (event.isCancelable()) {
			int dim = event.world.provider.dimensionId;
			float x = event.x;
			float y = event.y;
			float z = event.z;
			try {
			EmpireBlock block = CommandsEMP.getBlockFromPoint(dim, x, z); 
			Empire empire = block.getEmpire();
			if (empire.flagsContainer.getValue(FlagType.EXPLOSIONS) == false) {
			empire.notifyEveryone(CommandsEMP.getLocal().getLocalization("Empires.notification.ICBM.explosion.stopped"));
			event.setCanceled(true);
			}
			} catch (CommandException e) {
				event.setCanceled(false);
			}
		}
		}

	}
	@SubscribeEvent
	public void blockReplaceEvent(BlastEventBlockReplaced event) {
		if (Config.instance.icbmMissilesExplosion.get() == true) {
		if (event.isCancelable()) {
			int dim = event.world.provider.dimensionId;
			float x = event.x;
			float y = event.y;
			float z = event.z;
			try {
			EmpireBlock block = CommandsEMP.getBlockFromPoint(dim, x, z); 
			Empire empire = block.getEmpire();
			if (empire.flagsContainer.getValue(FlagType.EXPLOSIONS) == false) {
			empire.notifyEveryone(CommandsEMP.getLocal().getLocalization("Empires.notification.ICBM.explosion.stopped"));
			event.setCanceled(true);
			}
			} catch (CommandException e) {
				event.setCanceled(false);
			}
		}
		}

	}
	
	
}
