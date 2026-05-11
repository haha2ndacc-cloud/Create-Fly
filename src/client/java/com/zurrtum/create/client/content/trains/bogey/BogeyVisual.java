package com.zurrtum.create.client.content.trains.bogey;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public interface BogeyVisual {
    void update(CompoundTag bogeyData, float wheelAngle, PoseStack poseStack);

    void hide();

    void updateLight(int packedLight);

    void collectCrumblingInstances(Consumer<@Nullable Instance> consumer);

    void delete();

    default LongSet createSections(BlockPos pos) {
        return LongSet.of(SectionPos.asLong(pos));
    }

    default LongSet createSections(BlockPos pos, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        LongSet longSet = new LongArraySet();
        int offsetX = pos.getX();
        int offsetY = pos.getY();
        int offsetZ = pos.getZ();
        int maxSectionX = SectionPos.posToSectionCoord(offsetX + maxX);
        int maxSectionY = SectionPos.posToSectionCoord(offsetY + maxY);
        int maxSectionZ = SectionPos.posToSectionCoord(offsetZ + maxZ);
        for (int x = SectionPos.posToSectionCoord(offsetX + minX); x <= maxSectionX; x++) {
            for (int y = SectionPos.posToSectionCoord(offsetY + minY); y <= maxSectionY; y++) {
                for (int z = SectionPos.posToSectionCoord(offsetZ + minZ); z <= maxSectionZ; z++) {
                    longSet.add(SectionPos.asLong(x, y, z));
                }
            }
        }
        return longSet;
    }
}
