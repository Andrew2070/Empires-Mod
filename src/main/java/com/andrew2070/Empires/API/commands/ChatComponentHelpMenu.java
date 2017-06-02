package com.andrew2070.Empires.API.commands;


import com.andrew2070.Empires.API.commands.ChatComponentContainer;
import com.andrew2070.Empires.API.commands.ChatComponentFormatted;
import com.andrew2070.Empires.API.commands.ChatComponentMultiPage;
import com.andrew2070.Empires.API.commands.LocalManager;
import com.andrew2070.Empires.API.permissions.CommandTreeNode;
import com.andrew2070.Empires.entities.Permissions.Tree;
import com.andrew2070.Empires.entities.Permissions.TreeNode;

import net.minecraft.util.IChatComponent;

import java.util.List;

public class ChatComponentHelpMenu extends ChatComponentMultiPage {

    private CommandTreeNode command;

    public ChatComponentHelpMenu(int maxComponentsPerPage, CommandTreeNode command) {
        super(maxComponentsPerPage);
        this.command = command;
        this.construct();
    }

    public void construct() {

        for (CommandTreeNode subCommand : command.getChildren()) {
            this.add(new ChatComponentFormatted("{7| %s << %s}", subCommand.getCommandLine(), LocalManager.get(subCommand.getAnnotation().permission() + ".help")));
        }

    }

    @Override
    public ChatComponentContainer getHeader(int page) {
        ChatComponentContainer header = super.getHeader(page);
        header.add(new ChatComponentFormatted("{9| - Command: }{9o|%s}", command.getLocalizedSyntax()));
        return header;
    }
}