package EmpiresMod.Events;

import EmpiresMod.entities.Empire.EmpireBlock;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.common.MinecraftForge;

public class EmpireBlockEvent extends Event {
	public EmpireBlock block = null;

	public EmpireBlockEvent(EmpireBlock block) {
		this.block = block;
	}

	@Cancelable
	public static class BlockCreateEvent extends EmpireBlockEvent {
		public BlockCreateEvent(EmpireBlock block) {
			super(block);
		}
	}

	@Cancelable
	public static class BlockDeleteEvent extends EmpireBlockEvent {
		public BlockDeleteEvent(EmpireBlock block) {
			super(block);
		}
	}

	public static boolean fire(EmpireBlockEvent ev) {
		return MinecraftForge.EVENT_BUS.post(ev);
	}
}