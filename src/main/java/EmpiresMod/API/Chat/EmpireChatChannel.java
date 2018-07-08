package EmpiresMod.API.Chat;

import java.util.ArrayList;
import java.util.List;

import EmpiresMod.API.Commands.Command.CommandsEMP;
import EmpiresMod.Datasource.EmpiresUniverse;
import EmpiresMod.Utilities.ClassUtils;
import EmpiresMod.entities.Empire.Citizen;
import EmpiresMod.entities.Empire.Empire;
import EmpiresMod.entities.Empire.Rank;
import EmpiresMod.exceptions.Command.CommandException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.eventhandler.IEventListener;

public class EmpireChatChannel {
	public static final EmpireChatChannel instance = new EmpireChatChannel();
	
	@net.minecraftforge.fml.common.eventhandler.SubscribeEvent
	public void onServerChatReceivedEvent(ServerChatEvent event) {
		EntityPlayer player = (EntityPlayer) event.player;
		Citizen citizen = EmpiresUniverse.instance.getOrMakeCitizen(player);
		Empire empire = CommandsEMP.getEmpireFromCitizen(citizen);
		if (citizen.getChannelStatus() == true) {
		if (event.player != null) {
			event.setCanceled(true);
				List targets = new ArrayList();
				List listeners = new ArrayList();
				List<Empire> allEmpires = CommandsEMP.getUniverse().empires;
				List<EntityPlayer> players = MinecraftServer.getServer().getConfigurationManager().playerEntityList;	
				for (int i=0; i< +players.size(); i++) {
					EntityPlayer selectedPlayer = players.get(i);
					listeners.add(event.getListenerList().getListeners(i));
				 }
				
				for (int l=0; l< +listeners.size(); l++) {
					EntityPlayer listener = (EntityPlayer) listeners.get(l);
					try {
					Citizen listenerCitizen = EmpiresUniverse.instance.getOrMakeCitizen(listener);
					Empire listenerEmpire = CommandsEMP.getEmpireFromCitizen(listenerCitizen);
					
						if (empire != listenerEmpire) {
							event.getListenerList().unregisterAll(l, (IEventListener) listener);
						}
					
					} catch (CommandException e) {
					event.getListenerList().unregister(l, (IEventListener) listener);
					}
				
				}
				Rank rank = CommandsEMP.getRankFromCitizen(citizen);
				String rankChat = "[" + rank.getChatName() + "]";
				String empireChat = "[" + empire + "]";
				String appliedChatMsg = EnumChatFormatting.RED + rankChat  + EnumChatFormatting.GOLD + event.message;
				String finalChatMessage = EnumChatFormatting.DARK_RED + "[E] " + appliedChatMsg;
				player.addChatMessage(new ChatComponentTranslation(finalChatMessage));
				
				
	}
	}
	}
}
