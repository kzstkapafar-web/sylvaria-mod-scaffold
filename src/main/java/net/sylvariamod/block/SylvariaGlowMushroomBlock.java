package net.sylvariamod.block;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.sylvariamod.block.entity.SylvariaGlowMushroomBlockEntity;
import javax.annotation.Nullable;
/**
 * Custom glowing mushroom - cap+stem 3D model (see models/block/sylvaria_glow_mushroom.json),
 * emits light itself AND carries a BlockEntity purely so SylvariaGlowMushroomRenderer can draw
 * an always-full-bright emissive overlay on top of the normal model (see that class for details).
 *
 * Placement rules mirror vanilla mushrooms: dirt, podzol, mycelium or moss.
 */
public class SylvariaGlowMushroomBlock extends BaseEntityBlock {
    // Bounding box roughly matching the PAIRED model's overall extent - big mushroom is
    // full block height (8 stem + 8 domed cap), small one sits well below/beside it.
    // Used as the default shape when no explicit shape is passed (paired + big-only variant).
    protected static final VoxelShape DEFAULT_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 16.0D, 16.0D, 16.0D);

    // Required since 1.21: every BaseEntityBlock subclass must expose a codec
    // so the block can be (de)serialized. simpleCodec() just needs the constructor.
    public static final MapCodec<SylvariaGlowMushroomBlock> CODEC = simpleCodec(SylvariaGlowMushroomBlock::new);

    // Per-instance shape, so the same class can serve the paired mushroom, the standalone
    // big mushroom (both use DEFAULT_SHAPE) and the standalone small mushroom (much shorter/
    // narrower box, passed in explicitly - see ModBlocks.SYLVARIA_GLOW_MUSHROOM_SMALL).
    protected final VoxelShape shape;

    public SylvariaGlowMushroomBlock(BlockBehaviour.Properties properties) {
        this(properties, DEFAULT_SHAPE);
    }

    public SylvariaGlowMushroomBlock(BlockBehaviour.Properties properties, VoxelShape shape) {
        super(properties);
        this.shape = shape;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Раньше тут была Shapes.empty() - игрок физически проходил сквозь весь гриб
        // насквозь и оказывался камерой внутри сплошной геометрии купола (отсюда
        // "гигантские цветные плашки на весь экран" на скриншотах - это не дефект
        // текстуры, а обычный вид ЛЮБОЙ грани в упор, когда камера в неё влезла).
        // Настоящая коллизия по форме модели не даёт игроку туда попасть.
        return shape;
    }
    @Override
    public RenderShape getRenderShape(BlockState state) {
        // MODEL = the normal baked model still renders; the BlockEntityRenderer
        // additionally draws the emissive overlay on top of it.
        return RenderShape.MODEL;
    }
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.is(BlockTags.DIRT)
                || below.is(Blocks.PODZOL)
                || below.is(Blocks.MYCELIUM)
                || below.is(Blocks.MOSS_BLOCK)
                || below.is(Blocks.GRASS_BLOCK);
    }
    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SylvariaGlowMushroomBlockEntity(pos, state);
    }
    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }
}
