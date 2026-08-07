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
 * Стелющийся туман биома - спавнится САМ, вместо ванильного "ambient particle" биома.
 * Ванильный механизм (BiomeSpecialEffects.ambientParticle) берёт случайную точку в объёме
 * вокруг игрока БЕЗ проверки рельефа - отсюда частицы "всплывали из-под земли/воды" и
 * висели прямо в небе. Здесь позиция всегда берётся именно у поверхности земли: находим
 * высоту через heightmap, проверяем что сверху воздух и что это не вода, и только тогда
 * спавним частицу низко над самой землёй.
 */
@Mod.EventBusSubscriber(modid = SylvariaMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class SylvariaHazeSpawner {

    private static final int RADIUS = 12;
    private static final int ATTEMPTS_PER_TICK = 2;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null) return;

        BlockPos playerPos = player.blockPosition();
        if (!isSylvariaForest(level, playerPos)) return;

        var random = level.random;
        for (int i = 0; i < ATTEMPTS_PER_TICK; i++) {
            if (random.nextFloat() > 0.35F) continue;

            int dx = random.nextInt(RADIUS * 2) - RADIUS;
            int dz = random.nextInt(RADIUS * 2) - RADIUS;
            int x = playerPos.getX() + dx;
            int z = playerPos.getZ() + dz;

            // MOTION_BLOCKING_NO_LEAVES игнорирует листву - находит именно землю под кроной,
            // а не верх дерева, так что туман не зависает в воздухе на высоте веток.
            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos ground = new BlockPos(x, surfaceY - 1, z);
            BlockPos above = new BlockPos(x, surfaceY, z);

            BlockState groundState = level.getBlockState(ground);
            FluidState aboveFluid = level.getFluidState(above);
            BlockState aboveState = level.getBlockState(above);

            // Пропускаем воду/лёд/что угодно жидкое сверху - туман не должен "всплывать" из озёр.
            if (!aboveFluid.isEmpty()) continue;
            if (!aboveState.isAir()) continue;
            if (!isValidGround(groundState)) continue;

            // Игрок не должен быть слишком далеко по высоте (не спавним у него под ногами в
            // случае резкого перепада рельефа за пределами реального радиуса видимости).
            if (Math.abs(surfaceY - playerPos.getY()) > 24) continue;

            double px = x + 0.5D + (random.nextDouble() - 0.5D) * 0.8D;
            double py = surfaceY + 0.05D + random.nextDouble() * 0.35D;
            double pz = z + 0.5D + (random.nextDouble() - 0.5D) * 0.8D;

            level.addParticle(ModParticles.SYLVARIA_HAZE.get(), px, py, pz, 0.0D, 0.0D, 0.0D);
        }
    }

    private static boolean isSylvariaForest(ClientLevel level, BlockPos pos) {
        return level.getBiome(pos).unwrapKey()
                .map(key -> key.location().toString().equals("sylvaria:sylvaria_forest"))
                .orElse(false);
    }

    private static boolean isValidGround(BlockState state) {
        return state.is(net.minecraft.tags.BlockTags.DIRT)
                || state.is(net.minecraft.world.level.block.Blocks.PODZOL)
                || state.is(net.minecraft.world.level.block.Blocks.MYCELIUM)
                || state.is(net.minecraft.world.level.block.Blocks.MOSS_BLOCK)
                || state.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK);
    }
}
