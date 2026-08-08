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

    // ---- Decorative glowing mushroom ----
    // Custom cap+stem 3D model (not a flat cross-plant). Light level 15 lights up the
    // surroundings; the emissive overlay (see SylvariaGlowMushroomBlockEntity/Renderer)
    // additionally keeps the glow-spot pixels bright even in shadow.
    public static final RegistryObject<Block> SYLVARIA_GLOW_MUSHROOM = BLOCKS.register("sylvaria_glow_mushroom",
            () -> new net.sylvariamod.block.SylvariaGlowMushroomBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .lightLevel(state -> 7) // тускло, как редстоун-факел
                    .pushReaction(PushReaction.DESTROY)));

    public static final RegistryObject<Item> SYLVARIA_GLOW_MUSHROOM_ITEM = ITEMS.register("sylvaria_glow_mushroom",
            () -> new BlockItem(SYLVARIA_GLOW_MUSHROOM.get(), new Item.Properties()));

    // Standalone big mushroom - just the big cap+stem from the paired model, on its own.
    // Reuses the same full-block hitbox as the paired block (big mushroom already spans it).
    public static final RegistryObject<Block> SYLVARIA_GLOW_MUSHROOM_BIG = BLOCKS.register("sylvaria_glow_mushroom_big",
            () -> new net.sylvariamod.block.SylvariaGlowMushroomBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .lightLevel(state -> 7) // тускло, как редстоун-факел
                    .pushReaction(PushReaction.DESTROY)));

    public static final RegistryObject<Item> SYLVARIA_GLOW_MUSHROOM_BIG_ITEM = ITEMS.register("sylvaria_glow_mushroom_big",
            () -> new BlockItem(SYLVARIA_GLOW_MUSHROOM_BIG.get(), new Item.Properties()));

    // Standalone small mushroom - just the small cap+stem, recentered in the block, with a
    // much shorter/narrower hitbox matching its actual size instead of a full-block box.
    public static final RegistryObject<Block> SYLVARIA_GLOW_MUSHROOM_SMALL = BLOCKS.register("sylvaria_glow_mushroom_small",
            () -> new net.sylvariamod.block.SylvariaGlowMushroomBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .lightLevel(state -> 7) // тускло, как редстоун-факел
                    .pushReaction(PushReaction.DESTROY),
                    net.minecraft.world.level.block.Block.box(4.0D, 0.0D, 4.0D, 12.0D, 7.0D, 12.0D)));

    public static final RegistryObject<Item> SYLVARIA_GLOW_MUSHROOM_SMALL_ITEM = ITEMS.register("sylvaria_glow_mushroom_small",
            () -> new BlockItem(SYLVARIA_GLOW_MUSHROOM_SMALL.get(), new Item.Properties()));

    // ---- Второй вид светящейся флоры: кластер бирюзовых игл ----
    // Переиспользует SylvariaGlowMushroomBlock из шага 1 как есть (класс уже параметризован по
    // форме хитбокса и умеет светиться/испускать искры animateTick - никакой мухомор-специфики
    // внутри него нет). Свой хитбокс под приземистый кластер игл вместо полного блока гриба.
    // Хитбокс под новую модель (веер кристаллических осколков-крестовин): footprint
    // примерно 1-15 по X/Z (после поворота диагональю достаёт почти до краёв блока),
    // высота ограничена 16 - у самого высокого осколка геометрия уходит чуть выше (до 19),
    // это чисто визуальный, некликабельный "хвостик", как у декоративных кристаллов.
    public static final RegistryObject<Block> SYLVARIA_GLOW_CRYSTAL = BLOCKS.register("sylvaria_glow_crystal",
            () -> new net.sylvariamod.block.SylvariaGlowMushroomBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN)
                    .instabreak()
                    .sound(SoundType.AMETHYST_CLUSTER)
                    .lightLevel(state -> 7) // тускло, как редстоун-факел - единый стиль со всей флорой
                    .pushReaction(PushReaction.DESTROY),
                    net.minecraft.world.level.block.Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D)));

    public static final RegistryObject<Item> SYLVARIA_GLOW_CRYSTAL_ITEM = ITEMS.register("sylvaria_glow_crystal",
            () -> new BlockItem(SYLVARIA_GLOW_CRYSTAL.get(), new Item.Properties()));
}
