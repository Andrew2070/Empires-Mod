package EmpiresMod.Handlers;

import java.util.List;

import EmpiresMod.API.Commands.Command.CommandsEMP;
import EmpiresMod.Datasource.EmpiresUniverse;
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
					String chat = EnumChatFormatting.RED + rankChat + EnumChatFormatting.GOLD + empireChat + " "
							+ EnumChatFormatting.WHITE + player.getDisplayName() + ": " + event.message;
					target.addChatMessage(new ChatComponentTranslation(chat));

				} catch (CommandException e) {
					String chat2 = player.getDisplayName() + ": " + event.message;
					target.addChatMessage(new ChatComponentTranslation(chat2));
				}

			}

		}

	}

}
