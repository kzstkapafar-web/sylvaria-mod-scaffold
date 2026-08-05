package net.sylvariamod.event;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.sylvariamod.SylvariaMod;
import net.sylvariamod.block.ModBlockEntities;
import net.sylvariamod.block.ModBlocks;
import net.sylvariamod.client.renderer.SylvariaGlowMushroomRenderer;

@Mod.EventBusSubscriber(modid = SylvariaMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SYLVARIA_LEAVES.get(), RenderType.cutoutMipped());
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.SYLVARIA_GLOW_MUSHROOM.get(),
                SylvariaGlowMushroomRenderer::new);
    }

    // The emissive overlay model (models/block/sylvaria_glow_mushroom_emissive.json) is not
    // referenced by any blockstate, so the ModelManager never bakes it on its own. Without this
    // registration, SylvariaGlowMushroomRenderer's getModel(...) lookup silently falls back to
    // Minecraft's built-in "missing model" (the pink/black checkerboard cube), which is what was
    // being drawn on top of the real, correctly-textured mushroom model.
    @SubscribeEvent
    public static void onRegisterAdditional(ModelEvent.RegisterAdditional event) {
        event.register(ModelResourceLocation.standalone(
                ResourceLocation.fromNamespaceAndPath(SylvariaMod.MODID, "block/sylvaria_glow_mushroom_emissive")));
    }
}
