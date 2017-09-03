package com.EmpireMod.Empires.API.Chat.Component;

import com.EmpireMod.Empires.API.permissions.CommandTreeNode;
import com.EmpireMod.Empires.Localization.LocalizationManager;

public class ChatComponentHelpMenu extends ChatComponentMultiPage {

	private CommandTreeNode command;

	public ChatComponentHelpMenu(int maxComponentsPerPage, CommandTreeNode command) {
		super(maxComponentsPerPage);
		this.command = command;
		this.construct();
	}

	public void construct() {

		for (CommandTreeNode subCommand : command.getChildren()) {
			this.add(new ChatComponentFormatted("{9| %s << %s}", subCommand.getCommandLine(),
					LocalizationManager.get(subCommand.getAnnotation().permission() + ".help")));
		}

	}

	@Override
	public ChatComponentContainer getHeader(int page) {
		ChatComponentContainer header = super.getHeader(page);
		header.add(new ChatComponentFormatted("{6| - Command Syntax: }{9o|%s}", command.getLocalizedSyntax()));
		return header;
	}
}