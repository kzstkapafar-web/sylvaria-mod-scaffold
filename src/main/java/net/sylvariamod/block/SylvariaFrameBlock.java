package net.sylvariamod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.sylvariamod.portal.SylvariaPortalShape;

/**
 * Декоративная рамка портала. Сама по себе ничего не делает, но если в неё
 * (в прямоугольную рамку из таких блоков) встроен пустой проём и по любому
 * блоку рамки кликнуть кремнём и огнивом - проём зажигается блоками
 * sylvaria:sylvaria_portal (см. SylvariaPortalShape).
 *
 * Если рамку разрушить, соседний зажжённый портал гаснет (превращается в воздух) -
 * см. onRemove.
 */
public class SylvariaFrameBlock extends Block {

    public SylvariaFrameBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {

        if (!(stack.getItem() instanceof FlintAndSteelItem)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (level.isClientSide()) {
            return ItemInteractionResult.sidedSuccess(true);
        }

        SylvariaPortalShape.Shape shape = SylvariaPortalShape.tryIgnite(level, pos);

        return shape != null
                ? ItemInteractionResult.sidedSuccess(false)
                : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos,
                            BlockState newState, boolean movedByPiston) {

        if (!state.is(newState.getBlock())) {
            for (Direction dir : Direction.values()) {
                SylvariaPortalShape.clearIfBroken(level, pos.relative(dir));
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
