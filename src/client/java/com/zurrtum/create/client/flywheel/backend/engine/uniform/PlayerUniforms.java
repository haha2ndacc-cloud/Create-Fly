package com.zurrtum.create.client.flywheel.backend.engine.uniform;

import com.zurrtum.create.client.flywheel.api.backend.RenderContext;
import com.zurrtum.create.client.flywheel.backend.FlwBackendXplat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.TeamColor;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public final class PlayerUniforms extends UniformWriter {
    private static final int SIZE = 16 * 2 + 8 + 4 * 9;
    static final UniformBuffer BUFFER = new UniformBuffer(Uniforms.PLAYER_INDEX, SIZE);

    private PlayerUniforms() {
    }

    public static void update(RenderContext context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            BUFFER.clear();
            return;
        }

        long ptr = BUFFER.ptr();

        PlayerInfo info = player.getPlayerInfo();

        Vec3 eyePos = player.getEyePosition(context.partialTick());
        ptr = writeVec3(ptr, (float) eyePos.x, (float) eyePos.y, (float) eyePos.z);

        ptr = writeTeamColor(ptr, info == null ? null : info.getTeam());

        ptr = writeEyeBrightness(ptr, player);

        ptr = writeHeldLight(ptr, player);
        ptr = writeEyeIn(ptr, player);

        ptr = writeInt(ptr, player.isCrouching() ? 1 : 0);
        ptr = writeInt(ptr, player.isSleeping() ? 1 : 0);
        ptr = writeInt(ptr, player.isSwimming() ? 1 : 0);
        ptr = writeInt(ptr, player.isFallFlying() ? 1 : 0);

        ptr = writeInt(ptr, player.isShiftKeyDown() ? 1 : 0);

        ptr = writeInt(ptr, info == null ? 0 : info.getGameMode().getId());

        BUFFER.markDirty();
    }

    private static long writeTeamColor(long ptr, @Nullable PlayerTeam team) {
        if (team != null) {
            Optional<TeamColor> teamColor = team.getColor();
            if (teamColor.isPresent()) {
                int color = teamColor.get().rgb();
                int red = ARGB.red(color);
                int green = ARGB.green(color);
                int blue = ARGB.blue(color);
                return writeVec4(ptr, red / 255.0f, green / 255.0f, blue / 255.0f, 1.0f);
            }
            return writeVec4(ptr, 1.0f, 1.0f, 1.0f, 1.0f);
        }
        return writeVec4(ptr, 1.0f, 1.0f, 1.0f, 0.0f);
    }

    private static long writeEyeBrightness(long ptr, LocalPlayer player) {
        Level level = player.level();
        int blockBrightness = level.getBrightness(LightLayer.BLOCK, player.blockPosition());
        int skyBrightness = level.getBrightness(LightLayer.SKY, player.blockPosition());
        int maxBrightness = 15;

        return writeVec2(ptr, (float) blockBrightness / maxBrightness, (float) skyBrightness / maxBrightness);
    }

    private static long writeHeldLight(long ptr, LocalPlayer player) {
        int heldLight = 0;

        for (InteractionHand hand : InteractionHand.values()) {
            Item handItem = player.getItemInHand(hand).getItem();
            if (handItem instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                int blockLight = FlwBackendXplat.INSTANCE.getLightEmission(
                    block.defaultBlockState(),
                    player.level(),
                    player.blockPosition()
                );
                if (heldLight < blockLight) {
                    heldLight = blockLight;
                }
            }
        }

        return writeFloat(ptr, (float) heldLight / 15);
    }

    private static long writeEyeIn(long ptr, LocalPlayer player) {
        Level level = player.level();
        Vec3 eyePos = player.getEyePosition();
        BlockPos blockPos = BlockPos.containing(eyePos);
        return writeInFluidAndBlock(ptr, level, blockPos, eyePos);
    }
}
