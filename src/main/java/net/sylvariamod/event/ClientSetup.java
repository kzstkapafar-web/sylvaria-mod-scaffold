package net.sylvariamod.event;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
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
    // sylvaria_glow_mushroom.json's north/south/east/west (and stem "down") faces don't sample
    // real texture pixels at all anymore - they point at a small pure-white swatch padded into
    // the texture (cols 16-19) and rely on a "tintindex" to get their actual color from HERE,
    // in Java, instead of from the atlas. That sidesteps the whole class of bug this block kept
    // running into: those faces are tiny UV rects stretched across large, steeply-angled quads,
    // and at that stretch ratio even a fraction-of-a-texel sampling error at a mip level or atlas
    // edge could land on a neighboring sprite's pixel (or the next texel over) and show up as a
    // stray colored line/patch. A vertex-tinted white source can't bleed into "wrong" content -
    // white multiplied by anything just gives that color back exactly, every time, at any angle
    // or distance. This is the same underlying engine feature vanilla uses for biome-tinted grass
    // and leaves (BlockColor + "tintindex" in the model) - we're just supplying fixed constants
    // instead of a biome lookup.
    private static final int TINT_BIG_CAP    = 0x4E0F78; // (78,15,120)  - big cap rim
    private static final int TINT_BIG_STEM   = 0xAA78D2; // (170,120,210) - big stem
    private static final int TINT_SMALL_CAP  = 0x7823AF; // (120,35,175) - small cap rim
    private static final int TINT_SMALL_STEM = 0x8C5FB9; // (140,95,185) - small stem

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> switch (tintIndex) {
            case 0 -> TINT_BIG_CAP;
            case 1 -> TINT_BIG_STEM;
            case 2 -> TINT_SMALL_CAP;
            case 3 -> TINT_SMALL_STEM;
            default -> 0xFFFFFF;
        }, ModBlocks.SYLVARIA_GLOW_MUSHROOM.get());
    }

    // Block tint (above) only covers the in-world block render. The held/inventory/dropped-item
    // render goes through a separate ItemColor lookup even though it's the same model - without
    // this, the mushroom would show its real cap pattern in-world but plain white sides in hand.
    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> switch (tintIndex) {
            case 0 -> TINT_BIG_CAP;
            case 1 -> TINT_BIG_STEM;
            case 2 -> TINT_SMALL_CAP;
            case 3 -> TINT_SMALL_STEM;
            default -> 0xFFFFFF;
        }, ModBlocks.SYLVARIA_GLOW_MUSHROOM_ITEM.get());
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SYLVARIA_LEAVES.get(), RenderType.cutoutMipped());
            // Glow mushroom uses many small elements with tiny UV rects stretched over large,
            // steeply-angled faces. The default solid() layer samples the MIPPED block sheet,
            // which produces visible shimmer/banding on those faces as the camera moves.
            // cutout() uses the unmipped block sheet - same crisp look regardless of angle.
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SYLVARIA_GLOW_MUSHROOM.get(), RenderType.cutout());
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
        event.register(new ModelResourceLocation(
                ResourceLocation.fromNamespaceAndPath(SylvariaMod.MODID, "block/sylvaria_glow_mushroom_emissive"),
                "standalone"));
    }
}
