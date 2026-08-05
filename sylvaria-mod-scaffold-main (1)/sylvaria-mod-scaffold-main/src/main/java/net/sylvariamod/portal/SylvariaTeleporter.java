package net.sylvariamod.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.sylvariamod.block.ModBlocks;

public final class SylvariaTeleporter {

    private SylvariaTeleporter() {
    }

    public static void prepareLandingSpot(ServerLevel level, BlockPos pos) {
        // Пол 3x3
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos floor = pos.offset(x, -1, z);
                level.setBlockAndUpdate(floor, Blocks.GRASS_BLOCK.defaultBlockState());
                for (int y = 0; y <= 2; y++) {
                    BlockPos clear = pos.offset(x, y, z);
                    if (!level.getBlockState(clear).isAir()) {
                        level.setBlockAndUpdate(clear, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }

        // Портал сбоку, не в точке спавна игрока
        BlockPos portalPos = pos.offset(0, 0, 0);
        level.setBlockAndUpdate(portalPos, ModBlocks.SYLVARIA_PORTAL.get().defaultBlockState());

        // Место для игрока (+2 по X) — воздух и пол
        BlockPos stand = pos.offset(2, 0, 0);
        level.setBlockAndUpdate(stand.below(), Blocks.GRASS_BLOCK.defaultBlockState());
        level.setBlockAndUpdate(stand, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(stand.above(), Blocks.AIR.defaultBlockState());
    }
}
