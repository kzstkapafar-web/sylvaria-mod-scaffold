package net.sylvariamod.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.sylvariamod.SylvariaMod;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, SylvariaMod.MODID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, SylvariaMod.MODID);

    // Decorative frame block - build a rectangle out of it, then light it with flint & steel.
    // Strength matches obsidian (50 / 1200) - see needs_diamond_tool tag.
    public static final RegistryObject<Block> PORTAL_FRAME = BLOCKS.register("portal_frame",
            () -> new net.sylvariamod.block.SylvariaFrameBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(50.0F, 1200.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Item> PORTAL_FRAME_ITEM = ITEMS.register("portal_frame",
            () -> new BlockItem(PORTAL_FRAME.get(), new Item.Properties()));

    // The actual portal - walk into it to teleport. Normally formed by igniting a frame with
    // flint & steel (see SylvariaFrameBlock / SylvariaPortalShape); can also be /give'n directly
    // for testing, in which case it falls back to a default 2x3 shape with no real frame.
    public static final RegistryObject<Block> SYLVARIA_PORTAL = BLOCKS.register("sylvaria_portal",
            () -> new net.sylvariamod.block.SylvariaPortalBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .noCollission()
                    .noOcclusion()
                    .strength(-1.0F)
                    .lightLevel(state -> 11)
                    .sound(SoundType.GLASS)
                    .pushReaction(PushReaction.BLOCK)
                    .noLootTable()));

    public static final RegistryObject<Item> SYLVARIA_PORTAL_ITEM = ITEMS.register("sylvaria_portal",
            () -> new BlockItem(SYLVARIA_PORTAL.get(), new Item.Properties()));

    // ---- Magical forest tree ----

    // Log - violet bark, behaves like a normal log (axis-rotated pillar).
    // Uses SylvariaLogBlock so right-clicking with an axe strips it into SYLVARIA_STRIPPED_LOG.
    public static final RegistryObject<Block> SYLVARIA_LOG = BLOCKS.register("sylvaria_log",
            () -> new SylvariaLogBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava()));

    public static final RegistryObject<Item> SYLVARIA_LOG_ITEM = ITEMS.register("sylvaria_log",
            () -> new BlockItem(SYLVARIA_LOG.get(), new Item.Properties()));

    // Stripped log - result of using an axe on SYLVARIA_LOG.
    public static final RegistryObject<Block> SYLVARIA_STRIPPED_LOG = BLOCKS.register("sylvaria_stripped_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava()));

    public static final RegistryObject<Item> SYLVARIA_STRIPPED_LOG_ITEM = ITEMS.register("sylvaria_stripped_log",
            () -> new BlockItem(SYLVARIA_STRIPPED_LOG.get(), new Item.Properties()));

    // Planks - crafted from sylvaria_log (1 log -> 4 planks).
    public static final RegistryObject<Block> SYLVARIA_PLANKS = BLOCKS.register("sylvaria_planks",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava()));

    public static final RegistryObject<Item> SYLVARIA_PLANKS_ITEM = ITEMS.register("sylvaria_planks",
            () -> new BlockItem(SYLVARIA_PLANKS.get(), new Item.Properties()));

    // Leaves - softly glowing violet leaves (light level 6, like a dim natural light source).
    public static final RegistryObject<Block> SYLVARIA_LEAVES = BLOCKS.register("sylvaria_leaves",
            () -> new LeavesBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(0.2F)
                    .randomTicks()
                    .sound(SoundType.GRASS)
                    .noOcclusion()
                    .isValidSpawn((state, level, pos, type) -> false)
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)
                    .ignitedByLava()
                    .pushReaction(PushReaction.DESTROY)
                    .lightLevel(state -> 6)));

    public static final RegistryObject<Item> SYLVARIA_LEAVES_ITEM = ITEMS.register("sylvaria_leaves",
            () -> new BlockItem(SYLVARIA_LEAVES.get(), new Item.Properties()));
}
