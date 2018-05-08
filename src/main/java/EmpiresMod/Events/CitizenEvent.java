package EmpiresMod.Events;

import EmpiresMod.entities.Empire.Citizen;
import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraftforge.common.MinecraftForge;

public class CitizenEvent extends Event {
	public Citizen citizen = null;

	public CitizenEvent(Citizen citizen) {
		this.citizen = citizen;
	}

	@Cancelable
	public static class CitizenCreateEvent extends CitizenEvent {
		public CitizenCreateEvent(Citizen citizen) {
			super(citizen);
		}
	}

	@Cancelable
	public static class CitizenDeleteEvent extends CitizenEvent {
		public CitizenDeleteEvent(Citizen citizen) {
			super(citizen);
		}
	}

	public static boolean fire(CitizenEvent ev) {
		return MinecraftForge.EVENT_BUS.post(ev);
	}
}