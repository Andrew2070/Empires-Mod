package EmpiresMod.Utilities;

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

public class PvPUtils {

	public static void deathProcess(Entity attacker, Entity defender, float damage) {
		if (defender instanceof EntityPlayer) {
			EntityPlayer defendingPlayer = (EntityPlayer) defender;
			Citizen defendingCitizen = EmpiresUniverse.instance.getOrMakeCitizen(defender);
			if (attacker != null) {
			if (attacker instanceof EntityPlayer) { //PVP
				EntityPlayer attackingPlayer = (EntityPlayer) attacker;
				Citizen attackerCitizen = EmpiresUniverse.instance.getOrMakeCitizen(attacker);
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
