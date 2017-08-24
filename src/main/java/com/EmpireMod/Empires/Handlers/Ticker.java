package com.EmpireMod.Empires.Handlers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.bukkit.event.entity.PlayerDeathEvent;

import com.EmpireMod.Empires.Empires;
import com.EmpireMod.Empires.API.Chat.Component.ChatManager;
import com.EmpireMod.Empires.API.Commands.Command.CommandsEMP;
import com.EmpireMod.Empires.Configuration.Config;
import com.EmpireMod.Empires.Datasource.EmpiresDatasource;
import com.EmpireMod.Empires.Datasource.EmpiresUniverse;
import com.EmpireMod.Empires.entities.Empire.AdminEmpire;
import com.EmpireMod.Empires.entities.Empire.Citizen;
import com.EmpireMod.Empires.entities.Empire.Empire;
import com.EmpireMod.Empires.exceptions.Command.CommandException;
import com.EmpireMod.Empires.API.Commands.Command.CommandsEMP;
import com.sk89q.worldedit.entity.Player;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;

public class Ticker {

    public static final Ticker instance = new Ticker();
    private boolean ticked = true;
    private int lastCalendarDay = -1;
    @SubscribeEvent
    public void onTickEvent(TickEvent.WorldTickEvent ev) {
        if(ev.side == Side.CLIENT)
            return;

        for(Citizen res : EmpiresUniverse.instance.citizens) {
            res.tick();
        }

        
        if((Config.instance.costEmpireUpkeep.get() > 0 || Config.instance.costAdditionalUpkeep.get() > 0) && ev.phase == TickEvent.Phase.START) {
            if (ticked) {
                if(lastCalendarDay != -1 && Calendar.getInstance().get(Calendar.DAY_OF_YEAR) != lastCalendarDay) {
                    for (int i = 0; i < EmpiresUniverse.instance.empires.size(); i++) {
                        Empire empire = EmpiresUniverse.instance.empires.get(i);
                        if (!(empire instanceof AdminEmpire)) {
                            empire.bank.payUpkeep();
                            if(empire.bank.getDaysNotPaid() == Config.instance.upkeepEmpireDeletionDays.get() && Config.instance.upkeepEmpireDeletionDays.get() > 0) {
                                Empires.instance.LOG.info("Empire {} has been deleted because it didn't pay upkeep for {} days.", empire.getName(), Config.instance.upkeepEmpireDeletionDays.get());
                                getDatasource().deleteEmpire(empire);
                            } else {
                                getDatasource().saveEmpireBank(empire.bank);
                            }
                        }
                    }
                    ticked = false;
                }
                lastCalendarDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
            } else {
                ticked = true;
            }
        }
      
			//Get A List of Entities:
		    List<EntityPlayerMP> allPlayers = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
		     for(int i=0; i < allPlayers.size(); i++) {
					 

				//Cycle Through List of Entities and choose one:
				EntityPlayer player = allPlayers.get(i);
				//Check To See if this probable Player Entity Exists:
				if (player != null) {		
					//Check To See if Entity Player Is A Player:
					if (player instanceof EntityPlayer) {
	
							//Check To See if Player is Alive:
							if (player.isDead) continue;
							
							//Check To See if Player Has A Citizen Profile, If Not Then Make One:	
							String playerName =  player.getDisplayName();
							Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(playerName); 

							Citizen citizen = res;
							
		
						     
						    if (res.getPower() > res.getMaxPower()) {
									double newMaxPower = res.getPower();
									res.setMaxPower(newMaxPower);
								}

						    long FinishTime = System.currentTimeMillis();
							if (FinishTime - res.getLastPowerUpdateTime()  >= Config.instance.PowerUpdateTime.get() ) { 
								if (res.getPower() < res.getMaxPower()) {
									
							//Calculate New Power For This Selected Player:

							double powerPerHour = Config.instance.PowerPerHour.get();
							double powerUpdateTime = Config.instance.PowerUpdateTime.get();
							double newPower = res.getPower() + powerPerHour;
							
							try {

							Empire empire = CommandsEMP.getEmpireFromCitizen(res);
							
							if (empire.getPower() < empire.getMaxPower())
								
							empire.addPower(newPower);
							
							return;
							
							} catch (CommandException e) {

							
					
							res.setPower(newPower);		
							res.resetTime(FinishTime);
		
							Empires.instance.datasource.saveCitizen(res);
							
							
							
							
							if (res.getPower() < Config.instance.minPower.get()) {
								
								res.setPower(Config.instance.minPower.get());
							}
							
							return;
							}

								}
							
						}
	
					}	
				}
       }  
		     
    }     	
		     	
    
   @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerEvent.PlayerRespawnEvent ev) {
		if(ev.player.worldObj.isRemote || ev.isCanceled()) 
			return;
 
    	Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(ev.player);
    	
    	if (res != null) {

    		res.subtractPower(Config.instance.PowerPerDeath.get());
    	    ChatManager.send(ev.player, "Empires.notification.ciz.powerLostOnDeath", Config.instance.PowerPerDeath.get(), res.getPower());

    	}
    	
    
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent ev) {
        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(ev.player);
        if (res != null) {
            res.setPlayer(ev.player);
         //  Empires.instance.datasource.saveCitizen(res);
            
            
            
        } else {
            Empires.instance.LOG.error("[Player Login] Didn't create citizen for player {} ({})", ev.player.getCommandSenderName(), ev.player.getPersistentID());
       
        }
    }
   

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent ev) {
        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(ev.player);
        if (res != null) {
            res.setPlayer(ev.player);
            Empires.instance.datasource.saveCitizen(res);
        }
    }

    @SubscribeEvent
    public void onPlayerBreaksBlock(BlockEvent.BreakEvent ev) {
        if (VisualsHandler.instance.isBlockMarked(ev.x, ev.y, ev.z, ev.world.provider.dimensionId, (EntityPlayerMP) ev.getPlayer())) {
            // Cancel event if it's a border that has been broken
            ev.setCanceled(true);
        }
    }

    private EmpiresDatasource getDatasource() {
        return Empires.instance.datasource;
    }
}
