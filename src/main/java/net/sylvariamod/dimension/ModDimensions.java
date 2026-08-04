package net.sylvariamod.dimension;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.sylvariamod.SylvariaMod;

public class ModDimensions {
    public static final ResourceKey<Level> SYLVARIA_LEVEL = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(SylvariaMod.MODID, "sylvaria_forest"));
}
