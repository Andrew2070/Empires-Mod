package EmpiresMod.Handlers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import EmpiresMod.Empires;
import EmpiresMod.API.Chat.ChatComponentPEX;
import EmpiresMod.API.Commands.Command.CommandsEMP;
import EmpiresMod.Datasource.EmpiresUniverse;
import EmpiresMod.Utilities.ClassUtils;
import EmpiresMod.entities.Empire.Citizen;
import EmpiresMod.entities.Empire.Empire;
import EmpiresMod.entities.Empire.Rank;
import EmpiresMod.exceptions.Command.CommandException;
import EmpiresMod.exceptions.Permission.PermissionException;
import cpw.mods.fml.common.BukkitPluginRef;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.ServerChatEvent;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;
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
					// TODO: Add herochat support?

					String rankChat = "[" + rank.getChatName() + "]";
					String empireChat = "[" + empire + "]";
					if (ClassUtils.isBukkitLoaded() == true) {

					if (Bukkit.getPluginManager().getPlugin("PermissionsEx").isEnabled() == true) {	
						Empires.instance.LOG.info("[Empires Mod] PermissionsEX detected.. Applying hooks");
						//get pex prefix below					
					
					Player bukkitplayer = Bukkit.getPlayer(target.getUniqueID());
					Method prefix = ChatComponentPEX.class.getDeclaredMethod("getPrefix", Player.class);
					Method suffix = ChatComponentPEX.class.getDeclaredMethod("getSuffix", Player.class);
					
					prefix.invoke(instance, Player.class);
					suffix.invoke(instance, Player.class);
					
					String pexPrefix = ChatComponentPEX.getPrefix(bukkitplayer);
					String pexSuffix = ChatComponentPEX.getSuffix(bukkitplayer);

					String chat = pexPrefix + EnumChatFormatting.RED + rankChat + EnumChatFormatting.GOLD + empireChat + " "
								+ EnumChatFormatting.WHITE + player.getDisplayName() + pexSuffix + ": " + event.message;
						//add pex prefix here ^

						target.addChatMessage(new ChatComponentTranslation(chat));
					}
					}
					String chat = EnumChatFormatting.RED + rankChat + EnumChatFormatting.GOLD + empireChat + " "
							+ EnumChatFormatting.WHITE + player.getDisplayName() + ": " + event.message;
					target.addChatMessage(new ChatComponentTranslation(chat));

				} catch (CommandException e) {
					String chat2 = player.getDisplayName() + ": " + event.message;
					target.addChatMessage(new ChatComponentTranslation(chat2));
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}

	}

}
