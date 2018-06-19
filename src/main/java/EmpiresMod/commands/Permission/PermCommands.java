package EmpiresMod.commands.Permission;

import java.util.List;
import java.util.UUID;

import EmpiresMod.Empires;
import EmpiresMod.API.Chat.Component.ChatComponentFormatted;
import EmpiresMod.API.Chat.Component.ChatComponentList;
import EmpiresMod.API.Chat.Component.ChatManager;
import EmpiresMod.API.Commands.Command.Command;
import EmpiresMod.API.Commands.Command.CommandResponse;
import EmpiresMod.API.permissions.PermissionProxy;
import EmpiresMod.API.permissions.Bridges.EmpiresBridge;
import EmpiresMod.Localization.LocalizationManager;
import EmpiresMod.Utilities.ColorUtils;
import EmpiresMod.Utilities.PlayerUtils;
import EmpiresMod.entities.Permissions.Group;
import EmpiresMod.entities.Permissions.User;
import EmpiresMod.exceptions.Permission.PermissionCommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public class PermCommands {

	protected static Group getGroupFromName(String name) {
		Group group = getManager().groups.get(name);
		if (group == null) {
			throw new PermissionCommandException("Empires.perm.cmd.err.group.notExist",
					LocalizationManager.get("Empires.format.group.short", name));
		}
		return group;
	}

	protected static UUID getUUIDFromUsername(String username) {
		UUID uuid = PlayerUtils.getUUIDFromUsername(username);
		if (uuid == null) {
			throw new PermissionCommandException("Empires.perm.cmd.err.player.notExist",
					LocalizationManager.get("Empires.format.user.short", username));
		}
		return uuid;
	}

	@Command(name = "empireperm", permission = "Empires.perm.cmd", syntax = "/empireperm <command>", alias = { "empp", "empperm" })
	public static CommandResponse permCommand(ICommandSender sender, List<String> args) {
		return CommandResponse.SEND_HELP_MESSAGE;
	}

	@Command(name = "config", permission = "Empires.perm.cmd", parentName = "Empires.perm.cmd", syntax = "/empireperm config <command>")
	public static CommandResponse configCommand(ICommandSender sender, List<String> args) {
		return CommandResponse.SEND_HELP_MESSAGE;
	}

	@Command(name = "reload", permission = "Empires.perm.cmd.config.reload", parentName = "Empires.perm.cmd.config", syntax = "/empireperm config reload")
	public static CommandResponse configReloadCommand(ICommandSender sender, List<String> args) {
		Empires.instance.loadConfig();
		// REF: Change these to localized versions of themselves
		ChatManager.send(sender, "Empires.notification.config.reloaded");
		if (PermissionProxy.getPermissionManager() instanceof EmpiresBridge) {
			((EmpiresBridge) PermissionProxy.getPermissionManager()).loadConfigs();
			ChatManager.send(sender, "Empires.notification.permissions.config.reloaded");
		} else {
			ChatManager.send(sender, "Empires.notification.permissions.third_party");
		}
		return CommandResponse.DONE;
	}

		@Command(name = "group", permission = "Empires.perm.cmd.group", parentName = "Empires.perm.cmd", syntax = "/empireperm group <command>")
		public static CommandResponse groupCommand(ICommandSender sender, List<String> args) {
			return CommandResponse.SEND_HELP_MESSAGE;
		}

		@Command(name = "add", permission = "Empires.perm.cmd.group.add", parentName = "Empires.perm.cmd.group", syntax = "/empireperm group add <name> [parents]")
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

		@Command(name = "delete", permission = "Empires.perm.cmd.group.delete", parentName = "Empires.perm.cmd.group", syntax = "/empireperm group delete <name>")
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

		@Command(name = "rename", permission = "Empires.perm.cmd.group.rename", parentName = "Empires.perm.cmd.group", syntax = "/empireperm group rename <group> <name>")
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

		@Command(name = "list", permission = "Empires.perm.cmd.group.list", parentName = "Empires.perm.cmd.group", syntax = "/empireperm group list")
		public static CommandResponse groupListCommand(ICommandSender sender, List<String> args) {
			IChatComponent root = new ChatComponentList();
			root.appendSibling(
					LocalizationManager.get("Empires.format.list.header", new ChatComponentFormatted("{9|GROUPS}")));

			for (Group group : getManager().groups) {
				ChatComponentText parents = new ChatComponentText("");
				for (Group parent : group.parents) {
					IChatComponent parentComponent = LocalizationManager.get("Empires.format.group.parent",
							new ChatComponentText(parent.getName()));
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

		@Command(name = "perm", permission = "Empires.perm.cmd.group.perm", parentName = "Empires.perm.cmd.group", syntax = "/empireperm group perm <command>")
		public static CommandResponse groupPermCommand(ICommandSender sender, List<String> args) {
			return CommandResponse.SEND_HELP_MESSAGE;
		}

		@Command(name = "add", permission = "Empires.perm.cmd.group.perm.add", parentName = "Empires.perm.cmd.group.perm", syntax = "/empireperm group perm add <group> <perm>")
		public static CommandResponse groupPermAddCommand(ICommandSender sender, List<String> args) {
			if (args.size() < 2) {
				return CommandResponse.SEND_SYNTAX;
			}

			Group group = getGroupFromName(args.get(0));
			group.permsContainer.add(args.get(1));
			getManager().saveGroups();
			ChatManager.send(sender, "Empires.notification.perm.added");

			return CommandResponse.DONE;
		}

		@Command(name = "remove", permission = "Empires.perm.cmd.group.perm.remove", parentName = "Empires.perm.cmd.group.perm", syntax = "/empireperm group perm remove <group> <perm>")
		public static CommandResponse groupPermRemoveCommand(ICommandSender sender, List<String> args) {
			if (args.size() < 2) {
				return CommandResponse.SEND_SYNTAX;
			}

			Group group = getGroupFromName(args.get(0));
			group.permsContainer.remove(args.get(1));
			getManager().saveGroups();
			ChatManager.send(sender, "Empires.notification.perm.removed");

			return CommandResponse.DONE;
		}

		@Command(name = "list", permission = "Empires.perm.cmd.group.perm.list", parentName = "Empires.perm.cmd.group.perm", syntax = "/empireperm group perm list <group>")
		public static CommandResponse groupPermListCommand(ICommandSender sender, List<String> args) {
			if (args.size() < 1) {
				return CommandResponse.SEND_SYNTAX;
			}

			Group group = getGroupFromName(args.get(0));
			ChatManager.send(sender, group.permsContainer.toChatMessage());
			return CommandResponse.DONE;
		}

		@Command(name = "user", permission = "Empires.perm.cmd.user", parentName = "Empires.perm.cmd", syntax = "/empireperm user <command>")
		public static CommandResponse userCommand(ICommandSender sender, List<String> args) {
			return CommandResponse.SEND_HELP_MESSAGE;
		}

		@Command(name = "group", permission = "Empires.perm.cmd.user.group", parentName = "Empires.perm.cmd.user", syntax = "/empireperm user group <command>")
		public static CommandResponse userGroupCommand(ICommandSender sender, List<String> args) {
			return CommandResponse.SEND_HELP_MESSAGE;
		}

		@Command(name = "show", permission = "Empires.perm.cmd.user.group.show", parentName = "Empires.perm.cmd.user.group", syntax = "/empireperm user group show <player>")
		public static CommandResponse userGroupShowCommand(ICommandSender sender, List<String> args) {
			if (args.size() < 1) {
				return CommandResponse.SEND_SYNTAX;
			}

			UUID uuid = getUUIDFromUsername(args.get(0));
			User user = getManager().users.get(uuid);

			ChatManager.send(sender, "Empires.notification.user.group", user, user.group);

			return CommandResponse.DONE;
		}

		@Command(name = "set", permission = "Empires.perm.cmd.user.group.set", parentName = "Empires.perm.cmd.user.group", syntax = "/empireperm user group set <player> <group>")
		public static CommandResponse userGroupSetCommand(ICommandSender sender, List<String> args) {
			if (args.size() < 2) {
				return CommandResponse.SEND_SYNTAX;
			}

			UUID uuid = getUUIDFromUsername(args.get(0));
			Group group = getGroupFromName(args.get(1));

			User user = getManager().users.get(uuid);
			if (user == null) {
				getManager().users.add(new User(uuid, group));
			} else {
				user.group = group;
			}
			getManager().saveUsers();
			ChatManager.send(sender, "Empires.notification.user.group.set");

			return CommandResponse.DONE;
		}

		@Command(name = "list", permission = "Empires.perm.cmd.user.list", parentName = "Empires.perm.cmd.user", syntax = "/empireperm user list")
		public static CommandResponse userListCommand(ICommandSender sender, List<String> args) {
			ChatComponentList root = new ChatComponentList();
			root.appendSibling(
					LocalizationManager.get("Empires.format.list.header", new ChatComponentFormatted("{9|USERS}")));

			for (User user : getManager().users) {
				root.appendSibling(
						LocalizationManager.get("Empires.format.user.long", user.lastPlayerName, user.group));
			}

			return CommandResponse.DONE;
		}

		@Command(name = "perm", permission = "Empires.perm.cmd.user.perm", parentName = "Empires.perm.cmd.user", syntax = "/empireperm user perm <command>")
		public static CommandResponse userPermCommand(ICommandSender sender, List<String> args) {
			return CommandResponse.SEND_HELP_MESSAGE;
		}

		@Command(name = "add", permission = "Empires.perm.cmd.user.perm.add", parentName = "Empires.perm.cmd.user.perm", syntax = "/empireperm user perm add <player> <perm>")
		public static CommandResponse userPermAddCommand(ICommandSender sender, List<String> args) {
			if (args.size() < 2) {
				return CommandResponse.SEND_SYNTAX;
			}

			UUID uuid = getUUIDFromUsername(args.get(0));
			User user = getManager().users.get(uuid);
			user.permsContainer.add(args.get(1));
			getManager().saveUsers();
			ChatManager.send(sender, "Empires.notification.perm.added");

			return CommandResponse.DONE;
		}

		@Command(name = "remove", permission = "Empires.perm.cmd.user.perm.remove", parentName = "Empires.perm.cmd.user.perm", syntax = "/empireperm user perm remove <player> <perm>")
		public static CommandResponse userPermRemoveCommand(ICommandSender sender, List<String> args) {
			if (args.size() < 2) {
				return CommandResponse.SEND_SYNTAX;
			}

			UUID uuid = getUUIDFromUsername(args.get(0));
			User user = getManager().users.get(uuid);
			user.permsContainer.remove(args.get(1));
			getManager().saveUsers();
			ChatManager.send(sender, "Empires.notification.perm.removed");

			return CommandResponse.DONE;
		}

		@Command(name = "list", permission = "Empires.perm.cmd.user.perm.list", parentName = "Empires.perm.cmd.user.perm", syntax = "/empireperm user perm list <player>")
		public static CommandResponse userPermListCommand(ICommandSender sender, List<String> args) {
			if (args.size() < 1) {
				return CommandResponse.SEND_SYNTAX;
			}

			UUID uuid = getUUIDFromUsername(args.get(0));
			User user = getManager().users.get(uuid);

			getManager().saveUsers();
			ChatManager.send(sender, user.permsContainer.toChatMessage());

			return CommandResponse.DONE;
		}

	private static EmpiresBridge getManager() {
		return (EmpiresBridge) PermissionProxy.getPermissionManager();
	}
}