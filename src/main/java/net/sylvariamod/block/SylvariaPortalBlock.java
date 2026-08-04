package net.sylvariamod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.sylvariamod.dimension.ModDimensions;
import net.sylvariamod.portal.SylvariaTeleporter;

/**
 * A simple portal block - no frame detection, just place it and step in.
 * Bidirectional: walking into it in the Overworld sends you to Sylvaria,
 * walking into it in Sylvaria sends you back to the Overworld spawn.
 */
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

        Entity teleported = entity.changeDimension(destination, new SylvariaTeleporter(destPos));
        if (teleported != null) {
            teleported.setPortalCooldown();
        }
    }
}
