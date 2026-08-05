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
    public static void onRegisterAdditional(ModelEvent.RegisterAdditional event) {
        event.register(ModelResourceLocation.standalone(
                ResourceLocation.fromNamespaceAndPath(SylvariaMod.MODID, "block/sylvaria_glow_mushroom_emissive")));
    }

    // Модель свечения (models/block/sylvaria_glow_mushroom_emissive.json) не привязана
    // ни к одному blockstate, поэтому ModelManager сам её не запекает. Без этой регистрации
    // getModel(...) в SylvariaGlowMushroomRenderer возвращал дефолтную "missing model"
    // (розово-чёрный куб-шахматку) — именно это и рисовалось поверх настоящего гриба.
    @SubscribeEvent
    public static void onRegisterAdditional(ModelEvent.RegisterAdditional event) {
        event.register(ResourceLocation.fromNamespaceAndPath(SylvariaMod.MODID, "block/sylvaria_glow_mushroom_emissive"));
    }
}
