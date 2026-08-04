package net.sylvariamod.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.sylvariamod.block.ModBlocks;

public final class SylvariaTeleporter {

    private SylvariaTeleporter() {
    }

    public static void prepareLandingSpot(ServerLevel level, BlockPos pos) {
        // Real terrain is uneven now, so instead of flattening a fixed-height
        // platform we just make sure there's headroom to stand in and solid
        // ground directly underfoot, wherever the surface actually is.
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = 0; y <= 2; y++) {
                    BlockPos clear = pos.offset(x, y, z);
                    if (!level.getBlockState(clear).isAir()) {
                        level.setBlockAndUpdate(clear, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }

        BlockPos floor = pos.below();
        if (level.getBlockState(floor).isAir()) {
            level.setBlockAndUpdate(floor, Blocks.GRASS_BLOCK.defaultBlockState());
        }

        level.setBlockAndUpdate(pos, ModBlocks.SYLVARIA_PORTAL.get().defaultBlockState());
    }
}
