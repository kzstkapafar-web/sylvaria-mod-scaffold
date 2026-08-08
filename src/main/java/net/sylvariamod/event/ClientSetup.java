package net.sylvariamod.event;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.sylvariamod.client.particle.SylvariaHazeParticle;
import net.sylvariamod.particle.ModParticles;
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

    // ---- Glow mushroom side-wall colors ----
    // Only the STEMS (big_stem / small_stem) still use the vertex-tint trick: their
    // north/south/east/west/down faces point at a single pure-white pixel (uv ~15.3-15.7,
    // corner of the texture) and get their real color from HERE via "tintindex", instead of
    // sampling the atlas directly. That sidesteps a stretch-sampling bug on those tiny,
    // steeply-angled quads - a vertex-tinted white source can't bleed into "wrong" content,
    // since white multiplied by anything just gives that color back exactly, at any angle.
    //
    // The CAP rings (big_cap_0..7, small_cap_neck/belly/top) no longer use tintindex at all.
    // Previously every ring shared the same tintindex (0 for big cap, 2 for small cap), so the
    // whole stepped dome was painted a single flat color - the ridges of the model were visible
    // but the color never changed between them ("layered cake" look). Each ring's side faces now
    // point at its OWN small solid-color swatch baked directly into the texture (rows 9-15,
    // previously-unused padding), forming a real 8-step light-to-dark gradient for the big cap
    // and a 3-step one for the small cap, so the dome reads as one continuously shaded surface.
    private static final int TINT_BIG_STEM   = 0xAA78D2; // (170,120,210) - big stem
    private static final int TINT_SMALL_STEM = 0x8C5FB9; // (140,95,185) - small stem

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> switch (tintIndex) {
            case 1 -> TINT_BIG_STEM;
            case 3 -> TINT_SMALL_STEM;
            default -> 0xFFFFFF;
        }, ModBlocks.SYLVARIA_GLOW_MUSHROOM.get(),
           ModBlocks.SYLVARIA_GLOW_MUSHROOM_BIG.get(),
           ModBlocks.SYLVARIA_GLOW_MUSHROOM_SMALL.get());
    }

    // Block tint (above) only covers the in-world block render. The held/inventory/dropped-item
    // render goes through a separate ItemColor lookup even though it's the same model - without
    // this, the mushroom's stems would show their real color in-world but plain white in hand.
    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> switch (tintIndex) {
            case 1 -> TINT_BIG_STEM;
            case 3 -> TINT_SMALL_STEM;
            default -> 0xFFFFFF;
        }, ModBlocks.SYLVARIA_GLOW_MUSHROOM_ITEM.get(),
           ModBlocks.SYLVARIA_GLOW_MUSHROOM_BIG_ITEM.get(),
           ModBlocks.SYLVARIA_GLOW_MUSHROOM_SMALL_ITEM.get());
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Явная регистрация вместо @Mod.EventBusSubscriber - см. подробный комментарий
        // в самом SylvariaHazeSpawner про то, почему автообнаружение оказалось ненадёжным.
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(
                net.sylvariamod.client.particle.SylvariaHazeSpawner.class);

        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SYLVARIA_LEAVES.get(), RenderType.cutoutMipped());
            // Glow mushroom uses many small elements with tiny UV rects stretched over large,
            // steeply-angled faces. The default solid() layer samples the MIPPED block sheet,
            // which produces visible shimmer/banding on those faces as the camera moves.
            // cutout() uses the unmipped block sheet - same crisp look regardless of angle.
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SYLVARIA_GLOW_MUSHROOM.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SYLVARIA_GLOW_MUSHROOM_BIG.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SYLVARIA_GLOW_MUSHROOM_SMALL.get(), RenderType.cutout());
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.SYLVARIA_GLOW_MUSHROOM.get(),
                SylvariaGlowMushroomRenderer::new);
    }

    // Регистрируем фабрику частицы дымки (см. SylvariaHazeParticle) - без этого
    // ParticleEngine не знает, каким Java-классом рендерить sylvaria:sylvaria_haze,
    // и частица просто не появится, даже если её кто-то спавнит.
    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.SYLVARIA_HAZE.get(), SylvariaHazeParticle.Provider::new);
    }

    // The emissive overlay model (models/block/sylvaria_glow_mushroom_emissive.json) is not
    // referenced by any blockstate, so the ModelManager never bakes it on its own. Without this
    // registration, SylvariaGlowMushroomRenderer's getModel(...) lookup silently falls back to
    // Minecraft's built-in "missing model" (the pink/black checkerboard cube), which is what was
    // being drawn on top of the real, correctly-textured mushroom model.
    @SubscribeEvent
    public static void onRegisterAdditional(ModelEvent.RegisterAdditional event) {
        for (String path : new String[] {
                "block/sylvaria_glow_mushroom_emissive",
                "block/sylvaria_glow_mushroom_big_emissive",
                "block/sylvaria_glow_mushroom_small_emissive"
        }) {
            event.register(new ModelResourceLocation(
                    ResourceLocation.fromNamespaceAndPath(SylvariaMod.MODID, path),
                    "standalone"));
        }
    }
}
