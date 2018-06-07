package EmpiresMod.entities.Guards.API;

import java.util.List;

import EmpiresMod.Handlers.Ticker;
import EmpiresMod.entities.Empire.Empire;
import EmpiresMod.protection.ProtectionHandlers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.village.Village;

public class EntityAIDefendEmpire extends EntityAITarget {
	   EntityAIDefendEmpire.IEmpireGuard defender;
	   EntityLivingBase EmpireAgressorTarget;
	   
	public EntityAIDefendEmpire(EntityAIDefendEmpire.IEmpireGuard guard) {
	      super(guard.getCreature(), false, true);
	      this.defender = guard;
	      this.setMutexBits(1);
	}
	
	
	public Entity nearestPlayer(Double x, Double y, Double z) {
		List<EntityPlayerMP> allPlayers = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
		for (int i=1; i < allPlayers.size(); i++) {
			EntityPlayer player = allPlayers.get(i);
			
			double guardX = x;
			double guardY = y;
			double guardZ = z;
			
			double posX = player.posX;
			double posY = player.posY;
			double posZ = player.posZ;
			
			double distance = Math.sqrt(Math.pow((posX-guardX),2) + Math.pow((posY-guardY),2) + Math.pow((posZ-guardZ),2));
			
			if (distance <= 100) {
				return player;
			}
	}
		return null;
	}

	@Override
	public boolean shouldExecute() {
		Empire empire = this.defender.getEmpire();
		if (empire == null) {
		return false;
		} else {
			this.EmpireAgressorTarget = (EntityLivingBase) nearestPlayer(defender.getCreature().posX, defender.getCreature().posY, defender.getCreature().posZ);
			 if(!this.isSuitableTarget(this.EmpireAgressorTarget, false)) {
				 if(super.taskOwner.getRNG().nextInt(20) == 0) {
		               return this.isSuitableTarget(this.EmpireAgressorTarget, false);
				 } else {
		               return false;
		            }
		         } else {
		            return true;
		         }
		      }
		   }

	public void startExecuting() {
	      this.defender.getCreature().setAttackTarget(this.EmpireAgressorTarget);
	      super.startExecuting();
	   }
	
	public interface IEmpireGuard {
	      Empire getEmpire();
	      EntityCreature getCreature();
	   }
	}

