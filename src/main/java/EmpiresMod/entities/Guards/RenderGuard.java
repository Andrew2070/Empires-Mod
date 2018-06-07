package EmpiresMod.entities.Guards;

import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderGuard extends RenderBiped {

	   private static final ResourceLocation TEXTURE_URL = new ResourceLocation("minecraft", "textures/entity/zombie/zombie.png");


	   public RenderGuard() {
	      super(new ModelZombie(), 0.5F);
	   }

	   public ResourceLocation getEntityTexture(Entity entity) {
	      return this.func_110832_a((Guard)entity);
	   }

	   public ResourceLocation func_110832_a(Guard entity) {
	      return TEXTURE_URL;
	   }

	}