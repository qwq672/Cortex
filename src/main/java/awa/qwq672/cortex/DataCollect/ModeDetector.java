package awa.qwq672.cortex.DataCollect;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

public class ModeDetector {
    public static String detectMode(ServerPlayerEntity player) {
        if (player.isCreative()) return "creative";
        if (player.isSpectator()) return "spectator";
        if (player.interactionManager.getGameMode() == GameMode.SURVIVAL) {
            // 简单判断：手持剑视为pvp模式（粗略）
            boolean hasSword = player.getMainHandStack().getItem().toString().contains("sword");
            return hasSword ? "pvp" : "survival";
        }
        return "other";
    }
}