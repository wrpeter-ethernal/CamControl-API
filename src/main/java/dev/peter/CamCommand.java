package dev.peter;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.peter.network.StartCinematicPayload;
import dev.peter.network.StopCinematicPayload;
import dev.peter.util.Keyframe;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CamCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("cam")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(literal("add")
                            .then(argument("duration", FloatArgumentType.floatArg(0.1f))
                                    .executes(context -> {
                                        ServerPlayerEntity player = context.getSource().getPlayer();
                                        if (player == null) return 0;
                                        float duration = FloatArgumentType.getFloat(context, "duration");
                                        CamControl.addKeyframe(player.getX(), player.getEyeY(), player.getZ(), player.getYaw(), player.getPitch(), duration);
                                        context.getSource().sendFeedback(() -> Text.literal("[CamControl] Keyframe added at current position with duration: " + duration + "s")
                                                .formatted(Formatting.AQUA), true);
                                        return 1;
                                    })
                            )
                            .executes(context -> {
                                ServerPlayerEntity player = context.getSource().getPlayer();
                                if (player == null) return 0;
                                CamControl.addKeyframe(player.getX(), player.getEyeY(), player.getZ(), player.getYaw(), player.getPitch(), 5.0f);
                                context.getSource().sendFeedback(() -> Text.literal("[CamControl] Keyframe added at current position (Default duration: 5s)")
                                        .formatted(Formatting.AQUA), true);
                                return 1;
                            })
                    )
                    .then(literal("clear")
                            .executes(context -> {
                                CamControl.clearKeyframes();
                                context.getSource().sendFeedback(() -> Text.literal("[CamControl] All keyframes cleared.")
                                        .formatted(Formatting.YELLOW), true);
                                return 1;
                            })
                    )
                    .then(literal("list")
                            .executes(context -> {
                                var list = CamControl.getKeyframes();
                                if (list.isEmpty()) {
                                    context.getSource().sendFeedback(() -> Text.literal("[CamControl] No keyframes defined."), false);
                                    return 1;
                                }
                                context.getSource().sendFeedback(() -> Text.literal("[CamControl] Current Keyframes:").formatted(Formatting.GOLD), false);
                                for (int i = 0; i < list.size(); i++) {
                                    Keyframe k = list.get(i);
                                    int finalI = i;
                                    context.getSource().sendFeedback(() -> Text.literal("#" + finalI + ": [" + String.format("%.1f, %.1f, %.1f", k.x(), k.y(), k.z()) + "] Duration: " + k.duration() + "s"), false);
                                }
                                return 1;
                            })
                    )
                    .then(literal("remove")
                            .then(argument("index", IntegerArgumentType.integer(0))
                                    .executes(context -> {
                                        int index = IntegerArgumentType.getInteger(context, "index");
                                        var list = CamControl.getKeyframes();
                                        if (index >= list.size()) {
                                            context.getSource().sendError(Text.literal("[CamControl] Error: Index " + index + " out of bounds."));
                                            return 0;
                                        }
                                        CamControl.removeKeyframe(index);
                                        context.getSource().sendFeedback(() -> Text.literal("[CamControl] Removed keyframe #" + index).formatted(Formatting.YELLOW), true);
                                        return 1;
                                    })
                            )
                    )
                    .then(literal("play")
                            .then(argument("players", EntityArgumentType.players())
                                    .executes(context -> {
                                        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "players");
                                        playCinematic(context.getSource(), players);
                                        return 1;
                                    })
                            )
                            .executes(context -> {
                                playCinematic(context.getSource(), context.getSource().getServer().getPlayerManager().getPlayerList());
                                return 1;
                            })
                    )
                    .then(literal("stop")
                            .then(argument("players", EntityArgumentType.players())
                                    .executes(context -> {
                                        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "players");
                                        stopCinematic(context.getSource(), players);
                                        return 1;
                                    })
                            )
                            .executes(context -> {
                                stopCinematic(context.getSource(), context.getSource().getServer().getPlayerManager().getPlayerList());
                                return 1;
                            })
                    )
            );
        });
    }

    private static void playCinematic(ServerCommandSource source, Collection<ServerPlayerEntity> players) {
        var list = CamControl.getKeyframes();
        if (list.size() < 2) {
            source.sendError(Text.literal("[CamControl] Error: You need at least 2 keyframes to play a cinematic."));
            return;
        }
        StartCinematicPayload payload = new StartCinematicPayload(list);
        for (ServerPlayerEntity player : players) {
            ServerPlayNetworking.send(player, payload);
        }
        source.sendFeedback(() -> Text.literal("[CamControl] Cinematic started for " + players.size() + " players.")
                .formatted(Formatting.GREEN), true);
    }

    private static void stopCinematic(ServerCommandSource source, Collection<ServerPlayerEntity> players) {
        StopCinematicPayload payload = new StopCinematicPayload();
        for (ServerPlayerEntity player : players) {
            ServerPlayNetworking.send(player, payload);
        }
        source.sendFeedback(() -> Text.literal("[CamControl] Cinematic stopped for " + players.size() + " players.")
                .formatted(Formatting.RED), true);
    }
}
