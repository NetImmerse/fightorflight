package me.rufia.fightorflight.client.render.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import me.rufia.fightorflight.CobblemonFightOrFlight;
import me.rufia.fightorflight.client.model.FoFStoneModel;
import me.rufia.fightorflight.projectile.FoFStoneProjectile;
import me.rufia.fightorflight.init.FoFEntityRenderers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FoFStoneRenderer extends EntityRenderer<FoFStoneProjectile> {
    public static final ResourceLocation STONE_TEXTURE_LOCATION = new ResourceLocation(CobblemonFightOrFlight.MODID + ":textures/entity/fofstone.png");
    private final FoFStoneModel model;
    private final float scale;
    private float rotation0 = 0F;

    public FoFStoneRenderer(EntityRendererProvider.Context context){
        super(context);
        this.model = new FoFStoneModel(context.bakeLayer(FoFEntityRenderers.FOF_STONE_LAYER));
        this.scale = 2.0F;
    }

    public void render(FoFStoneProjectile entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, rotation0,rotation0+3F)));
        this.rotation0 += 3F;
        System.out.println("degrees:"+Mth.lerp(partialTicks, rotation0,rotation0+3F));
        System.out.println("partialTicks:"+(partialTicks));
        poseStack.scale(this.scale, this.scale, this.scale);
        VertexConsumer vertexConsumer = ItemRenderer.getFoilBufferDirect(buffer, this.model.renderType(this.getTextureLocation(entity)), false, false);
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }



    public ResourceLocation getTextureLocation(FoFStoneProjectile entity) {
        return STONE_TEXTURE_LOCATION;
    }

}