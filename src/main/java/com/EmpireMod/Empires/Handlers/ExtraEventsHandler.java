package com.EmpireMod.Empires.Handlers;


import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import net.minecraftforge.event.world.ExplosionEvent;

import com.EmpireMod.Empires.entities.Position.ChunkPos;
import com.EmpireMod.Empires.utils.WorldUtils;
import com.EmpireMod.Empires.Empires;
import com.EmpireMod.Empires.entities.Empire.Wild;
import com.EmpireMod.Empires.entities.Empire.EmpireBlock;
import com.EmpireMod.Empires.entities.Flags.FlagType;
import com.EmpireMod.Empires.Datasource.EmpiresUniverse;

import java.util.List;

/**
 * Handling any events that are not yet compatible with the most commonly used version of forge.
 */
public class ExtraEventsHandler {

    private static ExtraEventsHandler instance;
    public static ExtraEventsHandler getInstance() {
        if(instance == null)
            instance = new ExtraEventsHandler();
        return instance;
    }

    /**
     * Forge 1254 is needed for this
     */
    @SubscribeEvent
    public void onExplosion(ExplosionEvent.Start ev) {
        if(ev.world.isRemote)
            return;
        if (ev.isCanceled())
            return;
        List<ChunkPos> chunks = WorldUtils.getChunksInBox(ev.world.provider.dimensionId, (int) (ev.explosion.explosionX - ev.explosion.explosionSize - 2), (int) (ev.explosion.explosionZ - ev.explosion.explosionSize - 2), (int) (ev.explosion.explosionX + ev.explosion.explosionSize + 2), (int) (ev.explosion.explosionZ + ev.explosion.explosionSize + 2));
        for(ChunkPos chunk : chunks) {
            EmpireBlock block = EmpiresUniverse.instance.blocks.get(ev.world.provider.dimensionId, chunk.getX(), chunk.getZ());
            //Changed to EmpiresUniverse.instance from Empires.instance in above line
            if(block == null) {
                if(!(Boolean)Wild.instance.flagsContainer.getValue(FlagType.EXPLOSIONS)) {
                    ev.setCanceled(true);
                    return;
                }
            } else {
                if (!(Boolean) block.getEmpire().flagsContainer.getValue(FlagType.EXPLOSIONS)) {
                    ev.setCanceled(true);
                    block.getEmpire().notifyEveryone(Empires.instance.LOCAL.getLocalization(FlagType.EXPLOSIONS.getEmpireNotificationKey()));
                    return;
                }
            }
        }
    }
}