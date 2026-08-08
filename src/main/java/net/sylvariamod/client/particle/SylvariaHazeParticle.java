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
 * Летающая магическая искра - свободно блуждает в воздухе по всем 3 осям (плавно меняет
 * направление, а не летит по прямой и не жмётся к одной высоте), в отличие от прошлой
 * версии, которая стелилась туманом у земли. Насыщенный фиолетовый в цвет листвы/травы
 * биома (grass_color #7A4BC4). Спавнится по всему биому на разной высоте (см.
 * SylvariaHazeSpawner), и погуще вокруг светящихся грибов (SylvariaGlowMushroomBlock#animateTick).
 */
public class SylvariaHazeParticle extends TextureSheetParticle {

    // Насыщенный фиолетовый в тон листвы/травы биома (#7A4BC4), а не блёклый белёсый.
    private static final float BASE_R = 0.62F;
    private static final float BASE_G = 0.34F;
    private static final float BASE_B = 0.88F;
    private static final float MAX_ALPHA = 0.85F;

    // Насколько сильно каждый тик "подруливаем" скорость случайным образом - создаёт живое
    // блуждающее движение вместо прямой линии или прилипания к одному месту.
    private static final double WANDER_STRENGTH = 0.0018D;
    private static final double MAX_SPEED = 0.028D;

    private final SpriteSet spriteSet;
    private final float targetAlpha;

    protected SylvariaHazeParticle(ClientLevel level, double x, double y, double z,
                                    double dx, double dy, double dz, SpriteSet sprites) {
        super(level, x, y, z, dx, dy, dz);
        this.spriteSet = sprites;
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.friction = 1.0F;

        // Стартовая скорость в случайном направлении - дальше блуждание докручивает её в tick().
        double ang1 = this.random.nextDouble() * Math.PI * 2.0D;
        double ang2 = this.random.nextDouble() * Math.PI * 2.0D;
        double speed = MAX_SPEED * (0.3D + this.random.nextDouble() * 0.5D);
        this.xd = dx + Math.cos(ang1) * Math.cos(ang2) * speed;
        this.yd = dy + Math.sin(ang2) * speed * 0.6D;
        this.zd = dz + Math.sin(ang1) * Math.cos(ang2) * speed;

        this.quadSize = 0.12F + this.random.nextFloat() * 0.14F;
        this.lifetime = 200 + this.random.nextInt(160);

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
        // Живое блуждание: на каждый тик добавляем маленький случайный импульс к скорости
        // и подрезаем её сверху, чтобы искра плавно меняла курс, а не летела по прямой
        // и не разгонялась бесконечно.
        this.xd += (this.random.nextDouble() - 0.5D) * WANDER_STRENGTH;
        this.yd += (this.random.nextDouble() - 0.5D) * WANDER_STRENGTH * 0.7D;
        this.zd += (this.random.nextDouble() - 0.5D) * WANDER_STRENGTH;

        double speed = Math.sqrt(this.xd * this.xd + this.yd * this.yd + this.zd * this.zd);
        if (speed > MAX_SPEED) {
            double scale = MAX_SPEED / speed;
            this.xd *= scale;
            this.yd *= scale;
            this.zd *= scale;
        }

        super.tick();

        if (!this.removed) {
            this.setSpriteFromAge(this.spriteSet);

            float lifeRatio = (float) this.age / (float) this.lifetime;
            // Плавно нарастаем первые 15% жизни, держим насыщенный пик, гаснем последние 35%.
            if (lifeRatio < 0.15F) {
                this.alpha = Mth.lerp(lifeRatio / 0.15F, 0.0F, targetAlpha);
            } else if (lifeRatio > 0.65F) {
                this.alpha = Mth.lerp((lifeRatio - 0.65F) / 0.35F, targetAlpha, 0.0F);
            } else {
                this.alpha = targetAlpha;
            }
        }
    }

    @Override
    public float getQuadSize(float partialTicks) {
        // Лёгкое пульсирующее мерцание размера - живее, чем константный размер точки.
        float pulse = 0.85F + 0.15F * Mth.sin((this.age + partialTicks) * 0.2F);
        return this.quadSize * pulse;
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
