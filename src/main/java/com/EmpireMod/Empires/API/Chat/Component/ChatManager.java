package com.EmpireMod.Empires.API.Chat.Component;

import java.util.List;

import com.EmpireMod.Empires.Localization.LocalizationManager;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IChatComponent;

public class ChatManager {

	/**
	 * Global method for sending localized messages
	 */
	public static void send(ICommandSender sender, String localizationKey, Object... args) {
		send(sender, LocalizationManager.get(localizationKey, args));
	}

	/**
	 * Global method for sending messages If the message sent is a
	 * ChatComponentList then only its siblings are sent, omitting the root
	 * component
	 */
	@SuppressWarnings("unchecked")
	public static void send(ICommandSender sender, IChatComponent message) {
		if (sender == null) {
			return;
		}

		if (message == null) {
			return;
		}

		if (message instanceof ChatComponentList) {
			for (IChatComponent sibling : (List<IChatComponent>) message.getSiblings()) {
				if (sibling == null) {
					continue;
				}

				addChatMessageFixed(sender, sibling);
			}
		} else {
			addChatMessageFixed(sender, message);
		}
	}

	public static void addChatMessageFixed(ICommandSender sender, IChatComponent message) {
		if (sender == null || message == null)
			return;
		if (sender instanceof EntityPlayerMP) {
			if (((EntityPlayerMP) sender).playerNetServerHandler != null) {
				sender.addChatMessage(message);
			} else {
				// TODO Find a way to re-send the message.
			}
		} else {
			sender.addChatMessage(message);
		}

	}
}