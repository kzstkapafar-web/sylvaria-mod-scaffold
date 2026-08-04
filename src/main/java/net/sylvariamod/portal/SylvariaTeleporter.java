package net.sylvariamod.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;
import net.sylvariamod.block.ModBlocks;

import java.util.function.Function;

/**
 * Places the entity at a fixed destination position, making sure there's
 * solid ground to stand on and a portal block waiting there so the player
 * can travel back.
 */
public class SylvariaTeleporter implements ITeleporter {

    private final BlockPos destination;

    public SylvariaTeleporter(BlockPos destination) {
        this.destination = destination;
    }

    @Override
    public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
        ensureLandingSpot(destWorld, destination);
        Vec3 pos = new Vec3(destination.getX() + 0.5, destination.getY() + 1.0, destination.getZ() + 0.5);
        return new PortalInfo(pos, Vec3.ZERO, entity.getYRot(), entity.getXRot());
    }

    private void ensureLandingSpot(ServerLevel level, BlockPos pos) {
        // Flatten a small solid platform beneath the arrival point.
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos floor = pos.offset(x, -1, z);
                if (level.getBlockState(floor).isAir()) {
                    level.setBlockAndUpdate(floor, Blocks.GRASS_BLOCK.defaultBlockState());
                }
            }
        }
        // Make sure the arrival point itself is a portal block, so the
        // player (or anyone else) can step on it to travel back.
        level.setBlockAndUpdate(pos, ModBlocks.SYLVARIA_PORTAL.get().defaultBlockState());
    }
}
