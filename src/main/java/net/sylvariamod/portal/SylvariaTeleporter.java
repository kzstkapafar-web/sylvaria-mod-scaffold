package net.sylvariamod.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.sylvariamod.block.ModBlocks;
import net.sylvariamod.block.SylvariaPortalBlock;

/**
 * Отвечает за то, ЧТО появляется на другой стороне портала.
 *
 * Правила:
 *  - если рядом с "номинальной" точкой (те же X/Z, что и у портала-источника)
 *    уже есть зарегистрированный портал - телепорт ведёт в него, новый НЕ строится;
 *  - иначе ищем свободное место по расширяющейся спирали и строим там точную
 *    копию рамки + зажжённого портала (той же ширины/высоты/оси, что построил игрок);
 *  - "свободное" означает: достаточно далеко от footprint всех уже
 *    зарегистрированных порталов (с отступом PADDING), чтобы рамки никогда
 *    не накладывались друг на друга, даже если игроки ставят порталы часто
 *    и близко друг к другу.
 */
public final class SylvariaTeleporter {

    /** В этом радиусе от номинальной точки существующий портал переиспользуется, а не дублируется. */
    private static final int REUSE_RADIUS = 32;
    /** Отступ вокруг рамки, который держим свободным от других порталов. */
    private static final int PADDING = 4;
    private static final int MAX_SEARCH_RADIUS = 256;
    private static final int RING_STEP = 12;
    private static final int POINTS_PER_RING = 16;

    private SylvariaTeleporter() {
    }

    public record Destination(BlockPos standPos, BlockPos portalAnchor) {
    }

    public static Destination resolve(ServerLevel destLevel, BlockPos sourcePos, Direction.Axis axis, int width, int height) {
        PortalRegistrySavedData registry = PortalRegistrySavedData.get(destLevel);
        BlockPos nominal = surfacePos(destLevel, sourcePos);

        BlockPos existing = findNearby(registry, nominal, REUSE_RADIUS);
        if (existing != null) {
            return new Destination(standPosFor(existing, axis, width, height), existing);
        }

        BlockPos free = spiralSearch(destLevel, registry, nominal, width, height);
        build(destLevel, free, axis, width, height);
        registry.addAnchor(free);

        return new Destination(standPosFor(free, axis, width, height), free);
    }

    private static BlockPos surfacePos(ServerLevel level, BlockPos source) {
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, source.getX(), source.getZ());
        if (y < level.getMinBuildHeight() + 1) {
            y = 64;
        }
        return new BlockPos(source.getX(), y, source.getZ());
    }

    private static BlockPos findNearby(PortalRegistrySavedData registry, BlockPos nominal, int radius) {
        BlockPos closest = null;
        long bestDist = Long.MAX_VALUE;
        long radiusSq = (long) radius * radius;
        for (BlockPos anchor : registry.anchors()) {
            long d = anchor.distSqr(nominal);
            if (d <= radiusSq && d < bestDist) {
                bestDist = d;
                closest = anchor;
            }
        }
        return closest;
    }

    private static BlockPos spiralSearch(ServerLevel level, PortalRegistrySavedData registry, BlockPos nominal, int width, int height) {
        if (isFree(registry, nominal, width, height)) {
            return nominal;
        }
        for (int radius = RING_STEP; radius <= MAX_SEARCH_RADIUS; radius += RING_STEP) {
            for (int i = 0; i < POINTS_PER_RING; i++) {
                double angle = (Math.PI * 2 * i) / POINTS_PER_RING;
                int dx = (int) Math.round(Math.cos(angle) * radius);
                int dz = (int) Math.round(Math.sin(angle) * radius);
                BlockPos candidate = surfacePos(level, nominal.offset(dx, 0, dz));
                if (isFree(registry, candidate, width, height)) {
                    return candidate;
                }
            }
        }
        // Крайний случай (сотни порталов в одном районе) - ставим значительно выше, чтобы
        // хотя бы не влезать физически в чужую рамку.
        return nominal.above(2 + registry.anchors().size() % 64);
    }

    private static boolean isFree(PortalRegistrySavedData registry, BlockPos candidate, int width, int height) {
        int span = Math.max(width, height) + PADDING * 2;
        for (BlockPos anchor : registry.anchors()) {
            if (Math.abs(anchor.getX() - candidate.getX()) < span
                    && Math.abs(anchor.getZ() - candidate.getZ()) < span) {
                return false;
            }
        }
        return true;
    }

    private static BlockPos standPosFor(BlockPos anchor, Direction.Axis axis, int width, int height) {
        Direction side = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        // Игрок появляется рядом с порталом, а не внутри него.
        return anchor.above(1).relative(side, width + 2);
    }

    /** Строит рамку width x height (по внутреннему проёму) с якорем в нижнем левом углу и сразу зажигает портал. */
    private static void build(ServerLevel level, BlockPos anchor, Direction.Axis axis, int width, int height) {
        Direction side = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;

        for (int w = -PADDING; w <= width + PADDING; w++) {
            for (int h = -1; h <= height + PADDING; h++) {
                BlockPos p = anchor.relative(side, w).above(h);
                if (h == -1) {
                    level.setBlockAndUpdate(p, Blocks.GRASS_BLOCK.defaultBlockState());
                } else if (!level.getBlockState(p).isAir()) {
                    level.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
                }
            }
        }

        BlockState frameState = ModBlocks.PORTAL_FRAME.get().defaultBlockState();
        for (int w = -1; w <= width; w++) {
            for (int h = -1; h <= height; h++) {
                boolean border = (w == -1 || w == width || h == -1 || h == height);
                if (border) {
                    level.setBlockAndUpdate(anchor.relative(side, w).above(h), frameState);
                }
            }
        }

        BlockState portalState = ModBlocks.SYLVARIA_PORTAL.get().defaultBlockState()
                .setValue(SylvariaPortalBlock.AXIS, axis);
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                level.setBlockAndUpdate(anchor.relative(side, w).above(h), portalState);
            }
        }
    }
}
