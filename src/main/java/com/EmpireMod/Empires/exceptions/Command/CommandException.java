package com.EmpireMod.Empires.exceptions.Command;

import com.EmpireMod.Empires.exceptions.Format.FormattedException;

public class CommandException extends FormattedException {

    public CommandException(String localizationKey, Object... args) {
        super(localizationKey, args);
    }
}