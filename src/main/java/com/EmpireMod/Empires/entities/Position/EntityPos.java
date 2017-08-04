package com.EmpireMod.Empires.entities.Position;


import com.EmpireMod.Empires.Empires;
import com.EmpireMod.Empires.API.Chat.IChatFormat;

import net.minecraft.util.IChatComponent;



/**
 * Helper class for storing position of an entity
 */
public class EntityPos implements IChatFormat {
    private final int dim;
    private final double x;
    private final double y;
    private final double z;

    public EntityPos(double x, double y, double z, int dim) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dim = dim;
    }

    public int getDim() {
        return dim;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    @Override
    public String toString() {
        return toChatMessage().getUnformattedText();
    }

    @Override
    public IChatComponent toChatMessage() {
        return Empires.instance.LOCAL.getLocalization("Empires.format.entitypos", x, y, z, dim);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityPos) {
            EntityPos other = (EntityPos) obj;
            return other.x == x && other.y == y && other.z == z && other.dim == dim;
        } else {
            return super.equals(obj);
        }
    }
}