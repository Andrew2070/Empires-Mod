package com.EmpireMod.Empires.exceptions.Permission;

import com.EmpireMod.Empires.Empires;

import net.minecraft.command.CommandException;

public class PermissionCommandException extends CommandException {
	public PermissionCommandException(String localKey, Object... args) {
		super(Empires.instance.LOCAL.getLocalization(localKey, args).getUnformattedText());
	}
}