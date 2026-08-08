package net.sylvariamod.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.sylvariamod.SylvariaMod;
import net.sylvariamod.particle.ModParticles;

/**
 * Летающие в воздухе частицы-мотыльки/искры по всему биому - спавнятся сами вместо
 * ванильного "ambient particle" биома (тот механизм не проверяет рельеф и давал "всплытие
 * из-под земли/воды", см. историю правок). Раньше частицы жались строго к земле (0.05-0.35
 * блока над поверхностью) - теперь свободно раскиданы по высоте (0.3-7 блоков над землёй),
 * то есть реально летают в воздухе леса, а не стелятся туманом по грунту.
 */
@Mod.EventBusSubscriber(modid = SylvariaMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class SylvariaHazeSpawner {

    // Радиус в блоках вокруг игрока, где может появиться частица.
    private static final int RADIUS = 28;
    private static final int ATTEMPTS_PER_TICK = 8;

    // Диапазон высоты НАД землёй, где частицы летают - не жмутся к земле, а заполняют
    // весь подлесок/пространство между стволами.
    private static final double MIN_HEIGHT_ABOVE_GROUND = 0.3D;
    private static final double MAX_HEIGHT_ABOVE_GROUND = 7.0D;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null) return;

        BlockPos playerPos = player.blockPosition();
        if (!isSylvariaBiome(level, playerPos)) return;

        var random = level.random;
        for (int i = 0; i < ATTEMPTS_PER_TICK; i++) {
            if (random.nextFloat() > 0.5F) continue;

            int dx = random.nextInt(RADIUS * 2) - RADIUS;
            int dz = random.nextInt(RADIUS * 2) - RADIUS;
            int x = playerPos.getX() + dx;
            int z = playerPos.getZ() + dz;

            // Точку тоже проверяем на биом - радиус большой, игрок может стоять у самой
            // границы биома, и без этого частицы вылезали бы за пределы леса.
            if (!isSylvariaBiome(level, new BlockPos(x, playerPos.getY(), z))) continue;

            // MOTION_BLOCKING_NO_LEAVES игнорирует листву - это высота именно земли/травы,
            // а не верхушки дерева, от неё и откладываем высоту полёта частицы.
            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

            if (Math.abs(surfaceY - playerPos.getY()) > 32) continue;

            double heightAboveGround = MIN_HEIGHT_ABOVE_GROUND
                    + random.nextDouble() * (MAX_HEIGHT_ABOVE_GROUND - MIN_HEIGHT_ABOVE_GROUND);
            double px = x + 0.5D + (random.nextDouble() - 0.5D) * 0.8D;
            double py = surfaceY + heightAboveGround;
            double pz = z + 0.5D + (random.nextDouble() - 0.5D) * 0.8D;

            BlockPos target = BlockPos.containing(px, py, pz);
            BlockState targetState = level.getBlockState(target);
            FluidState targetFluid = level.getFluidState(target);

            // Не спавним прямо внутри блока (ствол/листва/земля) или в воде - иначе частицу
            // не видно вовсе или она "выныривает" из твёрдого объекта.
            if (!targetState.isAir()) continue;
            if (!targetFluid.isEmpty()) continue;

            level.addParticle(ModParticles.SYLVARIA_HAZE.get(), px, py, pz, 0.0D, 0.0D, 0.0D);
        }
    }

    // По неймспейсу мода, а не по точному имени файла биома - надёжнее (сработает для
    // sylvaria_forest и для любого другого биома мода, если такой появится) и защищено
    // от несовпадения из-за формата ключа/пути.
    private static boolean isSylvariaBiome(ClientLevel level, BlockPos pos) {
        return level.getBiome(pos).unwrapKey()
                .map(key -> key.location().getNamespace().equals(SylvariaMod.MODID))
                .orElse(false);
    }
}
