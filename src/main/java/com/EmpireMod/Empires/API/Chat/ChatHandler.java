package com.EmpireMod.Empires.API.Chat;

import java.util.List;

import org.bukkit.Bukkit;

import com.EmpireMod.Empires.API.commands.CommandsEMP;
import com.EmpireMod.Empires.Datasource.EmpiresUniverse;
import com.EmpireMod.Empires.entities.Empire.Citizen;
import com.EmpireMod.Empires.entities.Empire.Empire;
import com.EmpireMod.Empires.entities.Empire.Rank;
import com.EmpireMod.Empires.exceptions.CommandException;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.ServerChatEvent;

public class ChatHandler {
	public static final ChatHandler instance = new ChatHandler();

	@SubscribeEvent

	public void onServerChatReceivedEvent(ServerChatEvent event) {

		EntityPlayer player = (EntityPlayer) event.player;

		if (event.player != null) {

			event.setCanceled(true);

			List players = MinecraftServer.getServer().getConfigurationManager().playerEntityList;

			for (int i = 0; i < +players.size(); i++) {

				EntityPlayer target = (EntityPlayer) players.get(i);
				Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(target);
				Empire empire;
				Rank rank;

				try {
					empire = CommandsEMP.getEmpireFromCitizen(res);
					rank = CommandsEMP.getRankFromCitizen(res);
					// TODO: Abbreviate ranks to [L], [O], [M].
					// TODO: Add herochat support?
					String rankChat = "[" + rank.getChatName() + "]";
					String empireChat = "[" + empire + "]";
					String chat = EnumChatFormatting.RED + rankChat + 
							EnumChatFormatting.GOLD + empireChat + " " +
							EnumChatFormatting.WHITE + player.getDisplayName() + 
							": " + event.message;
					target.addChatMessage(new ChatComponentTranslation(chat));
				} catch (CommandException e) {
					String chat2 = player.getDisplayName() + ": " + event.message;
					target.addChatMessage(new ChatComponentTranslation(chat2));
				}

			}

		}

	}

}
