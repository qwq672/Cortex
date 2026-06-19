package awa.qwq672.cortex.DataCollect;

import com.google.gson.JsonObject;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DataCollector {
    private static final ConcurrentHashMap<UUID, AttackTarget> attackTargets = new ConcurrentHashMap<>();

    static class AttackTarget {
        LivingEntity entity;
        long lastAttackTick;
        AttackTarget(LivingEntity e, long tick) {
            entity = e;
            lastAttackTick = tick;
        }
    }

    public static void tickCleanup(long currentTick) {
        attackTargets.entrySet().removeIf(entry -> currentTick - entry.getValue().lastAttackTick > 40);
    }

    public static void onAttack(ServerPlayerEntity attacker, LivingEntity target) {
        if (RecordManager.isRecordingPlayer(attacker.getUuid())) {
            attackTargets.put(attacker.getUuid(), new AttackTarget(target, attacker.getServer().getTicks()));
            JsonObject event = new JsonObject();
            event.addProperty("event", "attack");
            event.addProperty("target_uuid", target.getUuid().toString());
            RecordManager.writeLine(event.toString());
        }
    }
}