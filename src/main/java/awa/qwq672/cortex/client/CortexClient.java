package awa.qwq672.cortex.client;

import awa.qwq672.cortex.SafeZoneManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class CortexClient implements ClientModInitializer {
    private static KeyBinding safeZoneKey;
    private static KeyBinding recordKey;

    @Override
    public void onInitializeClient() {
        SafeZoneManager.load();

        safeZoneKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.cortex.open_safezone_gui",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "category.cortex"
        ));

        recordKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.cortex.open_record_gui",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "category.cortex"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (safeZoneKey.wasPressed()) {
                client.setScreen(new SafeZoneScreen());
            }
            while (recordKey.wasPressed()) {
                client.setScreen(new RecordingScreen());
            }
            ClientDataCollector.collect();
        });
    }
}