package com.zurrtum.create.client.flywheel.impl;

import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.memory.FlwMemoryTracker;
import com.zurrtum.create.client.flywheel.lib.util.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.zurrtum.create.client.flywheel.impl.Flywheel.MOD_ID;

public class DebugEntryFlw implements DebugScreenEntry {
    private static final Identifier GROUP = Identifier.fromNamespaceAndPath(MOD_ID, "group");
    public static final Identifier ID = register("debug_flywheel", new DebugEntryFlw());

    private static Identifier register(String id, DebugScreenEntry entry) {
        Identifier key = Identifier.fromNamespaceAndPath(MOD_ID, id);
        DebugScreenEntries.ENTRIES_BY_ID.put(key, entry);
        return key;
    }

    public static void register() {
    }

    @Override
    public void display(
        DebugScreenDisplayer displayer,
        @Nullable Level serverOrClientLevel,
        @Nullable LevelChunk clientChunk,
        @Nullable LevelChunk serverChunk
    ) {
        List<String> systemInfo = new ArrayList<>();
        systemInfo.add("Flywheel: " + FlwImplXplat.INSTANCE.getVersionStr());
        systemInfo.add("Backend: " + BackendManagerImpl.getBackendString());
        systemInfo.add("Update limiting: " + (FlwConfig.INSTANCE.limitUpdates() ? "on" : "off"));
        VisualizationManager manager = VisualizationManager.get(Minecraft.getInstance().level);
        if (manager != null) {
            systemInfo.add("B: " + manager.blockEntities().visualCount() + ", E: " + manager.entities()
                .visualCount() + ", F: " + manager.effects().visualCount());
            Vec3i renderOrigin = manager.renderOrigin();
            systemInfo.add("Origin: " + renderOrigin.getX() + ", " + renderOrigin.getY() + ", " + renderOrigin.getZ());
        }
        systemInfo.add("Memory Usage: CPU: " + StringUtil.formatBytes(FlwMemoryTracker.getCpuMemory()) + ", GPU: " + StringUtil.formatBytes(
            FlwMemoryTracker.getGpuMemory()));
        displayer.addToGroup(GROUP, systemInfo);
    }
}
