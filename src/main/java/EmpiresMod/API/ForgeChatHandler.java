package EmpiresMod.API;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import EmpiresMod.Empires;
import EmpiresMod.API.Commands.Command.CommandsEMP;
import EmpiresMod.Datasource.EmpiresUniverse;
import EmpiresMod.Utilities.ClassUtils;
import EmpiresMod.entities.Empire.Citizen;
import EmpiresMod.entities.Empire.Empire;
import EmpiresMod.entities.Empire.Rank;
import EmpiresMod.exceptions.Command.CommandException;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.ServerChatEvent;

public class ForgeChatHandler {
	
	public static final ForgeChatHandler instance = new ForgeChatHandler();
			
	@SubscribeEvent
	public void onServerChatReceivedEvent(ServerChatEvent event) {
		if (ClassUtils.isBukkitLoaded() == false) {
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
							String rankChat = "[" + rank.getChatName() + "]";
							String empireChat = "[" + empire + "]";
							String appliedChatMsg = "EnumChatFormatting.RED + rankChat + EnumChatFormatting.GOLD + empireChat";
				
							String chat =  EnumChatFormatting.RED + rankChat + EnumChatFormatting.GOLD + empireChat + " "
								+ EnumChatFormatting.WHITE + player.getDisplayName() + ": " + event.message;
							target.addChatMessage(new ChatComponentTranslation(chat));
				
					} catch (CommandException e) {
					String chat = player.getDisplayName() + ": " + event.message;
					target.addChatMessage(new ChatComponentTranslation(chat));
				}
			}
			}
		}

	}

}

