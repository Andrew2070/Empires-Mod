package com.EmpireMod.Empires.commands.Permission;



import com.EmpireMod.Empires.API.Chat.Component.ChatComponentFormatted;
import com.EmpireMod.Empires.API.Chat.Component.ChatComponentList;
import com.EmpireMod.Empires.API.Chat.Component.ChatManager;
import com.EmpireMod.Empires.API.Commands.Command.Command;
import com.EmpireMod.Empires.API.Commands.Command.CommandResponse;
import com.EmpireMod.Empires.Empires;
import com.EmpireMod.Empires.exceptions.PermissionCommandException;
import com.EmpireMod.Empires.API.permissions.PermissionProxy;
import com.EmpireMod.Empires.API.permissions.Bridges.MyPermissionsBridge;
import com.EmpireMod.Empires.Localization.LocalizationManager;
import com.EmpireMod.Empires.Utilities.ColorUtils;
import com.EmpireMod.Empires.Utilities.PlayerUtils;
import com.EmpireMod.Empires.entities.Permissions.Group;
import com.EmpireMod.Empires.entities.Permissions.User;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.util.List;
import java.util.UUID;

public class PermCommands {

    protected static Group getGroupFromName(String name) {
        Group group = getManager().groups.get(name);
        if(group == null) {
            throw new PermissionCommandException("Empires.cmd.err.group.notExist", LocalizationManager.get("Empires.format.group.short", name));
        }
        return group;
    }

    protected static UUID getUUIDFromUsername(String username) {
        UUID uuid = PlayerUtils.getUUIDFromUsername(username);
        if(uuid == null) {
            throw new PermissionCommandException("Empires.cmd.err.player.notExist", LocalizationManager.get("Empires.format.user.short", username));
        }
        return uuid;
    }

