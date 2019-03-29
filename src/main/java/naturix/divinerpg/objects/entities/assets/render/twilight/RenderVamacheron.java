package naturix.divinerpg.objects.entities.assets.render.twilight;

import javax.annotation.Nullable;

import naturix.divinerpg.objects.entities.assets.model.twilight.model.ModelVamacheron;
import naturix.divinerpg.objects.entities.entity.twilight.boss.Vamecheron;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderVamacheron extends RenderLiving<Vamecheron> {
	
	public static final IRenderFactory FACTORY = new Factory();
	ResourceLocation texture = new ResourceLocation("divinerpg:textures/entity/vamecheron.png");
	private final ModelVamacheron ModelVamacheron;
    
	public RenderVamacheron(RenderManager rendermanagerIn, ModelBase modelbaseIn, float shadowsizeIn) {
        super(rendermanagerIn, new ModelVamacheron(), 1F);
        ModelVamacheron = (ModelVamacheron) super.mainModel;

    }


	@Nullable
    @Override
    protected ResourceLocation getEntityTexture(Vamecheron entity) {
        return texture;
    }

	 public static class Factory implements IRenderFactory<Vamecheron> {

	        @Override
	        public Render<? super Vamecheron> createRenderFor(RenderManager manager) {
	            return new RenderVamacheron(manager, new ModelVamacheron(), 0.5F);
	        }
	    }

	}