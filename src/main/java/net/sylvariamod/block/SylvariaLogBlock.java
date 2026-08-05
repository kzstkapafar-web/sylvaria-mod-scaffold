package net.sylvariamod.block;

import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import javax.annotation.Nullable;

/**
 * A log block that can be "stripped" by right-clicking it with an axe,
 * turning it into ModBlocks.SYLVARIA_STRIPPED_LOG (preserving its axis).
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
}
