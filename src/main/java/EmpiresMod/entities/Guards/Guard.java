package EmpiresMod.entities.Guards;
import EmpiresMod.Constants;
import EmpiresMod.entities.Empire.Empire;
import EmpiresMod.entities.Empire.EmpireBlock;
import net.minecraft.block.Block;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.command.IEntitySelector;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import EmpiresMod.entities.Guards.RenderGuard;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveThroughVillage;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import EmpiresMod.entities.Guards.API.EntityAIDefendEmpire;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;

public class Guard extends EntityCreature implements EntityAIDefendEmpire.IEmpireGuard {
private EntityAIAttackOnCollide aiAttackOnCollide = new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.2D, false);
   private int homeCheckTimer;
   Empire empire;


   public Guard(World world) {
      super(world);
      this.getNavigator().setAvoidsWater(true);
      this.getNavigator().setBreakDoors(true);
      super.tasks.addTask(1, new EntityAISwimming(this));
      this.tasks.addTask(2, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
      this.tasks.addTask(4, new EntityAIAttackOnCollide(this, EntityMob.class, 1.0D, true));
      super.tasks.addTask(6, new EntityAIMoveThroughVillage(this, 0.6D, true));
      super.tasks.addTask(7, new EntityAIMoveTowardsRestriction(this, 1.0D));
      super.tasks.addTask(8, new EntityAIRestrictOpenDoor(this));
      super.tasks.addTask(9, new EntityAIOpenDoor(this, true));
      super.tasks.addTask(10, new EntityAIWander(this, 1.0D));
      super.tasks.addTask(11, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
      super.tasks.addTask(12, new EntityAILookIdle(this));
      super.targetTasks.addTask(1, new EntityAIDefendEmpire(this));
      super.targetTasks.addTask(5, new EntityAIHurtByTarget(this, true));
      super.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
      super.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityVillager.class, 0, false));
      if(world != null && !world.isRemote) {
         this.setCombatTask();
      }

      super.experienceValue = 5;
   }

   public boolean canAttackClass(Class p_70686_1_) {
      return EntityCreeper.class != p_70686_1_ && this.getClass() != p_70686_1_;
   }

   public Empire getEmpire() {
      return this.empire;
   }

   public EntityCreature getCreature() {
      return this;
   }

   protected void updateAITick() {
      if(--this.homeCheckTimer <= 0) {
         this.homeCheckTimer = 70 + super.rand.nextInt(50);
    //get empire from coordinates     this.empire = (MathHelper.floor_double(), MathHelper.floor_double(super.posY), MathHelper.floor_double(super.posZ), 32);
         
         if(this.empire == null) {
            this.detachHome();
         } else { 
            //float chunkcoordinates = this.empire.getSpawn().getX() + this.empire.getSpawn().getY() + this.empire.getSpawn().getZ();
            		int x = (int) this.empire.getSpawn().getX();
            		int y = (int) this.empire.getSpawn().getY();
            		int z = (int) this.empire.getSpawn().getZ();
            		
            		
            /*/	for (EmpireBlock block : this.empire.empireBlocksContainer.values()) {
            		int X = block.getX();
            		int Z = block.getZ();
            		if (X > MAX_X >> MAX_X == X)
            	}
            /*/ //Using random radius until i fix this		
            	//radius of empire = how many claims left or right it goes
            		
            this.setHomeArea(x, y, z, (int) 1.5);/*/(int)((float)this.villageObj.getVillageRadius() * 1.5F)/*/
            if(this.getAttackTarget() == null) {
               this.heal(1.0F);
            }
         }
      }

      super.updateAITick();
   }

   protected String getSwimSound() {
      return "game.hostile.swim";
   }

   protected String getSplashSound() {
      return "game.hostile.swim.splash";
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage);
      this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(40.0D);
      this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.5D);
      this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(40.0D);
      this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(4.0D);
   }

   protected void entityInit() {
      super.entityInit();
      super.dataWatcher.addObject(13, new Byte((byte)0));
      super.dataWatcher.addObject(14, new Integer(500));
   }

   public boolean isAIEnabled() {
      return true;
   }

   protected String getLivingSound() {
      return "mob.villager.idle";
   }

   protected String getHurtSound() {
      return "mob.villager.hit";
   }

   protected String getDeathSound() {
      return "mob.villager.death";
   }

   protected float getSoundPitch() {
      return 0.8F;
   }

   public void onLivingUpdate() {
      this.updateArmSwingProgress();
      float f = this.getBrightness(1.0F);
      if(f > 0.5F) {
         super.entityAge += 2;
      }

      super.onLivingUpdate();
   }

   public boolean attackEntityAsMob(Entity p_70652_1_) {
      float f = (float)this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
      int i = 0;
      if(p_70652_1_ instanceof EntityLivingBase) {
         f += EnchantmentHelper.getEnchantmentModifierLiving(this, (EntityLivingBase)p_70652_1_);
         i += EnchantmentHelper.getKnockbackModifier(this, (EntityLivingBase)p_70652_1_);
      }

      boolean flag = p_70652_1_.attackEntityFrom(DamageSource.causeMobDamage(this), f);
      if(flag) {
         if(i > 0) {
            p_70652_1_.addVelocity((double)(-MathHelper.sin(super.rotationYaw * 3.1415927F / 180.0F) * (float)i * 0.5F), 0.1D, (double)(MathHelper.cos(super.rotationYaw * 3.1415927F / 180.0F) * (float)i * 0.5F));
            super.motionX *= 0.6D;
            super.motionZ *= 0.6D;
         }

         int j = EnchantmentHelper.getFireAspectModifier(this);
         if(j > 0) {
            p_70652_1_.setFire(j * 4);
         }

         if(p_70652_1_ instanceof EntityLivingBase) {
            EnchantmentHelper.func_151384_a((EntityLivingBase)p_70652_1_, this);
         }

         EnchantmentHelper.func_151385_b(this, p_70652_1_);
      }

      return flag;
   }

   protected void attackEntity(Entity p_70785_1_, float p_70785_2_) {
      if(super.attackTime <= 0 && p_70785_2_ < 2.0F && p_70785_1_.boundingBox.maxY > super.boundingBox.minY && p_70785_1_.boundingBox.minY < super.boundingBox.maxY) {
         super.attackTime = 20;
         this.attackEntityAsMob(p_70785_1_);
      }

   }

   protected void func_145780_a(int p_145780_1_, int p_145780_2_, int p_145780_3_, Block p_145780_4_) {}

   public void updateRidden() {
      super.updateRidden();
      if(super.ridingEntity instanceof EntityCreature) {
         EntityCreature entitycreature = (EntityCreature)super.ridingEntity;
         super.renderYawOffset = entitycreature.renderYawOffset;
      }

   }

   protected String func_146067_o(int p_146067_1_) {
      return p_146067_1_ > 4?"game.hostile.hurt.fall.big":"game.hostile.hurt.fall.small";
   }

   public void onDeath(DamageSource p_70645_1_) {
      if(super.attackingPlayer != null && this.empire != null) {
         //what to do when guard dies:
      }

      super.onDeath(p_70645_1_);
   }

   protected Item getDropItem() {
      return Items.arrow;
   }

   protected void dropRareDrop(int p_70600_1_) {
      this.entityDropItem(new ItemStack(Items.leather_chestplate, 1), 0.0F);
   }

   protected void addArmor() {
      this.setCurrentItemOrArmor(0, new ItemStack(Items.diamond_sword));
      this.setCurrentItemOrArmor(1, new ItemStack(Items.diamond_boots));
      this.setCurrentItemOrArmor(2, new ItemStack(Items.diamond_leggings));
      this.setCurrentItemOrArmor(3, new ItemStack(Items.diamond_chestplate));
      this.setCurrentItemOrArmor(4, new ItemStack(Items.diamond_helmet));
   }

   public String getCommandSenderName() {
      return this.hasCustomNameTag()?this.getCustomNameTag():StatCollector.translateToLocal("entity.empiresmod.guard");
   }

   public IEntityLivingData onSpawnWithEgg(IEntityLivingData p_110161_1_) {
      p_110161_1_ = super.onSpawnWithEgg(p_110161_1_);
      this.addRandomArmor();
      return p_110161_1_;
   }

   public void setCombatTask() {
      super.tasks.removeTask(this.aiAttackOnCollide);
      ItemStack itemstack = this.getHeldItem();
         super.tasks.addTask(4, this.aiAttackOnCollide);
      }

   public int getGuardType() {
      return super.dataWatcher.getWatchableObjectByte(13);
   }

   public void setGuardType(int p_82201_1_) {
      super.dataWatcher.updateObject(13, Byte.valueOf((byte)p_82201_1_));
      super.isImmuneToFire = p_82201_1_ == 1;
      if(p_82201_1_ == 1) {
         this.setSize(0.72F, 2.34F);
      } else {
         this.setSize(0.6F, 1.8F);
      }

   }
   public void setCurrentItemOrArmor(int p_70062_1_, ItemStack p_70062_2_) {
      super.setCurrentItemOrArmor(p_70062_1_, p_70062_2_);
      if(!super.worldObj.isRemote && p_70062_1_ == 0) {
         this.setCombatTask();
      }

   }
   public double getYOffset() {
      return super.getYOffset() - 0.5D;
   }
   

   public static void createFrom(EntityPlayer player, Empire empire) {
      World world = player.worldObj;
      Guard entity = new Guard(world);
      entity.func_110163_bv();
      entity.copyLocationAndAnglesFrom(player);
      entity.empire = empire;
      entity.onSpawnWithEgg((IEntityLivingData)null);
      entity.posX = player.posX;
      entity.posY = player.posY;
      entity.posZ = player.posZ;
      world.spawnEntityInWorld(entity);
      EntityRegistry.registerModEntity(Guard.class, "Guard", entity.getEntityId(), Constants.MODNAME, 10, 10, false);
      //RenderingRegistry.registerEntityRenderingHandler(Guard.class,
    		 //RenderGuard(new ModelZombie(), 0.5F));
   }

}