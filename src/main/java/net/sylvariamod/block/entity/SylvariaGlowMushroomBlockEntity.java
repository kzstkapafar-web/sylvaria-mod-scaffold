package net.sylvariamod.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.sylvariamod.block.ModBlockEntities;

/**
 * Carries no real data - it exists only so SylvariaGlowMushroomRenderer has
 * a hook to attach the emissive-overlay render pass to.
 */
public class SylvariaGlowMushroomBlockEntity extends BlockEntity {

    public SylvariaGlowMushroomBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SYLVARIA_GLOW_MUSHROOM.get(), pos, state);
    }
}
