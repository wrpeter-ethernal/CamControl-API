package dev.peter;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
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
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
                    .then(literal("delete")
                            .then(argument("name", StringArgumentType.string())
                                    .suggests((context, builder) -> CommandSource.suggestMatching(CinematicStorage.getAll(context.getSource().getServer()).keySet(), builder))
                                    .executes(context -> {
                                        String name = StringArgumentType.getString(context, "name");
                                        CinematicStorage.delete(context.getSource().getServer(), name);
                                        context.getSource().sendFeedback(() -> Text.literal("[CamControl] Deleted cinematic: ").formatted(Formatting.RED)
                                                .append(Text.literal(name).formatted(Formatting.GOLD)), true);
                                        return 1;
                                    })
                            )
                    )
                    .then(literal("clear")
                            .executes(context -> {
                                CamControl.clearKeyframes();
                                context.getSource().sendFeedback(() -> Text.literal("[CamControl] All session keyframes cleared.")
                                        .formatted(Formatting.YELLOW), true);
                                return 1;
                            })
                    )
                    .then(literal("list")
                            .executes(context -> {
                                var current = CamControl.getKeyframes();
                                Map<String, List<Keyframe>> saved = CinematicStorage.getAll(context.getSource().getServer());

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
                                    context.getSource().sendFeedback(() -> Text.literal("[CamControl] Saved Cinematics (Click to Play):").formatted(Formatting.GOLD), false);
                                    for (String name : saved.keySet()) {
                                        int count = saved.get(name).size();
                                        MutableText text = Text.literal("- " + name + " (" + count + " points)")
                                                .formatted(Formatting.YELLOW)
                                                .styled(style -> style
                                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cam play " + name))
                                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to play cinematic: " + name))));
                                        context.getSource().sendFeedback(() -> text, false);
                                    }
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
                            .then(argument("name", StringArgumentType.string())
                                    .suggests((context, builder) -> CommandSource.suggestMatching(CinematicStorage.getAll(context.getSource().getServer()).keySet(), builder))
                                    .executes(context -> {
                                        String name = StringArgumentType.getString(context, "name");
                                        var list = CinematicStorage.load(context.getSource().getServer(), name);
                                        if (list == null) {
                                            context.getSource().sendError(Text.literal("[CamControl] Error: Cinematic '" + name + "' not found."));
                                            return 0;
                                        }
                                        playCinematic(context.getSource(), context.getSource().getServer().getPlayerManager().getPlayerList(), list);
                                        return 1;
                                    })
                            )
                            .executes(context -> {
                                playCinematic(context.getSource(), context.getSource().getServer().getPlayerManager().getPlayerList(), CamControl.getKeyframes());
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
