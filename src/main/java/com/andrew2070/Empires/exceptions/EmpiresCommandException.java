package com.andrew2070.Empires.exceptions;

import com.andrew2070.Empires.exceptions.CommandException;


public class EmpiresCommandException extends CommandException {
    public EmpiresCommandException(String key, Object... args) {
        super(key, args);
    }

    public EmpiresCommandException(String key, Throwable cause, Object... args) {
        this(key, args);
        initCause(cause);
    }
}