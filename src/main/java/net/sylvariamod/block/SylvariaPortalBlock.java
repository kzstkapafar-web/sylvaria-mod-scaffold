package net.sylvariamod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.sylvariamod.dimension.ModDimensions;
import net.sylvariamod.portal.SylvariaTeleporter;

public class SylvariaPortalBlock extends Block {

    public SylvariaPortalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (entity.isOnPortalCooldown()) {
            return;
        }

        boolean inSylvaria = serverLevel.dimension().equals(ModDimensions.SYLVARIA_LEVEL);
        var destinationKey = inSylvaria ? Level.OVERWORLD : ModDimensions.SYLVARIA_LEVEL;
        ServerLevel destination = serverLevel.getServer().getLevel(destinationKey);
        if (destination == null) {
            return;
        }

        BlockPos destPos = inSylvaria
                ? destination.getSharedSpawnPos()
                : new BlockPos(pos.getX(), 6, pos.getZ());

        SylvariaTeleporter.prepareLandingSpot(destination, destPos);

        DimensionTransition transition = new DimensionTransition(
                destination,
                new Vec3(destPos.getX() + 0.5, destPos.getY() + 1.0, destPos.getZ() + 0.5),
                Vec3.ZERO,
                entity.getYRot(),
                entity.getXRot(),
                false,
                DimensionTransition.DO_NOTHING
        );

        entity.changeDimension(transition);
        entity.setPortalCooldown();
    }
}
