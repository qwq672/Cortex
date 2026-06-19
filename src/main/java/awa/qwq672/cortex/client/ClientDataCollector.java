package awa.qwq672.cortex.client;

import awa.qwq672.cortex.DataCollect.RecordManager;
import awa.qwq672.cortex.SafeZoneManager;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.math.Vec3d;

public class ClientDataCollector {
    private static boolean recording = false;
    private static String currentName = "";
    private static int currentMode = 2;
    private static int currentSubgoal = -1;

    public static boolean isRecording() { return recording; }
    public static String getCurrentName() { return currentName; }
    public static void setMode(int mode) { currentMode = mode; }
    public static int getMode() { return currentMode; }
    public static void setSubgoal(int subgoal) { currentSubgoal = subgoal; }
    public static int getSubgoal() { return currentSubgoal; }
    public static String getModeName() {
        switch (currentMode) {
            case 0: return "PVP";
            case 1: return "Minigame";
            case 2: return "Survival";
            default: return "Unknown";
        }
    }

    public static void startRecording(String name) {
        if (recording) return;
        RecordManager.startRecording(name);
        recording = true;
        currentName = name;
        System.out.println("[Cortex] Client recording started: " + name);
    }

    public static void stopRecording() {
        if (!recording) return;
        RecordManager.stopRecording();
        recording = false;
        currentName = "";
        System.out.println("[Cortex] Client recording stopped");
    }

    public static void collect() {
        if (!recording) return;
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) return;

        if (SafeZoneManager.isInSafeZone(player)) return;

        JsonObject root = new JsonObject();
        root.addProperty("timestamp", System.currentTimeMillis());

        JsonObject state = new JsonObject();

        state.addProperty("health", player.getHealth() / player.getMaxHealth());
        state.addProperty("food_level", player.getHungerManager().getFoodLevel() / 20.0f);
        state.addProperty("x", player.getX());
        state.addProperty("z", player.getZ());
        state.addProperty("y", player.getY());
        Vec3d vel = player.getVelocity();
        state.addProperty("vel_x", vel.x);
        state.addProperty("vel_z", vel.z);
        state.addProperty("on_ground", player.isOnGround() ? 1 : 0);
        state.addProperty("in_water", player.isTouchingWater() ? 1 : 0);
        state.addProperty("yaw", (player.getYaw() % 360) / 360.0f);
        state.addProperty("pitch", player.getPitch() / 90.0f);

        addEnemyInfo(player, state);
        state.addProperty("attack_cooldown", player.getAttackCooldownProgress(0.5f));
        state.addProperty("held_item", getItemCode(player.getMainHandStack()));
        state.addProperty("mode", currentMode);

        if (currentSubgoal >= 0) {
            state.addProperty("subgoal", currentSubgoal);
        }

        root.add("state", state);

        JsonObject action = new JsonObject();
        action.addProperty("jump", client.options.jumpKey.isPressed() ? 1 : 0);
        action.addProperty("sneak", client.options.sneakKey.isPressed() ? 1 : 0);
        action.addProperty("attack", client.options.attackKey.isPressed() ? 1 : 0);
        action.addProperty("use", client.options.useKey.isPressed() ? 1 : 0);
        action.addProperty("forward", client.options.forwardKey.isPressed() ? 1 : 0);
        action.addProperty("back", client.options.backKey.isPressed() ? 1 : 0);
        action.addProperty("left", client.options.leftKey.isPressed() ? 1 : 0);
        action.addProperty("right", client.options.rightKey.isPressed() ? 1 : 0);
        root.add("action", action);

        RecordManager.writeLine(root.toString());
    }

    private static void addEnemyInfo(ClientPlayerEntity player, JsonObject state) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            state.addProperty("enemy_dist", 1.0f);
            state.addProperty("enemy_angle", 0.0f);
            return;
        }
        PlayerEntity nearest = null;
        double minDist = Double.MAX_VALUE;
        for (PlayerEntity other : client.world.getPlayers()) {
            if (other == player) continue;
            double dist = player.distanceTo(other);
            if (dist < minDist) {
                minDist = dist;
                nearest = other;
            }
        }
        if (nearest != null) {
            state.addProperty("enemy_dist", (float) Math.min(1.0, minDist / 32.0));
            double dx = nearest.getX() - player.getX();
            double dz = nearest.getZ() - player.getZ();
            double angleToEnemy = Math.toDegrees(Math.atan2(dz, dx));
            double playerYaw = player.getYaw();
            double relativeAngle = angleToEnemy - playerYaw;
            relativeAngle = ((relativeAngle % 360) + 360) % 360;
            if (relativeAngle > 180) relativeAngle -= 360;
            state.addProperty("enemy_angle", (float) (relativeAngle / 180.0));
        } else {
            state.addProperty("enemy_dist", 1.0f);
            state.addProperty("enemy_angle", 0.0f);
        }
    }

    private static int getItemCode(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        Item item = stack.getItem();
        if (item instanceof SwordItem) return 1;
        if (item instanceof AxeItem) return 2;
        if (item instanceof PickaxeItem) return 3;
        if (item instanceof BlockItem) return 4;
        if (item instanceof BowItem) return 5;
        if (item.isFood()) return 6;
        return 7;
    }
}