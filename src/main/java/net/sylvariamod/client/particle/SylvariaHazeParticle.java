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
 * Стелющийся магический туман - держится низко у земли (спавнится спавнером прямо
 * на поверхности, см. SylvariaHazeSpawner), почти не поднимается вверх, только медленно
 * ползёт вбок и слегка покачивается. Насыщенный фиолетовый в цвет листвы/травы биома
 * (grass_color #7A4BC4), а не блёклый белёсый - и не круглый "пузырь", а вытянутое рваное
 * облако (см. текстуры sylvaria_haze_0..3.png). Используется и как фоновый эффект по земле
 * биома (SylvariaHazeSpawner), и как более частый эффект вокруг светящихся грибов
 * (SylvariaGlowMushroomBlock#animateTick).
 */
public class SylvariaHazeParticle extends TextureSheetParticle {

    // Насыщенный фиолетовый в тон листвы/травы биома (#7A4BC4), а не блёклый белёсый.
    private static final float BASE_R = 0.62F;
    private static final float BASE_G = 0.34F;
    private static final float BASE_B = 0.88F;
    private static final float MAX_ALPHA = 0.75F;

    private final SpriteSet spriteSet;

    private final float targetAlpha;
    private final double swayPhase;
    private final double swaySpeed;
    private final double swayAmount;
    private final double baseY;

    protected SylvariaHazeParticle(ClientLevel level, double x, double y, double z,
                                    double dx, double dy, double dz, SpriteSet sprites) {
        super(level, x, y, z, dx, dy, dz);
        this.spriteSet = sprites;
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.friction = 1.0F;

        // Никакого подъёма вверх - только очень медленный горизонтальный дрейф "по земле".
        // Вертикальное покачивание делаем отдельно синусом в tick(), а не постоянной скоростью,
        // чтобы туман стелился на месте, а не улетал в небо.
        this.xd = dx + (this.random.nextDouble() - 0.5D) * 0.006D;
        this.yd = 0.0D;
        this.zd = dz + (this.random.nextDouble() - 0.5D) * 0.006D;

        this.baseY = y;
        this.swayPhase = this.random.nextDouble() * Math.PI * 2.0D;
        this.swaySpeed = 0.02D + this.random.nextDouble() * 0.02D;
        this.swayAmount = 0.04D + this.random.nextDouble() * 0.05D;

        this.quadSize = 0.55F + this.random.nextFloat() * 0.65F;
        this.lifetime = 140 + this.random.nextInt(100);

        float variance = 0.9F + this.random.nextFloat() * 0.2F;
        this.rCol = Math.min(1.0F, BASE_R * variance);
        this.gCol = Math.min(1.0F, BASE_G * variance);
        this.bCol = Math.min(1.0F, BASE_B * variance);
        this.targetAlpha = MAX_ALPHA * (0.75F + this.random.nextFloat() * 0.25F);
        this.alpha = 0.0F;

        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            this.setSpriteFromAge(this.spriteSet);

            // Держим частицу у исходной высоты земли + лёгкое синусоидальное покачивание -
            // никакого систематического всплытия вверх.
            double sway = Math.sin(swayPhase + this.age * swaySpeed) * swayAmount;
            this.setPos(this.x, this.baseY + sway, this.z);

            float lifeRatio = (float) this.age / (float) this.lifetime;
            // Плавно нарастаем первые 20% жизни, держим насыщенный пик, гаснем последние 45%.
            if (lifeRatio < 0.2F) {
                this.alpha = Mth.lerp(lifeRatio / 0.2F, 0.0F, targetAlpha);
            } else if (lifeRatio > 0.55F) {
                this.alpha = Mth.lerp((lifeRatio - 0.55F) / 0.45F, targetAlpha, 0.0F);
            } else {
                this.alpha = targetAlpha;
            }
        }
    }

    @Override
    public float getQuadSize(float partialTicks) {
        // Лёгкое разрастание со временем - ощущение расползающегося тумана, а не жёсткой точки.
        float lifeRatio = ((float) this.age + partialTicks) / (float) this.lifetime;
        return this.quadSize * (0.75F + lifeRatio * 0.4F);
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
