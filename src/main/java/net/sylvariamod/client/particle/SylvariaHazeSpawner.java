package net.sylvariamod.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.sylvariamod.SylvariaMod;
import net.sylvariamod.particle.ModParticles;

/**
 * Летающие в воздухе частицы-мотыльки/искры по всему биому - спавнятся сами вместо
 * ванильного "ambient particle" биома (тот механизм не проверяет рельеф и давал "всплытие
 * из-под земли/воды", см. историю правок). Раньше частицы жались строго к земле (0.05-0.35
 * блока над поверхностью) - теперь свободно раскиданы по высоте (0.3-7 блоков над землёй),
 * то есть реально летают в воздухе леса, а не стелятся туманом по грунту.
 *
 * ВАЖНО: класс регистрируется ЯВНО из ClientSetup.onClientSetup (MinecraftForge.EVENT_BUS
 * .register(SylvariaHazeSpawner.class)), а не через @Mod.EventBusSubscriber. Триггер - тоже
 * не TickEvent (несколько версий подряд с разной логикой проверки биома/измерения не давали
 * вообще никакого эффекта - похоже, TickEvent.ClientTickEvent в этой сборке Forge просто не
 * долетал до слушателя). Вместо него - RenderLevelStageEvent: он жёстко привязан к отрисовке
 * кадра, а раз игра вообще что-то рисует на экране, это событие гарантированно происходит.
 */
public class SylvariaHazeSpawner {

    // Радиус в блоках вокруг игрока, где может появиться частица.
    private static final int RADIUS = 28;
    // RenderLevelStageEvent летит ~60 раз/сек (за кадром), а не ~20 раз/сек как тик - поэтому
    // попыток на срабатывание меньше, чтобы итоговая плотность частиц осталась комфортной.
    private static final int ATTEMPTS_PER_FRAME = 3;

    // Диапазон высоты НАД землёй, где частицы летают - не жмутся к земле, а заполняют
    // весь подлесок/пространство между стволами.
    private static final double MIN_HEIGHT_ABOVE_GROUND = 0.3D;
    private static final double MAX_HEIGHT_ABOVE_GROUND = 7.0D;

    private static boolean loggedOnce = false;
    private static int frameCounter = 0;
    private static int rejectedDimension = 0;
    private static int rejectedHeightDiff = 0;
    private static int rejectedNotAir = 0;
    private static int rejectedFluid = 0;
    private static int spawnedCount = 0;

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null) return;

        if (!loggedOnce) {
            // Разовая метка в лог - открой logs/latest.log и поищи эту строку, чтобы
            // убедиться, что спавнер реально запускается на твоей стороне.
            SylvariaMod.LOGGER.info("[Sylvaria] SylvariaHazeSpawner активен, рендер-хук сработал.");
            loggedOnce = true;
        }

        // Раз в ~200 кадров (~каждые 3 сек) печатаем ПОЛНУЮ сводку: в каком мы измерении,
        // прошла ли проверка, и на каком именно фильтре отсеиваются попытки спавна. Этого
        // достаточно, чтобы одним логом точно понять, где затык, вместо гадания по коду.
        frameCounter++;
        boolean printSummary = frameCounter % 200 == 0;

        BlockPos playerPos = player.blockPosition();
        boolean inSylvaria = isSylvariaDimension(level);
        if (!inSylvaria) {
            rejectedDimension++;
            if (printSummary) {
                SylvariaMod.LOGGER.info("[Sylvaria] СВОДКА: измерение={} (namespace={}) - НЕ sylvaria, спавн пропускается. Отклонено по измерению за интервал: {}",
                        level.dimension().location(), level.dimension().location().getNamespace(), rejectedDimension);
                rejectedDimension = 0;
            }
            return;
        }

        var random = level.random;
        for (int i = 0; i < ATTEMPTS_PER_FRAME; i++) {
            if (random.nextFloat() > 0.5F) continue;

            int dx = random.nextInt(RADIUS * 2) - RADIUS;
            int dz = random.nextInt(RADIUS * 2) - RADIUS;
            int x = playerPos.getX() + dx;
            int z = playerPos.getZ() + dz;

            // MOTION_BLOCKING_NO_LEAVES игнорирует листву - это высота именно земли/травы,
            // а не верхушки дерева, от неё и откладываем высоту полёта частицы.
            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

            if (Math.abs(surfaceY - playerPos.getY()) > 32) {
                rejectedHeightDiff++;
                continue;
            }

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
            if (!targetState.isAir()) {
                rejectedNotAir++;
                continue;
            }
            if (!targetFluid.isEmpty()) {
                rejectedFluid++;
                continue;
            }

            level.addParticle(ModParticles.SYLVARIA_HAZE.get(), px, py, pz, 0.0D, 0.0D, 0.0D);
            spawnedCount++;
        }

        if (printSummary) {
            SylvariaMod.LOGGER.info("[Sylvaria] СВОДКА: измерение OK ({}). За интервал заспавнено={}, отклонено по высоте={}, отклонено (не воздух)={}, отклонено (жидкость)={}. Игрок y={}",
                    level.dimension().location(), spawnedCount, rejectedHeightDiff, rejectedNotAir, rejectedFluid, playerPos.getY());
            spawnedCount = 0;
            rejectedHeightDiff = 0;
            rejectedNotAir = 0;
            rejectedFluid = 0;
        }
    }

    // Проверяем ИЗМЕРЕНИЕ, а не биом через Holder.unwrapKey() - тот способ ненадёжен (может
    // не резолвиться на границах чанков/при биом-бленде и просто возвращать false молча,
    // из-за чего фоновый рой не спавнился нигде, а искры были видны только у грибов через
    // Block#animateTick, который от биома вообще не зависит). Весь dimension sylvaria - это
    // один фиксированный биом (см. ModDimensions/worldgen), так что достаточно проверить
    // ключ измерения - он не зависит от блендинга и всегда резолвится однозначно.
    private static boolean isSylvariaDimension(ClientLevel level) {
        return level.dimension().location().getNamespace().equals(SylvariaMod.MODID);
    }
}
