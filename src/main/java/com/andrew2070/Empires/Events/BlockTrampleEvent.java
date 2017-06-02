package com.andrew2070.Empires.Events;


import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFarmland;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;

/**
 * Fired when an entity destroys tilled land and restores it to dirt.
 */
@Cancelable
public class BlockTrampleEvent extends BlockEvent {
    /**
     * The entity that trampled the farm land
     */
    public final Entity entity;

    public BlockTrampleEvent(Entity entity, int x, int y, int z, Block block, int blockMetadata) {
        super(x, y, z, entity.worldObj, block, blockMetadata);
        this.entity = entity;
    }

    @SuppressWarnings("unused")
    public static boolean fireEvent(Entity entity, BlockFarmland block, int x, int y, int z) {
        return MinecraftForge.EVENT_BUS.post(new BlockTrampleEvent(entity, x, y, z, block, entity.worldObj.getBlockMetadata(x, y, z)));
    }
}
