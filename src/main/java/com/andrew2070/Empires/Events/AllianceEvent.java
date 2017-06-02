package com.andrew2070.Empires.Events;


import com.andrew2070.Empires.entities.Empire.Alliance;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraftforge.common.MinecraftForge;


public class AllianceEvent extends Event {
    public Alliance alliance = null;

    public AllianceEvent(Alliance alliance) {
        this.alliance = alliance;
    }

    @Cancelable
    public static class AllianceCreateEvent extends AllianceEvent {
        public AllianceCreateEvent(Alliance alliance) {
            super(alliance);
        }
    }

    @Cancelable
    public static class AllianceDeleteEvent extends AllianceEvent {
        public AllianceDeleteEvent(Alliance alliance) {
            super(alliance);
        }
    }

    public static boolean fire(AllianceEvent ev) {
        return MinecraftForge.EVENT_BUS.post(ev);
    }
}