    @Command(
            name = "empireperm",
            permission = "Empires.cmd",
            syntax = "/empireperm <command>",
            alias = {"p", "perm"})
    public static CommandResponse permCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "config",
            permission = "Empires.cmd.config",
            parentName = "Empires.cmd",
            syntax = "/empireperm config <command>")
    public static CommandResponse configCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "reload",
            permission = "Empires.cmd.config.reload",
            parentName = "Empires.cmd.config",
            syntax = "/empireperm config reload")
    public static CommandResponse configReloadCommand(ICommandSender sender, List<String> args) {
        Empires.instance.loadConfig();
        // REF: Change these to localized versions of themselves
        ChatManager.send(sender, "Empires.notification.config.reloaded");
        if(PermissionProxy.getPermissionManager() instanceof MyPermissionsBridge) {
            ((MyPermissionsBridge) PermissionProxy.getPermissionManager()).loadConfigs();
            ChatManager.send(sender, "Empires.notification.permissions.config.reloaded");
        } else {
            ChatManager.send(sender, "Empires.notification.permissions.third_party");
        }
        return CommandResponse.DONE;
    }

    public static class MyPermissionManagerCommands {
        @Command(
                name = "group",
                permission = "Empires.cmd.group",
                parentName = "Empires.cmd",
                syntax = "/perm group <command>")
        public static CommandResponse groupCommand(ICommandSender sender, List<String> args) {
            return CommandResponse.SEND_HELP_MESSAGE;
        }

        @Command(
                name = "add",
                permission = "Empires.cmd.group.add",
                parentName = "Empires.cmd.group",
                syntax = "/perm group add <name> [parents]")
        public static CommandResponse groupAddCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 1) {
                return CommandResponse.SEND_SYNTAX;
            }

            Group group = new Group(args.get(0));
            getManager().groups.add(group);

            getManager().saveGroups();
            ChatManager.send(sender, "Empires.notification.group.added");
            return CommandResponse.DONE;
        }

        @Command(
                name = "delete",
                permission = "Empires.cmd.group.delete",
                parentName = "Empires.cmd.group",
                syntax = "/perm group delete <name>")
        public static CommandResponse groupDeleteCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 1) {
                return CommandResponse.SEND_SYNTAX;
            }

            Group group = getGroupFromName(args.get(0));
            getManager().groups.remove(group);
            getManager().saveGroups();
            ChatManager.send(sender, "Empires.notification.group.deleted");
            return CommandResponse.DONE;
        }

        @Command(
                name = "rename",
                permission = "Empires.cmd.group.rename",
                parentName = "Empires.cmd.group",
                syntax = "/perm group rename <group> <name>")
        public static CommandResponse groupRenameCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 2) {
                return CommandResponse.SEND_SYNTAX;
            }

            Group group = getGroupFromName(args.get(0));
            group.setName(args.get(1));
            getManager().saveGroups();
            ChatManager.send(sender, "Empires.notification.group.renamed");
            return CommandResponse.DONE;
        }

        @Command(
                name = "list",
                permission = "Empires.cmd.group.list",
                parentName = "Empires.cmd.group",
                syntax = "/perm group list")
        public static CommandResponse groupListCommand(ICommandSender sender, List<String> args) {
            IChatComponent root = new ChatComponentList();
            root.appendSibling(LocalizationManager.get("Empires.format.list.header", new ChatComponentFormatted("{9|GROUPS}")));

            for (Group group : getManager().groups) {
                ChatComponentText parents = new ChatComponentText("");
                for (Group parent : group.parents) {
                    IChatComponent parentComponent = LocalizationManager.get("Empires.format.group.parent", new ChatComponentText(parent.getName()));
                    if (parents.getSiblings().size() == 0) {
                        parents.appendSibling(parentComponent);
                    } else {
                        parents.appendSibling(new ChatComponentText(", ").setChatStyle(ColorUtils.styleComma))
                                .appendSibling(parentComponent);
                    }
                }

                root.appendSibling(LocalizationManager.get("Empires.format.group.long", group.getName(), parents));
            }

            ChatManager.send(sender, root);
            return CommandResponse.DONE;
        }

        @Command(
                name = "perm",
                permission = "Empires.cmd.group.perm",
                parentName = "Empires.cmd.group",
                syntax = "/perm group perm <command>")
        public static CommandResponse groupPermCommand(ICommandSender sender, List<String> args) {
            return CommandResponse.SEND_HELP_MESSAGE;
        }

        @Command(
                name = "add",
                permission = "Empires.cmd.group.perm.add",
                parentName = "Empires.cmd.group.perm",
                syntax = "/perm group perm add <group> <perm>")
        public static CommandResponse groupPermAddCommand(ICommandSender sender, List<String> args) {
            if(args.size() < 2) {
                return CommandResponse.SEND_SYNTAX;
            }

            Group group = getGroupFromName(args.get(0));
            group.permsContainer.add(args.get(1));
            getManager().saveGroups();
            ChatManager.send(sender, "Empires.notification.perm.added");

            return CommandResponse.DONE;
        }

        @Command(
                name = "remove",
                permission = "Empires.cmd.group.perm.remove",
                parentName = "Empires.cmd.group.perm",
                syntax = "/perm group perm remove <group> <perm>")
        public static CommandResponse groupPermRemoveCommand(ICommandSender sender, List<String> args) {
            if(args.size() < 2) {
                return CommandResponse.SEND_SYNTAX;
            }

            Group group = getGroupFromName(args.get(0));
            group.permsContainer.remove(args.get(1));
            getManager().saveGroups();
            ChatManager.send(sender, "Empires.notification.perm.removed");

            return CommandResponse.DONE;
        }

        @Command(
                name = "list",
                permission = "Empires.cmd.group.perm.list",
                parentName = "Empires.cmd.group.perm",
                syntax = "/perm group perm list <group>")
        public static CommandResponse groupPermListCommand(ICommandSender sender, List<String> args) {
            if(args.size() < 1) {
                return CommandResponse.SEND_SYNTAX;
            }

            Group group = getGroupFromName(args.get(0));
            ChatManager.send(sender, group.permsContainer.toChatMessage());
            return CommandResponse.DONE;
        }

        @Command(
                name = "user",
                permission = "Empires.cmd.user",
                parentName = "Empires.cmd",
                syntax = "/perm user <command>")
        public static CommandResponse userCommand(ICommandSender sender, List<String> args) {
            return CommandResponse.SEND_HELP_MESSAGE;
        }

        @Command(
                name = "group",
                permission = "Empires.cmd.user.group",
                parentName = "Empires.cmd.user",
                syntax = "/perm user group <command>")
        public static CommandResponse userGroupCommand(ICommandSender sender, List<String> args) {
            return CommandResponse.SEND_HELP_MESSAGE;
        }

        @Command(
                name = "show",
                permission = "Empires.cmd.user.group.show",
                parentName = "Empires.cmd.user.group",
                syntax = "/perm user group show <player>")
        public static CommandResponse userGroupShowCommand(ICommandSender sender, List<String> args) {
            if(args.size() < 1) {
                return CommandResponse.SEND_SYNTAX;
            }

            UUID uuid = getUUIDFromUsername(args.get(0));
            User user = getManager().users.get(uuid);

            ChatManager.send(sender, "Empires.notification.user.group",  user, user.group);

            return CommandResponse.DONE;
        }

        @Command(
                name = "set",
                permission = "Empires.cmd.user.group.set",
                parentName = "Empires.cmd.user.group",
                syntax = "/perm user group set <player> <group>")
        public static CommandResponse userGroupSetCommand(ICommandSender sender, List<String> args) {
            if(args.size() < 2) {
                return CommandResponse.SEND_SYNTAX;
            }

            UUID uuid = getUUIDFromUsername(args.get(0));
            Group group = getGroupFromName(args.get(1));

            User user = getManager().users.get(uuid);
            if(user == null) {
                getManager().users.add(new User(uuid, group));
            } else {
                user.group = group;
            }
            getManager().saveUsers();
            ChatManager.send(sender, "Empires.notification.user.group.set");

            return CommandResponse.DONE;
        }

        @Command(
                name = "list",
                permission = "Empires.cmd.user.list",
                parentName = "Empires.cmd.user",
                syntax = "/perm user list")
        public static CommandResponse userListCommand(ICommandSender sender, List<String> args) {
            ChatComponentList root = new ChatComponentList();
            root.appendSibling(LocalizationManager.get("Empires.format.list.header", new ChatComponentFormatted("{9|USERS}")));

            for (User user : getManager().users) {
                root.appendSibling(LocalizationManager.get("Empires.format.user.long", user.lastPlayerName, user.group));
            }

            return CommandResponse.DONE;
        }

            @Command(
                name = "perm",
                permission = "Empires.cmd.user.perm",
                parentName = "Empires.cmd.user",
                syntax = "/perm user perm <command>")
        public static CommandResponse userPermCommand(ICommandSender sender, List<String> args) {
            return CommandResponse.SEND_HELP_MESSAGE;
        }

        @Command(
                name = "add",
                permission = "Empires.cmd.user.perm.add",
                parentName = "Empires.cmd.user.perm",
                syntax = "/perm user perm add <player> <perm>")
        public static CommandResponse userPermAddCommand(ICommandSender sender, List<String> args) {
            if(args.size() < 2) {
                return CommandResponse.SEND_SYNTAX;
            }

            UUID uuid = getUUIDFromUsername(args.get(0));
            User user = getManager().users.get(uuid);
            user.permsContainer.add(args.get(1));
            getManager().saveUsers();
            ChatManager.send(sender, "Empires.notification.perm.added");

            return CommandResponse.DONE;
        }

        @Command(
                name = "remove",
                permission = "Empires.cmd.user.perm.remove",
                parentName = "Empires.cmd.user.perm",
                syntax = "/perm user perm remove <player> <perm>")
        public static CommandResponse userPermRemoveCommand(ICommandSender sender, List<String> args) {
            if(args.size() < 2) {
                return CommandResponse.SEND_SYNTAX;
            }

            UUID uuid = getUUIDFromUsername(args.get(0));
            User user = getManager().users.get(uuid);
            user.permsContainer.remove(args.get(1));
            getManager().saveUsers();
            ChatManager.send(sender, "Empires.notification.perm.removed");

            return CommandResponse.DONE;
        }

        @Command(
                name = "list",
                permission = "Empires.cmd.user.perm.list",
                parentName = "Empires.cmd.user.perm",
                syntax = "/perm user perm list <player>")
        public static CommandResponse userPermListCommand(ICommandSender sender, List<String> args) {
            if(args.size() < 1) {
                return CommandResponse.SEND_SYNTAX;
            }

            UUID uuid = getUUIDFromUsername(args.get(0));
            User user = getManager().users.get(uuid);

            getManager().saveUsers();
            ChatManager.send(sender, user.permsContainer.toChatMessage());

            return CommandResponse.DONE;
        }
    }

    private static MyPermissionsBridge getManager() {
        return (MyPermissionsBridge) PermissionProxy.getPermissionManager();
    }
}