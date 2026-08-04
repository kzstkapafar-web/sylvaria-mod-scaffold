package net.sylvariamod.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.sylvariamod.block.ModBlocks;

public final class SylvariaTeleporter {

    private SylvariaTeleporter() {
    }

    public static void prepareLandingSpot(ServerLevel level, BlockPos pos) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos floor = pos.offset(x, -1, z);
                if (level.getBlockState(floor).isAir()) {
                    level.setBlockAndUpdate(floor, Blocks.GRASS_BLOCK.defaultBlockState());
                }
            }
        }
        level.setBlockAndUpdate(pos, ModBlocks.SYLVARIA_PORTAL.get().defaultBlockState());
    }
}
