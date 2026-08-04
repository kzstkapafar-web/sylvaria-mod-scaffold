package net.sylvariamod.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
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

    // Decorative frame block - build a rectangle out of it around the portal block.
    public static final RegistryObject<Block> PORTAL_FRAME = BLOCKS.register("portal_frame",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(50.0F, 1200.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Item> PORTAL_FRAME_ITEM = ITEMS.register("portal_frame",
            () -> new BlockItem(PORTAL_FRAME.get(), new Item.Properties()));

    // The actual portal - walk into it to teleport. Placed by hand (no flint & steel needed).
    public static final RegistryObject<Block> SYLVARIA_PORTAL = BLOCKS.register("sylvaria_portal",
            () -> new SylvariaPortalBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .noCollission()
                    .strength(-1.0F)
                    .lightLevel(state -> 11)
                    .sound(SoundType.GLASS)
                    .pushReaction(PushReaction.BLOCK)
                    .noLootTable()));

    public static final RegistryObject<Item> SYLVARIA_PORTAL_ITEM = ITEMS.register("sylvaria_portal",
            () -> new BlockItem(SYLVARIA_PORTAL.get(), new Item.Properties()));
}
