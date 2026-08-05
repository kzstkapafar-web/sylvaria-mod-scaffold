package net.sylvariamod.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;

/**
 * Хранит координаты "якорей" (нижний левый угол рамки) всех порталов Sylvaria,
 * когда-либо автоматически построенных на стороне назначения в данном измерении.
 * Используется, чтобы:
 *  1) при повторном входе в тот же портал не плодить дубликаты рядом;
 *  2) новые автопостроенные порталы никогда не пересекались с уже существующими.
 *
 * Данные сохраняются на диск вместе с миром (в data storage измерения),
 * поэтому переживают перезапуск сервера.
 */
public class PortalRegistrySavedData extends SavedData {

    private static final String DATA_NAME = "sylvaria_portal_registry";

    private final List<BlockPos> anchors = new ArrayList<>();

    public static PortalRegistrySavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(PortalRegistrySavedData::new, PortalRegistrySavedData::load),
                DATA_NAME
        );
    }

    public List<BlockPos> anchors() {
        return anchors;
    }

    public void addAnchor(BlockPos pos) {
        anchors.add(pos.immutable());
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        long[] packed = new long[anchors.size()];
        for (int i = 0; i < anchors.size(); i++) {
            packed[i] = anchors.get(i).asLong();
        }
        tag.putLongArray("anchors", packed);
        return tag;
    }

    public static PortalRegistrySavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        PortalRegistrySavedData data = new PortalRegistrySavedData();
        for (long packed : tag.getLongArray("anchors")) {
            data.anchors.add(BlockPos.of(packed));
        }
        return data;
    }
}
