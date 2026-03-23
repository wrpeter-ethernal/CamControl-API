package dev.peter;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.peter.network.ShakePayload;
import dev.peter.network.StartCinematicPayload;
import dev.peter.network.StopCinematicPayload;
import dev.peter.util.CinematicStorage;
import dev.peter.util.Keyframe;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CamCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("cam")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(literal("effect")
                            .then(literal("shake")
                                    .then(argument("targets", EntityArgumentType.players())
                                            .then(literal("start")
                                                    .then(argument("level", IntegerArgumentType.integer(1, 10))
                                                            .executes(context -> {
                                                                Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "targets");
                                                                int level = IntegerArgumentType.getInteger(context, "level");
                                                                float intensity = level * 0.15f;
                                                                for (ServerPlayerEntity player : targets) {
                                                                    ServerPlayNetworking.send(player, new ShakePayload(true, intensity));
                                                                }
                                                                context.getSource().sendFeedback(() -> Text.literal("[CamControl] Shake Level " + level + " started for " + targets.size() + " players.").formatted(Formatting.GREEN), true);
                                                                return 1;
                                                            })
                                                    )
                                                    .executes(context -> {
                                                        Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "targets");
                                                        for (ServerPlayerEntity player : targets) {
                                                            ServerPlayNetworking.send(player, new ShakePayload(true, 0.15f));
                                                        }
                                                        context.getSource().sendFeedback(() -> Text.literal("[CamControl] Shake Level 1 started for " + targets.size() + " players.").formatted(Formatting.GREEN), true);
                                                        return 1;
                                                    })
                                            )
                                            .then(literal("stop")
                                                    .executes(context -> {
                                                        Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "targets");
                                                        for (ServerPlayerEntity player : targets) {
                                                            ServerPlayNetworking.send(player, new ShakePayload(false, 0f));
                                                        }
                                                        context.getSource().sendFeedback(() -> Text.literal("[CamControl] Shake stopped for " + targets.size() + " players.").formatted(Formatting.YELLOW), true);
                                                        return 1;
                                                    })
                                            )
                                    )
                            )
                    )
                    .then(literal("add")
                            .then(argument("duration", FloatArgumentType.floatArg(0.1f))
                                    .then(argument("target", EntityArgumentType.entity())
                                            .then(argument("shakeIntensity", FloatArgumentType.floatArg(0))
                                                    .then(argument("shakeSpeed", FloatArgumentType.floatArg(0))
                                                            .executes(context -> {
                                                                ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                                                Entity target = EntityArgumentType.getEntity(context, "target");
                                                                float duration = FloatArgumentType.getFloat(context, "duration");
                                                                float shakeI = FloatArgumentType.getFloat(context, "shakeIntensity");
                                                                float shakeS = FloatArgumentType.getFloat(context, "shakeSpeed");
                                                                CamControl.addKeyframe(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch(), duration, shakeI, shakeS, target.getId());
                                                                CamControl.sync(context.getSource().getServer());
                                                                context.getSource().sendFeedback(() -> Text.literal("[CamControl] Added keyframe targeting: ").formatted(Formatting.GREEN)
                                                                        .append(target.getDisplayName()), true);
                                                                return 1;
                                                            })
                                                    )
                                            )
                                            .executes(context -> {
                                                ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                                Entity target = EntityArgumentType.getEntity(context, "target");
                                                float duration = FloatArgumentType.getFloat(context, "duration");
                                                CamControl.addKeyframe(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch(), duration, 0, 0, target.getId());
                                                CamControl.sync(context.getSource().getServer());
                                                context.getSource().sendFeedback(() -> Text.literal("[CamControl] Added keyframe targeting: ").formatted(Formatting.GREEN)
                                                        .append(target.getDisplayName()), true);
                                                return 1;
                                            })
                                    )
                                    .then(argument("shakeIntensity", FloatArgumentType.floatArg(0))
                                            .then(argument("shakeSpeed", FloatArgumentType.floatArg(0))
                                                    .executes(context -> {
                                                        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                                        float duration = FloatArgumentType.getFloat(context, "duration");
                                                        float shakeI = FloatArgumentType.getFloat(context, "shakeIntensity");
                                                        float shakeS = FloatArgumentType.getFloat(context, "shakeSpeed");
                                                        CamControl.addKeyframe(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch(), duration, shakeI, shakeS, -1);
                                                        CamControl.sync(context.getSource().getServer());
                                                        context.getSource().sendFeedback(() -> Text.literal("[CamControl] Added keyframe with shake: " + shakeI + " speed: " + shakeS).formatted(Formatting.GREEN), true);
                                                        return 1;
                                                    })
                                            )
                                    )
                                    .executes(context -> {
                                        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                        float duration = FloatArgumentType.getFloat(context, "duration");
                                        CamControl.addKeyframe(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch(), duration, 0, 0, -1);
                                        CamControl.sync(context.getSource().getServer());
                                        context.getSource().sendFeedback(() -> Text.literal("[CamControl] Added keyframe (" + duration + "s)").formatted(Formatting.GREEN), true);
                                        return 1;
                                    })
                            )
                            .executes(context -> {
                                ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                CamControl.addKeyframe(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch(), 1.0f, 0, 0, -1);
                                CamControl.sync(context.getSource().getServer());
                                context.getSource().sendFeedback(() -> Text.literal("[CamControl] Added keyframe (1.0s)").formatted(Formatting.GREEN), true);
                                return 1;
                            })
                    )
                    .then(literal("save")
                            .then(argument("name", StringArgumentType.string())
                                    .suggests((context, builder) -> CommandSource.suggestMatching(CinematicStorage.getAll(context.getSource().getServer()).keySet(), builder))
                                    .executes(context -> {
                                        String name = StringArgumentType.getString(context, "name");
                                        var list = new ArrayList<>(CamControl.getKeyframes());
                                        if (list.size() < 2) {
                                            context.getSource().sendError(Text.literal("[CamControl] Error: You need at least 2 keyframes to save a cinematic."));
                                            return 0;
                                        }
                                        CinematicStorage.save(context.getSource().getServer(), name, list);
                                        context.getSource().sendFeedback(() -> Text.literal("[CamControl] Cinematic saved as: ").formatted(Formatting.GREEN)
                                                .append(Text.literal(name).formatted(Formatting.GOLD)), true);
                                        return 1;
                                    })
                            )
                    )
                    .then(literal("list")
                            .executes(context -> {
                                var current = CamControl.getKeyframes();
                                var saved = CinematicStorage.getAll(context.getSource().getServer());

                                if (current.isEmpty() && saved.isEmpty()) {
                                    context.getSource().sendFeedback(() -> Text.literal("[CamControl] No keyframes or saved cinematics found."), false);
                                    return 1;
                                }

                                if (!current.isEmpty()) {
                                    context.getSource().sendFeedback(() -> Text.literal("[CamControl] Session Keyframes:").formatted(Formatting.AQUA), false);
                                    for (int i = 0; i < current.size(); i++) {
                                        Keyframe k = current.get(i);
                                        int finalI = i;
                                        context.getSource().sendFeedback(() -> Text.literal("#" + finalI + ": [" + String.format("%.1f, %.1f, %.1f", k.x(), k.y(), k.z()) + "] Duration: " + k.duration() + "s"), false);
                                    }
                                }

                                if (!saved.isEmpty()) {
                                    context.getSource().sendFeedback(() -> Text.literal(" "), false);
                                    context.getSource().sendFeedback(() -> Text.literal("[CamControl] Saved Cinematics:").formatted(Formatting.GOLD), false);
                                    for (String name : saved.keySet()) {
                                        int count = saved.get(name).size();
                                        context.getSource().sendFeedback(() -> Text.literal("- " + name + " (" + count + " points)").formatted(Formatting.YELLOW), false);
                                    }
                                }
                                return 1;
                            })
                    )
                    .then(literal("remove")
                            .then(argument("name", StringArgumentType.string())
                                    .suggests((context, builder) -> CommandSource.suggestMatching(CinematicStorage.getAll(context.getSource().getServer()).keySet(), builder))
                                    .executes(context -> {
                                        String name = StringArgumentType.getString(context, "name");
                                        if (CinematicStorage.delete(context.getSource().getServer(), name)) {
                                            CamControl.clearKeyframes();
                                            CamControl.sync(context.getSource().getServer());
                                            context.getSource().sendFeedback(() -> Text.literal("[CamControl] Removed cinematic and cleared session: ").formatted(Formatting.RED)
                                                    .append(Text.literal(name).formatted(Formatting.GOLD)), true);
                                        } else {
                                            context.getSource().sendError(Text.literal("[CamControl] Cinematic not found: " + name).formatted(Formatting.RED));
                                        }
                                        return 1;
                                    })
                            )
                    )
                    .then(literal("play")
                            .then(argument("players", EntityArgumentType.players())
                                    .then(argument("name", StringArgumentType.string())
                                            .suggests((context, builder) -> CommandSource.suggestMatching(CinematicStorage.getAll(context.getSource().getServer()).keySet(), builder))
                                            .executes(context -> {
                                                String name = StringArgumentType.getString(context, "name");
                                                Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "players");
                                                var list = CinematicStorage.load(context.getSource().getServer(), name);
                                                if (list == null) {
                                                    context.getSource().sendError(Text.literal("[CamControl] Error: Cinematic '" + name + "' not found."));
                                                    return 0;
                                                }
                                                playCinematic(context.getSource(), players, list);
                                                return 1;
                                            })
                                    )
                                    .executes(context -> {
                                        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "players");
                                        playCinematic(context.getSource(), players, CamControl.getKeyframes());
                                        return 1;
                                    })
                            )
                            .executes(context -> {
                                List<ServerPlayerEntity> players = List.of(context.getSource().getPlayerOrThrow());
                                playCinematic(context.getSource(), players, CamControl.getKeyframes());
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
                    .then(literal("clear")
                            .executes(context -> {
                                CamControl.clearKeyframes();
                                CamControl.sync(context.getSource().getServer());
                                context.getSource().sendFeedback(() -> Text.literal("[CamControl] Cleared session keyframes.").formatted(Formatting.RED), true);
                                return 1;
                            })
                    )
            );
        });
    }

    private static void playCinematic(ServerCommandSource source, Collection<ServerPlayerEntity> players, List<Keyframe> list) {
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
