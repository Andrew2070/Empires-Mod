package EmpiresMod.Utilities;

import java.util.List;

import EmpiresMod.Empires;
import EmpiresMod.API.Chat.Component.ChatManager;
import EmpiresMod.API.Commands.Command.CommandsEMP;
import EmpiresMod.Configuration.Config;
import EmpiresMod.Datasource.EmpiresUniverse;
import EmpiresMod.entities.Empire.Citizen;
import EmpiresMod.entities.Empire.Empire;
import EmpiresMod.exceptions.Command.CommandException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class PvPUtils {
	
	
	public static Boolean enemyNearby(EntityPlayer localPlayer) {
		List<EntityPlayerMP> allPlayers = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
	     for(int i=0; i < allPlayers.size(); i++) {
			EntityPlayer player = allPlayers.get(i);
			
			double x1 = localPlayer.posX;
			double y1 = localPlayer.posY;
			double z1 = localPlayer.posZ;
			
			double x2 = player.posX;
			double y2 = player.posY;
			double z2 = player.posZ;
			
			double distance = Math.sqrt(Math.pow((x1-x2), 2) + Math.pow((y1-y2), 2));
			
			if (distance < 64) {
				return true;
			}
	     }
		return false;
		
	}
	
	public static void deathProcess(Entity attacker, Entity defender, float damage) {
		if (defender instanceof EntityPlayer) {
			EntityPlayer defendingPlayer = (EntityPlayer) defender;
			Citizen defendingCitizen = EmpiresUniverse.instance.getOrMakeCitizen(defender);
			if (attacker != null) {
			if (attacker instanceof EntityPlayer) { //PVP
				EntityPlayer attackingPlayer = (EntityPlayer) attacker;
				Citizen attackerCitizen = EmpiresUniverse.instance.getOrMakeCitizen(attacker);
				if (defendingPlayer.capabilities.isCreativeMode = false) {
					if (damage >= defendingPlayer.getHealth()) {
						if (defendingCitizen.getPower() < Config.instance.minPower.get()) {
							defendingCitizen.setPower(Config.instance.minPower.get());
					//Just a check + fix to make sure a player's power is not too negative. 
				}
				attackerCitizen.addPower(Config.instance.pvpPowerTransfer.get());
				
				if (Config.instance.exceedMaxPowerLimit.get() == true) {
					attackerCitizen.addMaxPower(Config.instance.pvpPowerTransfer.get());
				}
				try {
					Empire defenderEmpire = CommandsEMP.getEmpireFromCitizen(defendingCitizen);
					ChatManager.send(defendingPlayer, "Empires.notification.ciz.powerLostOnDeath", Config.instance.PowerPerDeath.get(), defendingCitizen.getPower());
					ChatManager.send(attackingPlayer, "Empires.notification.pvp.kill.success.event", defenderEmpire, defendingCitizen, Config.instance.pvpPowerTransfer.get());
					defenderEmpire.sendToSpawn(defendingCitizen);
					Empires.instance.datasource.saveCitizen(defendingCitizen);
					Empires.instance.datasource.saveCitizen(attackerCitizen);
				} catch (CommandException e) {
					ChatManager.send(defendingPlayer, "Empires.notification.ciz.powerLostOnDeath", Config.instance.PowerPerDeath.get(), defendingCitizen.getPower());
					Empires.instance.datasource.saveCitizen(defendingCitizen);
				}
			  }
			}
			}
			}
		if (defendingPlayer.capabilities.isCreativeMode = false) {
			if (damage >= defendingPlayer.getHealth()) {
				if (defendingCitizen.getPower() < Config.instance.minPower.get()) {
					defendingCitizen.setPower(Config.instance.minPower.get());
					//Just a check + fix to make sure a player's power is not too negative. 
				}
				defendingCitizen.subtractPower(Config.instance.PowerPerDeath.get());
				ChatManager.send(defendingPlayer, "Empires.notification.ciz.powerLostOnDeath", Config.instance.PowerPerDeath.get(), defendingCitizen.getPower());
				Empires.instance.datasource.saveCitizen(defendingCitizen);
				if (Config.instance.sendToEmpireSpawn.get() == true) {
				try {
					Empire defenderEmpire = CommandsEMP.getEmpireFromCitizen(defendingCitizen);
					defenderEmpire.sendToSpawn(defendingCitizen); //Send Player To Empire Spawn Point
				} catch (CommandException e) {
				//TODO Tell Player they got sent to spawn, because they don't have an Empire to teleport to.
				}
				}
			}
		}
		}
		
	}
}
