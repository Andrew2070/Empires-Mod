package com.EmpireMod.Empires.exceptions;

import com.EmpireMod.Empires.Empires;
import net.minecraft.command.WrongUsageException;

public class EmpiresWrongUsageException extends WrongUsageException {
    public EmpiresWrongUsageException(String key, Object... args) {
        super(Empires.instance.LOCAL.getLocalization(key, args).getUnformattedText());
    }
}