package EmpiresMod.Events;

import EmpiresMod.entities.Empire.Relationship;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.common.MinecraftForge;

public class RelationEvent extends Event {
	public Relationship rel = null;

	public RelationEvent(Relationship rel) {
		this.rel = rel;
	}

	@Cancelable
	public static class RelationCreateEvent extends RelationEvent {
		public RelationCreateEvent(Relationship rel) {
			super(rel);
		}
	}

	@Cancelable
	public static class RelationDeleteEvent extends RelationEvent {
		public RelationDeleteEvent(Relationship rel) {
			super(rel);
		}
	}

	public static boolean fire(RelationEvent ev) {
		return MinecraftForge.EVENT_BUS.post(ev);
	}
}