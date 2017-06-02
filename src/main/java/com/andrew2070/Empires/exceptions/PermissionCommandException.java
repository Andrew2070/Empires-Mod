package com.andrew2070.Empires.exceptions;

import com.andrew2070.Empires.Empires;
import net.minecraft.command.CommandException;

public class PermissionCommandException extends CommandException {
    public PermissionCommandException(String localKey, Object... args) {
        super(Empires.instance.LOCAL.getLocalization(localKey, args).getUnformattedText());
    }
}