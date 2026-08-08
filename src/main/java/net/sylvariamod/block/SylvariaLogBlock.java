package net.sylvariamod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.sylvariamod.particle.ModParticles;

import javax.annotation.Nullable;

/**
 * A log block that can be "stripped" by right-clicking it with an axe,
 * turning it into ModBlocks.SYLVARIA_STRIPPED_LOG (preserving its axis).
 *
 * Also emits magic spore particles around the WHOLE tree (trunk + canopy volume), but only
 * for genuine, still-growing trees - not for logs the player chopped down and placed back
 * as decoration. The distinction uses the existing vanilla LeavesBlock#PERSISTENT flag:
 * world-generated leaves are always placed with PERSISTENT=false (that's what lets them
 * decay without a nearby log), while ANY leaves block placed by a player - via BlockItem,
 * regardless of how it's arranged - is automatically marked PERSISTENT=true by vanilla
 * placement logic. So even if a player rebuilds an exact-looking tree from harvested logs
 * and leaves, none of those leaves will be PERSISTENT=false, and no spores will appear.
 */
public class SylvariaLogBlock extends RotatedPillarBlock {

    public SylvariaLogBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate) {
        if (toolAction == ToolActions.AXE_STRIP) {
            return ModBlocks.SYLVARIA_STRIPPED_LOG.get()
                    .defaultBlockState()
                    .setValue(AXIS, state.getValue(AXIS));
        }
        return super.getToolModifiedState(state, context, toolAction, simulate);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!hasNaturalCanopyNearby(level, pos)) return;

        // Спавним пачкой (3-5 штук) за один вызов вместо одной, чтобы вокруг дерева
        // ощущалось реальное облако спор, а не редкие одиночные точки.
        int count = 3 + random.nextInt(3);
        for (int i = 0; i < count; i++) {
            // Точка берётся в объёме, примерно повторяющем силуэт дерева (от ствола до кроны
            // над ним) - споры кружат вокруг всего дерева, а не сыпятся с одного листа.
            double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 3.0D;
            double y = pos.getY() + random.nextDouble() * 7.0D;
            double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 3.0D;

            BlockPos target = BlockPos.containing(x, y, z);
            if (!level.getBlockState(target).isAir()) continue;

            level.addParticle(ModParticles.SYLVARIA_HAZE.get(), x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }

    // Ищем поблизости хотя бы один блок нашей листвы с PERSISTENT=false (значит натуральный,
    // выросший вместе с деревом, а не поставленный игроком вручную) - только тогда это дерево
    // считается "живым" источником спор.
    private static boolean hasNaturalCanopyNearby(Level level, BlockPos pos) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = 0; dy <= 6; dy++) {
                    BlockPos p = pos.offset(dx, dy, dz);
                    BlockState s = level.getBlockState(p);
                    if (s.is(ModBlocks.SYLVARIA_LEAVES.get())
                            && s.hasProperty(LeavesBlock.PERSISTENT)
                            && !s.getValue(LeavesBlock.PERSISTENT)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
