package net.sylvariamod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.sylvariamod.dimension.ModDimensions;
import net.sylvariamod.portal.SylvariaPortalShape;
import net.sylvariamod.portal.SylvariaTeleporter;
import org.joml.Vector3f;

public class SylvariaPortalBlock extends Block {

    // Только горизонтальные оси - портал стоит "стеной", как портал в Нижний мир.
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    // Цвета частиц взяты из палитры биома sylvaria_forest: мшисто-зелёный -> фиолетовое свечение.
    private static final Vector3f COLOR_MOSS = new Vector3f(0.31f, 0.78f, 0.36f);
    private static final Vector3f COLOR_GLOW = new Vector3f(0.56f, 0.36f, 0.85f);

    public SylvariaPortalBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(8) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS,
                    0.25F, 0.9F + random.nextFloat() * 0.2F, false);
        }
        double x = pos.getX() + random.nextDouble();
        double y = pos.getY() + random.nextDouble();
        double z = pos.getZ() + random.nextDouble();
        level.addParticle(new DustColorTransitionOptions(COLOR_MOSS, COLOR_GLOW, 1.2F), x, y, z, 0.0, 0.0, 0.0);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (entity.isOnPortalCooldown() || !entity.canChangeDimensions(serverLevel, serverLevel)) {
            return;
        }

        boolean inSylvaria = serverLevel.dimension().equals(ModDimensions.SYLVARIA_LEVEL);
        var destinationKey = inSylvaria ? Level.OVERWORLD : ModDimensions.SYLVARIA_LEVEL;
        ServerLevel destination = serverLevel.getServer().getLevel(destinationKey);
        if (destination == null) {
            return;
        }

        // Кулдаун СРАЗУ, до телепорта - иначе цикл каждый тик.
        entity.setPortalCooldown();

        // Если портал был честно зажжён в рамке - используем её реальный размер/ось,
        // иначе (например, блок выдан командой без рамки) - разумные значения по умолчанию.
        SylvariaPortalShape.Shape shape = SylvariaPortalShape.measureIgnited(serverLevel, pos);
        Direction.Axis axis = shape != null ? shape.axis() : state.getValue(AXIS);
        int width = shape != null ? shape.width() : 2;
        int height = shape != null ? shape.height() : 3;

        SylvariaTeleporter.Destination dest = SylvariaTeleporter.resolve(destination, pos, axis, width, height);

        DimensionTransition transition = new DimensionTransition(
                destination,
                new Vec3(dest.standPos().getX() + 0.5, dest.standPos().getY(), dest.standPos().getZ() + 0.5),
                Vec3.ZERO,
                entity.getYRot(),
                entity.getXRot(),
                false,
                DimensionTransition.DO_NOTHING
        );

        entity.changeDimension(transition);
    }
}
