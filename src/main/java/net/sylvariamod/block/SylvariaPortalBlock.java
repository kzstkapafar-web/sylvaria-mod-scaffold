package net.sylvariamod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
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
        // Уже на кулдауне или не может менять измерение
        if (entity.isOnPortalCooldown() || !entity.canChangeDimensions(serverLevel, serverLevel)) {
            return;
        }

        boolean inSylvaria = serverLevel.dimension().equals(ModDimensions.SYLVARIA_LEVEL);
        var destinationKey = inSylvaria ? Level.OVERWORLD : ModDimensions.SYLVARIA_LEVEL;
        ServerLevel destination = serverLevel.getServer().getLevel(destinationKey);
        if (destination == null) {
            return;
        }

        // Кулдаун СРАЗУ, до телепорта — иначе цикл каждый тик
        entity.setPortalCooldown();

        BlockPos destPos;
        if (inSylvaria) {
            destPos = destination.getSharedSpawnPos();
        } else {
            int surfaceY = destination.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
            if (surfaceY < destination.getMinBuildHeight() + 1) {
                surfaceY = 64;
            }
            destPos = new BlockPos(pos.getX(), surfaceY, pos.getZ());
        }

        // Площадка и обратный портал РЯДОМ, не под ногами
        SylvariaTeleporter.prepareLandingSpot(destination, destPos);

        // Игрок встаёт на безопасную клетку РЯДОМ с порталом, не внутрь
        BlockPos standPos = destPos.offset(2, 0, 0);
        DimensionTransition transition = new DimensionTransition(
                destination,
                new Vec3(standPos.getX() + 0.5, destPos.getY() + 1.0, standPos.getZ() + 0.5),
                Vec3.ZERO,
                entity.getYRot(),
                entity.getXRot(),
                false,
                DimensionTransition.DO_NOTHING
        );

        entity.changeDimension(transition);
    }
}
