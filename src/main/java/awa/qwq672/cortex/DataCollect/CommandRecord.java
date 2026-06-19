package awa.qwq672.cortex.DataCollect;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.*;

public class CommandRecord {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("record")
                    .then(literal("start")
                            .then(argument("name", StringArgumentType.word())
                                    .executes(ctx -> {
                                        String name = StringArgumentType.getString(ctx, "name");
                                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                                        if (player == null) return 0;
                                        if (RecordManager.isRecording()) {
                                            ctx.getSource().sendError(Text.literal("Already recording globally"));
                                            return 0;
                                        }
                                        RecordManager.startRecording(name);
                                        RecordManager.addRecordingPlayer(player.getUuid());
                                        ctx.getSource().sendFeedback(() -> Text.literal("Started recording as " + name), false);
                                        return 1;
                                    })
                            )
                    )
                    .then(literal("stop")
                            .executes(ctx -> {
                                ServerPlayerEntity player = ctx.getSource().getPlayer();
                                if (player == null) return 0;
                                if (!RecordManager.isRecording()) {
                                    ctx.getSource().sendError(Text.literal("Not recording"));
                                    return 0;
                                }
                                RecordManager.removeRecordingPlayer(player.getUuid());
                                if (RecordManager.getRecordingPlayerCount() == 0) {
                                    RecordManager.stopRecording();
                                }
                                ctx.getSource().sendFeedback(() -> Text.literal("Stopped recording for you"), false);
                                return 1;
                            })
                    )
                    .then(literal("mode")
                            .then(literal("pvp").executes(ctx -> {
                                RecordManager.setMode(0);
                                ctx.getSource().sendFeedback(() -> Text.literal("Mode set to PVP"), false);
                                return 1;
                            }))
                            .then(literal("minigame").executes(ctx -> {
                                RecordManager.setMode(1);
                                ctx.getSource().sendFeedback(() -> Text.literal("Mode set to Minigame"), false);
                                return 1;
                            }))
                            .then(literal("survival").executes(ctx -> {
                                RecordManager.setMode(2);
                                ctx.getSource().sendFeedback(() -> Text.literal("Mode set to Survival"), false);
                                return 1;
                            }))
                    )
            );
        });
    }
}