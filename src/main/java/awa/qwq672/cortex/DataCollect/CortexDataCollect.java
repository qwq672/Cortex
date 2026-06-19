package awa.qwq672.cortex.DataCollect;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CortexDataCollect implements ModInitializer {
    public static final String MOD_ID = "cortex";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Cortex Data Collection Mod initialized");
        CommandRecord.register();

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (player instanceof ServerPlayerEntity attacker && entity instanceof LivingEntity target) {
                DataCollector.onAttack(attacker, target);
            }
            return ActionResult.PASS;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            DataCollector.tickCleanup(server.getTicks());
        });
    }
}