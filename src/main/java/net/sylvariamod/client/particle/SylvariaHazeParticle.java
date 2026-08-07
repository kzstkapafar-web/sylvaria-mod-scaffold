package net.sylvariamod.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

/**
 * Мягкое полупрозрачное фиолетовое облачко - медленно дрейфует вверх и в стороны,
 * плавно появляется и исчезает (без резких вспышек/обрывов). Используется и как фоновый
 * амбиент по всему биому (biome effects.particle), и как более частый эффект вокруг
 * светящихся грибов (SylvariaGlowMushroomBlock#animateTick).
 */
public class SylvariaHazeParticle extends TextureSheetParticle {

    // Приглушённый фиолетовый, тот же тон, что и у эмиссивного свечения грибов -
    // единый стиль для всех "магических" эффектов мода.
    private static final float BASE_R = 0.66F;
    private static final float BASE_G = 0.45F;
    private static final float BASE_B = 0.92F;
    private static final float MAX_ALPHA = 0.35F;

    private final SpriteSet spriteSet;

    private final float targetAlpha;

    protected SylvariaHazeParticle(ClientLevel level, double x, double y, double z,
                                    double dx, double dy, double dz, SpriteSet sprites) {
        super(level, x, y, z, dx, dy, dz);
        this.spriteSet = sprites;
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.friction = 1.0F;

        // Медленный дрейф: чуть вверх, лёгкое случайное блуждание в стороны.
        this.xd = dx + (this.random.nextDouble() - 0.5D) * 0.01D;
        this.yd = dy + 0.006D + this.random.nextDouble() * 0.008D;
        this.zd = dz + (this.random.nextDouble() - 0.5D) * 0.01D;

        this.quadSize = 0.35F + this.random.nextFloat() * 0.5F;
        this.lifetime = 80 + this.random.nextInt(60);

        float variance = 0.85F + this.random.nextFloat() * 0.15F;
        this.rCol = BASE_R * variance;
        this.gCol = BASE_G * variance;
        this.bCol = BASE_B * variance;
        this.targetAlpha = MAX_ALPHA * (0.6F + this.random.nextFloat() * 0.4F);
        this.alpha = 0.0F;

        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            this.setSpriteFromAge(this.spriteSet);
            float lifeRatio = (float) this.age / (float) this.lifetime;
            // Плавно нарастаем первые 25% жизни, держим пик, плавно гаснем последние 40%.
            if (lifeRatio < 0.25F) {
                this.alpha = Mth.lerp(lifeRatio / 0.25F, 0.0F, targetAlpha);
            } else if (lifeRatio > 0.6F) {
                this.alpha = Mth.lerp((lifeRatio - 0.6F) / 0.4F, targetAlpha, 0.0F);
            } else {
                this.alpha = targetAlpha;
            }
        }
    }

    @Override
    public float getQuadSize(float partialTicks) {
        // Лёгкое разрастание со временем - ощущение расползающейся дымки, а не жёсткой точки.
        float lifeRatio = ((float) this.age + partialTicks) / (float) this.lifetime;
        return this.quadSize * (0.7F + lifeRatio * 0.5F);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                        double x, double y, double z,
                                        double dx, double dy, double dz) {
            return new SylvariaHazeParticle(level, x, y, z, dx, dy, dz, sprites);
        }
    }
}
