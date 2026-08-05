package net.sylvariamod.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.sylvariamod.block.ModBlocks;
import net.sylvariamod.block.SylvariaPortalBlock;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * Вся геометрия портала Sylvaria: поиск валидной рамки, её зажигание,
 * замер уже зажжённого портала и "тушение" при поломке рамки.
 *
 * Рамка - сплошной прямоугольный периметр из sylvaria:portal_frame,
 * проём внутри - воздух. Внутренний проём: от 2x2 до 19x19 (итоговая
 * рамка снаружи - до 21x21), ориентация по оси X или Z, как у портала
 * в Нижний мир.
 */
public final class SylvariaPortalShape {

    public static final int MIN_INTERIOR = 2;
    public static final int MAX_SIZE = 21;

    private SylvariaPortalShape() {
    }

    /** bottomLeft - блок РАМКИ (не воздуха) в нижнем левом углу периметра. */
    public record Shape(BlockPos bottomLeft, Direction.Axis axis, int width, int height) {
    }

    // ---------------------------------------------------------------- поджиг

    /** Клик кремнём и огнивом по блоку рамки. Возвращает форму, если поджиг удался. */
    @Nullable
    public static Shape tryIgnite(Level level, BlockPos framePos) {
        for (Direction.Axis axis : new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}) {
            Shape shape = scan(level, framePos, axis);
            if (shape != null) {
                fill(level, shape);
                return shape;
            }
        }
        return null;
    }

    private static Shape scan(Level level, BlockPos clicked, Direction.Axis axis) {
        if (!isFrame(level, clicked)) {
            return null;
        }
        Direction side = sideOf(axis);

        // 1. Самый левый столбец рамки на этой высоте
        BlockPos left = clicked;
        int guard = 0;
        while (isFrame(level, left.relative(side.getOpposite())) && guard++ < MAX_SIZE) {
            left = left.relative(side.getOpposite());
        }

        // 2. Нижний левый угол рамки
        BlockPos corner = left;
        guard = 0;
        while (isFrame(level, corner.below()) && guard++ < MAX_SIZE) {
            corner = corner.below();
        }

        // 3. Ширина внутреннего проёма
        BlockPos innerBottomLeft = corner.above().relative(side);
        int width = 0;
        while (width <= MAX_SIZE - 2 && level.getBlockState(innerBottomLeft.relative(side, width)).isAir()) {
            width++;
        }
        if (width < MIN_INTERIOR || width > MAX_SIZE - 2) {
            return null;
        }
        if (!isFrame(level, innerBottomLeft.relative(side, width))) {
            return null;
        }

        // 4. Высота внутреннего проёма
        int height = 0;
        while (height <= MAX_SIZE - 2 && level.getBlockState(innerBottomLeft.above(height)).isAir()) {
            height++;
        }
        if (height < MIN_INTERIOR || height > MAX_SIZE - 2) {
            return null;
        }
        if (!isFrame(level, innerBottomLeft.above(height))) {
            return null;
        }

        // 5. Полная проверка периметра и того, что внутри только воздух
        for (int w = -1; w <= width; w++) {
            for (int h = -1; h <= height; h++) {
                boolean border = (w == -1 || w == width || h == -1 || h == height);
                BlockPos p = innerBottomLeft.relative(side, w).above(h);
                if (border) {
                    if (!isFrame(level, p)) {
                        return null;
                    }
                } else if (!level.getBlockState(p).isAir()) {
                    return null;
                }
            }
        }

        return new Shape(corner, axis, width, height);
    }

    private static void fill(Level level, Shape shape) {
        Direction side = sideOf(shape.axis());
        BlockPos innerBottomLeft = shape.bottomLeft().above().relative(side);
        BlockState portalState = ModBlocks.SYLVARIA_PORTAL.get().defaultBlockState()
                .setValue(SylvariaPortalBlock.AXIS, shape.axis());

        for (int w = 0; w < shape.width(); w++) {
            for (int h = 0; h < shape.height(); h++) {
                level.setBlockAndUpdate(innerBottomLeft.relative(side, w).above(h), portalState);
            }
        }
        level.playSound(null, shape.bottomLeft(), SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    // ------------------------------------------------------------- измерение

    /** Замеряет уже существующий зажжённый портал, начиная с любого его блока. */
    @Nullable
    public static Shape measureIgnited(Level level, BlockPos portalPos) {
        BlockState state = level.getBlockState(portalPos);
        if (!(state.getBlock() instanceof SylvariaPortalBlock)) {
            return null;
        }
        Direction.Axis axis = state.getValue(SylvariaPortalBlock.AXIS);
        Direction side = sideOf(axis);

        BlockPos left = portalPos;
        int guard = 0;
        while (isPortal(level, left.relative(side.getOpposite()), axis) && guard++ < MAX_SIZE) {
            left = left.relative(side.getOpposite());
        }
        BlockPos bottom = left;
        guard = 0;
        while (isPortal(level, bottom.below(), axis) && guard++ < MAX_SIZE) {
            bottom = bottom.below();
        }

        int width = 1;
        while (isPortal(level, bottom.relative(side, width), axis) && width < MAX_SIZE) {
            width++;
        }
        int height = 1;
        while (isPortal(level, bottom.above(height), axis) && height < MAX_SIZE) {
            height++;
        }

        // bottomLeft здесь - это блок рамки под/слева от портала, а не сам портал
        BlockPos frameCorner = bottom.below().relative(side.getOpposite());
        return new Shape(frameCorner, axis, width, height);
    }

    // ------------------------------------------------------------- тушение

    /**
     * Вызывается для соседа сломанной рамки. Если это блок портала - находит
     * весь связанный "пузырь" таких блоков и, если после поломки периметр
     * рамки где-то нарушен, гасит весь пузырь (заменяет на воздух).
     */
    public static void clearIfBroken(Level level, BlockPos start) {
        BlockState startState = level.getBlockState(start);
        if (!(startState.getBlock() instanceof SylvariaPortalBlock)) {
            return;
        }
        Direction.Axis axis = startState.getValue(SylvariaPortalBlock.AXIS);
        Direction side = sideOf(axis);

        Set<BlockPos> blob = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start.immutable());

        Direction[] withinPortal = {Direction.UP, Direction.DOWN, side, side.getOpposite()};

        while (!queue.isEmpty()) {
            BlockPos p = queue.poll();
            if (!blob.add(p)) {
                continue;
            }
            if (blob.size() > MAX_SIZE * MAX_SIZE) {
                break; // защита от аномально большого/испорченного портала
            }
            for (Direction dir : withinPortal) {
                BlockPos n = p.relative(dir);
                if (!blob.contains(n) && isPortal(level, n, axis)) {
                    queue.add(n);
                }
            }
        }

        boolean valid = true;
        outer:
        for (BlockPos p : blob) {
            for (Direction dir : Direction.values()) {
                BlockPos n = p.relative(dir);
                if (blob.contains(n)) {
                    continue;
                }
                boolean shouldBeFrame = dir == Direction.UP || dir == Direction.DOWN || dir == side || dir == side.getOpposite();
                if (shouldBeFrame && !isFrame(level, n)) {
                    valid = false;
                    break outer;
                }
            }
        }

        if (!valid) {
            for (BlockPos p : blob) {
                level.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
            }
        }
    }

    // --------------------------------------------------------------- утилиты

    private static Direction sideOf(Direction.Axis axis) {
        return axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
    }

    private static boolean isFrame(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(ModBlocks.PORTAL_FRAME.get());
    }

    private static boolean isPortal(Level level, BlockPos pos, Direction.Axis axis) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof SylvariaPortalBlock && state.getValue(SylvariaPortalBlock.AXIS) == axis;
    }
}
