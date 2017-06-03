package com.EmpireMod.Empires.Events;


import com.EmpireMod.Empires.entities.Empire.Citizen;
import com.EmpireMod.Empires.entities.Empire.Empire;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraftforge.common.MinecraftForge;

public class EmpireEvent extends Event {
    public Empire empire = null;

    public EmpireEvent(Empire empire) {
        this.empire = empire;
    }

    @Cancelable
    public static class EmpireCreateEvent extends EmpireEvent {
        public EmpireCreateEvent(Empire empire) {
            super(empire);
        }
    }

    @Cancelable
    public static class EmpireDeleteEvent extends EmpireEvent {
        public EmpireDeleteEvent(Empire empire) {
            super(empire);
        }
    }

    // TODO: Make them cancelable?
    public static class EmpireEnterEvent extends EmpireEvent {
        public Citizen citizen = null;

        public EmpireEnterEvent(Empire empire, Citizen citizen) {
            super(empire);
            this.citizen = citizen;
        }
    }

    public static class EmpireEnterInRangeEvent extends EmpireEvent {
        public Citizen citizen = null;

        public EmpireEnterInRangeEvent(Empire empire, Citizen citizen) {
            super(empire);
            this.citizen = citizen;
        }
    }

    public static boolean fire(EmpireEvent ev) {
        return MinecraftForge.EVENT_BUS.post(ev);
    }
}