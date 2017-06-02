package com.andrew2070.Empires.entities.Empire;


import com.andrew2070.Empires.API.commands.LocalManager;
import com.andrew2070.Empires.API.commands.ChatManager;
import com.andrew2070.Empires.entities.Flags.FlagType;
import com.andrew2070.Empires.entities.Flags.Flag;


import net.minecraft.util.EnumChatFormatting;
import sun.util.locale.LocaleMatcher;

/**
 * Wilderness permissions
 */
public class Wild {

    public static final Wild instance = new Wild();

    public final Flag.Container flagsContainer = new Flag.Container();

    /**
     * Checks if Citizen is allowed to do the action specified by the FlagType in the Wild
     */
    public boolean hasPermission(Citizen res, FlagType<Boolean> flagType) {
        if (res == null) {
            return true;
        }

        if (!flagsContainer.getValue(flagType)) {
            ChatManager.send(res.getPlayer(), flagType.getDenialKey());
            ChatManager.send(res.getPlayer(), "Empires.notification.empire.owners", LocalManager.get("Empires.notification.empire.owners.admins"));
            return false;
        }
        return true;
    }
}