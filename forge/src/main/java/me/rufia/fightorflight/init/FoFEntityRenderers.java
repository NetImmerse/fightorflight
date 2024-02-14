package me.rufia.fightorflight.init;

import com.google.common.collect.ImmutableMap;
import me.rufia.fightorflight.CobblemonFightOrFlight;
import me.rufia.fightorflight.CobblemonFightOrFlightForge;
import me.rufia.fightorflight.client.model.FoFStoneModel;
import me.rufia.fightorflight.client.render.renderers.entity.FoFStoneRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.api.distmarker.Dist;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class FoFEntityRenderers {
	public static ModelLayerLocation FOF_STONE_LAYER = new ModelLayerLocation(new ResourceLocation(CobblemonFightOrFlight.MODID, "fof_stone"), "fof_stone_layer");
	//add model to register

	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(FoFEntityTypes.FOF_STONE_PROJECTILE.get(), FoFStoneRenderer::new);
	}
	@SubscribeEvent
	public static void registerModelLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
		LayerDefinition FoFStoneMesh = FoFStoneModel.createLayer();
		ImmutableMap.Builder<ModelLayerLocation, LayerDefinition> builder = ImmutableMap.builder();
		event.registerLayerDefinition(FOF_STONE_LAYER,  FoFStoneModel::createLayer);
	}

}
