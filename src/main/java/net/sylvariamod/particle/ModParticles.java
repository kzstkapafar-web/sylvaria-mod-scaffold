package net.sylvariamod.particle;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.sylvariamod.SylvariaMod;

/**
 * Лёгкая фиолетовая дымка в стиле Сильварии - фоновый амбиент по всему биому
 * (см. worldgen/biome/sylvaria_forest.json -> effects.particle) и погуще вокруг
 * светящихся грибов (см. SylvariaGlowMushroomBlock#animateTick).
 */
public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, SylvariaMod.MODID);

    public static final RegistryObject<SimpleParticleType> SYLVARIA_HAZE =
            PARTICLE_TYPES.register("sylvaria_haze", () -> new SimpleParticleType(false));
}
