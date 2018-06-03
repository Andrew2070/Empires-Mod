package EmpiresMod.Handlers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import EmpiresMod.Empires;
import EmpiresMod.API.ForgeChatHandler;
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

public class BukkitChatCompat {
	public String message = "";
	public static final BukkitChatCompat instance = new BukkitChatCompat();
	public void onBukkitServerChatReceivedEvent(AsyncPlayerChatEvent event) {
		if (ClassUtils.isBukkitLoaded() == true) {
		EntityPlayer player = (EntityPlayer) event.getPlayer();
		event.setCancelled(true);
		if (player != null) {
			event.setCancelled(true);

			List players = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
			for (int i = 0; i < +players.size(); i++) {
				EntityPlayer target = (EntityPlayer) players.get(i);
				String message = event.getMessage().toString();
				String format = event.getFormat();
				this.message = message;
		try {
			
			ClassLoader classloaderCitizen = EmpiresMod.Datasource.EmpiresUniverse.class.getClassLoader();
			ClassLoader classloaderEmpire= EmpiresMod.API.Commands.Command.CommandsEMP.class.getClassLoader();
			ClassLoader classloaderRanks= EmpiresMod.API.Commands.Command.CommandsEMP.class.getClassLoader();
			
			Method CitizenMethod = classloaderCitizen.loadClass("EmpiresMod.Datasource.EmpiresUniverse.class").getDeclaredMethod("getOrMakeCitizen", EntityPlayer.class);
			Method EmpireMethod = classloaderEmpire.loadClass("EmpiresMod.API.Command.CommandsEMP.class").getDeclaredMethod("getEmpireFromCitizen", Citizen.class);
			Method EmpireRankMethod = classloaderRanks.loadClass("EmpiresMod.API.Command.CommandsEMP.class").getDeclaredMethod("getRankFromCitizen", Citizen.class);
			
			
				Citizen citizen = (Citizen) CitizenMethod.invoke(null, target);
				Empire empire = (Empire) EmpireMethod.invoke(null, citizen);
				Rank rank = (Rank) EmpireRankMethod.invoke(null, citizen);
				String empirePrefix = "[" + empire.toString() + "]";
				String rankPrefix = "[" + rank.toString() + "]";
				
				String finalModdedChat = rankPrefix + empirePrefix;
				String finalMessage = finalModdedChat + message;
				
				String finalFormat = finalMessage + format;
				target.addChatMessage(new ChatComponentTranslation(finalMessage));
				target.addChatMessage(new ChatComponentTranslation(message));
				
				Bukkit.getLogger().log(null, finalMessage + " Original Message: " + message);
				Bukkit.getServer().getLogger().log(null, finalMessage + " Original Message: " + message);
					
		} catch (IllegalAccessException e) {
			target.addChatMessage(new ChatComponentTranslation(message));
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			target.addChatMessage(new ChatComponentTranslation(message));
			//send original message:
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			target.addChatMessage(new ChatComponentTranslation(message));
			//send original message:
			e.printStackTrace();		
		} catch (NoSuchMethodException e) {
			target.addChatMessage(new ChatComponentTranslation(message));
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			target.addChatMessage(new ChatComponentTranslation(message));
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			target.addChatMessage(new ChatComponentTranslation(message));
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace(); 
		  }
		}
		}
		
		}
		
	}
}
