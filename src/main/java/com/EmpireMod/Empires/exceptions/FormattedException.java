package com.EmpireMod.Empires.exceptions;

import com.EmpireMod.Empires.Localization.LocalizationManager;

import net.minecraft.util.IChatComponent;

public abstract class FormattedException extends RuntimeException {

    public final IChatComponent message;

    public FormattedException(String localizationKey, Object... args) {
        message = LocalizationManager.get(localizationKey, args);
    }
}