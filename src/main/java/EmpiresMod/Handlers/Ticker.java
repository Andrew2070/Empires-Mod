package EmpiresMod.Handlers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import EmpiresMod.Empires;
import EmpiresMod.API.Chat.Component.ChatManager;
import EmpiresMod.API.Commands.Command.CommandsEMP;
import EmpiresMod.Configuration.Config;
import EmpiresMod.Datasource.EmpiresDatasource;
import EmpiresMod.Datasource.EmpiresUniverse;
import EmpiresMod.Utilities.PlayerUtils;
import EmpiresMod.entities.Empire.AdminEmpire;
import EmpiresMod.entities.Empire.Citizen;
import EmpiresMod.entities.Empire.Empire;
import EmpiresMod.entities.Empire.Rank;
import EmpiresMod.entities.Empire.Relationship;
import EmpiresMod.entities.Empire.Relationship.Type;
import EmpiresMod.exceptions.Command.CommandException;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
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
							//Essentially a bunch of mathematical equations from here on:
							PlayerUtils.recalculatePower(res);
						}
	
					}	
				
				}
     
    }
    


// 			Incorporated into PvPUtils and ProtectionHandler  
//    
// @SubscribeEvent(priority = EventPriority.HIGHEST)
//    public void onPlayerDeath(PlayerEvent.PlayerRespawnEvent ev) {
//		if(ev.player.worldObj.isRemote || ev.isCanceled()) {
//			return;
//		}
//	 
//    		Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(ev.player);
//    		
//    		if (Config.instance.sendToEmpireSpawn.get() == true) {
//        	Empire empire = CommandsEMP.getEmpireFromCitizen(res);
//    		empire.sendToSpawn(res);
//    		}
//
//    		res.subtractPower(Config.instance.PowerPerDeath.get());
//    		Empires.instance.datasource.saveCitizen(res);
//    	    ChatManager.send(ev.player, "Empires.notification.ciz.powerLostOnDeath", Config.instance.PowerPerDeath.get(), res.getPower());
//    	     //Weird bug subtracts power twice.
//    	    	try {
//    	    	Empire empire = CommandsEMP.getEmpireFromCitizen(res);
//    	    	empire.subtractPower(Config.instance.PowerPerDeath.get());	
//    	    	}catch (CommandException e) {
//
//    	    	}
//    	    	
//    	    	if (res.getPower() < Config.instance.minPower.get()) {
//    	    		res.setPower(Config.instance.minPower.get()); //Just a check + fix to make sure a player's power is not too negative. 
//    	    	}
//    	    	
//    	    	return;
//
//    }
  
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
