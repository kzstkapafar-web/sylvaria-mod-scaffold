package net.sylvariamod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.sylvariamod.block.entity.SylvariaGlowMushroomBlockEntity;

/**
 * Renders sylvaria_glow_mushroom_emissive.json (same geometry as the main model, but pointing
 * at a texture that is transparent everywhere except the glow-spot pixels) on top of the normal
 * block model, using a forced full-bright lightmap. That's what keeps the glow spots bright
 * even in shadow/darkness - the same underlying trick vanilla uses for things like mob eyes.
 */
public class SylvariaGlowMushroomRenderer implements BlockEntityRenderer<SylvariaGlowMushroomBlockEntity> {

    private static final ResourceLocation EMISSIVE_MODEL_LOC =
            ResourceLocation.fromNamespaceAndPath("sylvaria", "block/sylvaria_glow_mushroom_emissive");

    public SylvariaGlowMushroomRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SylvariaGlowMushroomBlockEntity be, float partialTick, PoseStack poseStack,
                        MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        BlockState state = be.getBlockState();
        BakedModel emissiveModel = Minecraft.getInstance().getModelManager()
                .getModel(new ModelResourceLocation(EMISSIVE_MODEL_LOC, "standalone"));

        if (emissiveModel == null) {
            return;
        }

        int fullBright = LightTexture.pack(15, 15);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.translucent());

        poseStack.pushPose();
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(),
                vertexConsumer,
                state,
                emissiveModel,
                1.0F, 1.0F, 1.0F,
                fullBright,
                OverlayTexture.NO_OVERLAY
        );
        poseStack.popPose();
    }
}
