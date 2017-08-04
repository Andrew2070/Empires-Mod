package com.EmpireMod.Empires.entities.Empire;


import com.EmpireMod.Empires.API.Chat.Component.ChatManager;
import com.EmpireMod.Empires.Localization.LocalizationManager;
import com.EmpireMod.Empires.entities.Flags.FlagType;
import com.EmpireMod.Empires.entities.Flags.Flag;

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
            ChatManager.send(res.getPlayer(), "Empires.notification.empire.owners", LocalizationManager.get("Empires.notification.empire.owners.admins"));
            return false;
        }
        return true;
    }
}