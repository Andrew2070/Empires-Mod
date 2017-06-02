package com.andrew2070.Empires.exceptions;

import com.andrew2070.Empires.Empires;
import net.minecraft.command.WrongUsageException;

public class EmpiresWrongUsageException extends WrongUsageException {
    public EmpiresWrongUsageException(String key, Object... args) {
        super(Empires.instance.LOCAL.getLocalization(key, args).getUnformattedText());
    }
}