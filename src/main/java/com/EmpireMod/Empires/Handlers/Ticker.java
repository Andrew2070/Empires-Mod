package com.EmpireMod.Empires.Handlers;


import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;


import com.EmpireMod.Empires.Empires;
import com.EmpireMod.Empires.Config.Config;
import com.EmpireMod.Empires.Datasource.EmpiresDatasource;
import com.EmpireMod.Empires.Datasource.EmpiresUniverse;
import com.EmpireMod.Empires.entities.Empire.AdminEmpire;
import com.EmpireMod.Empires.entities.Empire.Citizen;
import com.EmpireMod.Empires.entities.Empire.Empire;


import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.world.BlockEvent;

import java.util.Calendar;

public class Ticker {

    public static final Ticker instance = new Ticker();
    private boolean ticked = true;
    private int lastCalendarDay = -1;
    @SubscribeEvent
    public void onTickEvent(TickEvent.WorldTickEvent ev) {
        if(ev.side == Side.CLIENT)
            return;


        for(Citizen res : EmpiresUniverse.instance.citizens) {
            res.tick();
        }

        if((Config.instance.costEmpireUpkeep.get() > 0 || Config.instance.costAdditionalUpkeep.get() > 0) && ev.phase == TickEvent.Phase.START) {
            if (ticked) {
                if(lastCalendarDay != -1 && Calendar.getInstance().get(Calendar.DAY_OF_YEAR) != lastCalendarDay) {
                    for (int i = 0; i < EmpiresUniverse.instance.empires.size(); i++) {
                        Empire empire = EmpiresUniverse.instance.empires.get(i);
                        if (!(empire instanceof AdminEmpire)) {
                            empire.bank.payUpkeep();
                            if(empire.bank.getDaysNotPaid() == Config.instance.upkeepEmpireDeletionDays.get() && Config.instance.upkeepEmpireDeletionDays.get() > 0) {
                                Empires.instance.LOG.info("Empire {} has been deleted because it didn't pay upkeep for {} days.", empire.getName(), Config.instance.upkeepEmpireDeletionDays.get());
                                getDatasource().deleteEmpire(empire);
                            } else {
                                getDatasource().saveEmpireBank(empire.bank);
                            }
                        }
                    }
                    ticked = false;
                }
                lastCalendarDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
            } else {
                ticked = true;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent ev) {
        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(ev.player);
        if (res != null) {
            res.setPlayer(ev.player);
        } else {
            Empires.instance.LOG.error("Didn't create resident for player {} ({})", ev.player.getCommandSenderName(), ev.player.getPersistentID());
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent ev) {
        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(ev.player);
        if (res != null) {
            res.setPlayer(ev.player);
        }
    }

    @SubscribeEvent
    public void onPlayerBreaksBlock(BlockEvent.BreakEvent ev) {
        if (VisualsHandler.instance.isBlockMarked(ev.x, ev.y, ev.z, ev.world.provider.dimensionId, (EntityPlayerMP) ev.getPlayer())) {
            // Cancel event if it's a border that has been broken
            ev.setCanceled(true);
        }
    }

    private EmpiresDatasource getDatasource() {
        return Empires.instance.datasource;
    }
}