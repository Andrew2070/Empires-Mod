package com.andrew2070.Empires.Events;

import com.andrew2070.Empires.entities.Empire.Plot;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraftforge.common.MinecraftForge;

public class PlotEvent extends Event {
    public Plot plot = null;

    public PlotEvent(Plot plot) {
        this.plot = plot;
    }

    @Cancelable
    public static class PlotCreateEvent extends PlotEvent {
        public PlotCreateEvent(Plot plot) {
            super(plot);
        }
    }

    @Cancelable
    public static class PlotDeleteEvent extends PlotEvent {
        public PlotDeleteEvent(Plot plot) {
            super(plot);
        }
    }

    public static boolean fire(PlotEvent ev) {
        return MinecraftForge.EVENT_BUS.post(ev);
    }
}