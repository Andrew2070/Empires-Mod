package com.andrew2070.Empires.exceptions;

import com.andrew2070.Empires.exceptions.FormattedException;

public class CommandException extends FormattedException {

    public CommandException(String localizationKey, Object... args) {
        super(localizationKey, args);
    }
}