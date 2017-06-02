package com.andrew2070.Empires.API.permissions;


import cpw.mods.fml.common.Optional;
import com.andrew2070.Empires.API.permissions.PermissionProxy;
import com.andrew2070.Empires.API.permissions.Bridges.ForgeEssentialsPermissionBridge;
import com.andrew2070.Empires.API.permissions.PermissionLevel;
import com.andrew2070.Empires.API.commands.ForgeEssentialsCommandRegistrar;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

import com.andrew2070.Empires.API.permissions.PermissionObject;

import java.util.Arrays;
import java.util.List;

/**
 * Command model which instantiates all base commands that need to be registered to Minecraft
 */
@Optional.InterfaceList({
        @Optional.Interface(iface = "net.minecraftforge.permission.PermissionObject", modid = "ForgeEssentials")})
public class CommandModel extends CommandBase implements PermissionObject {

    private CommandTree commandTree;

    public CommandModel(CommandTree commandTree) {
        this.commandTree = commandTree;
    }

    @Override
    public List getCommandAliases() {
        return Arrays.asList(commandTree.getRoot().getAnnotation().alias());
    }

    @Override
    public String getCommandName() {
        return commandTree.getRoot().getLocalizedName();
    }

    public String getCommandUsage(ICommandSender sender) {
        return commandTree.getRoot().getLocalizedSyntax();
    }

    /**
     * Processes the command by calling the method that was linked to it.
     */
    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        commandTree.commandCall(sender, Arrays.asList(args));
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        CommandTreeNode node = commandTree.getNodeFromArgs(Arrays.asList(args));

        int argumentNumber = commandTree.getArgumentNumber(Arrays.asList(args));
        if(argumentNumber < 0)
            return null;

        return node.getTabCompletionList(argumentNumber, args[args.length - 1]);
    }

    /**
     * This method does not have enough arguments to check for subcommands down the command trees therefore it always returns true.
     * The check is moved directly to the processCommand method.
     */
    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public String getPermissionNode() {
        return commandTree.getRoot().getAnnotation().permission();
    }

    @Override
    @Optional.Method(modid = "ForgeEssentials")
    public PermissionLevel getPermissionLevel() {
        return PermissionLevel.fromBoolean(!(PermissionProxy.getPermissionManager() instanceof ForgeEssentialsPermissionBridge));
    }
}
