package net.sylvariamod.block;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.sylvariamod.SylvariaMod;
import net.sylvariamod.block.entity.SylvariaGlowMushroomBlockEntity;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, SylvariaMod.MODID);

    // Один и тот же BlockEntityType обслуживает все три варианта гриба (пара/большой/маленький) -
    // сама BlockEntity ничего не хранит, это просто хук для рендера эмиссивного оверлея,
    // так что дублировать тип под каждый блок не нужно.
    public static final RegistryObject<BlockEntityType<SylvariaGlowMushroomBlockEntity>> SYLVARIA_GLOW_MUSHROOM =
            BLOCK_ENTITIES.register("sylvaria_glow_mushroom", () -> BlockEntityType.Builder.of(
                    SylvariaGlowMushroomBlockEntity::new,
                    ModBlocks.SYLVARIA_GLOW_MUSHROOM.get(),
                    ModBlocks.SYLVARIA_GLOW_MUSHROOM_BIG.get(),
                    ModBlocks.SYLVARIA_GLOW_MUSHROOM_SMALL.get()
            ).build(null));
}
