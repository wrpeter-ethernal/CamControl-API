package dev.peter;

import dev.peter.network.ShakePayload;
import dev.peter.network.StartCinematicPayload;
import dev.peter.network.StopCinematicPayload;
import dev.peter.network.SyncKeyframesPayload;
import dev.peter.util.Keyframe;
import dev.peter.util.CinematicStorage;
import net.minecraft.util.Identifier;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class CamControl implements ModInitializer {

    private static final List<Keyframe> keyframes = new ArrayList<>();

    public static Identifier id(String path) {
        return Identifier.of("camcontrol", path);
    }

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(StartCinematicPayload.ID, StartCinematicPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(StopCinematicPayload.ID, StopCinematicPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncKeyframesPayload.ID, SyncKeyframesPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ShakePayload.ID, ShakePayload.CODEC);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            List<Keyframe> loaded = CinematicStorage.loadSession(server);
            if (loaded != null) {
                keyframes.clear();
                keyframes.addAll(loaded);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            CinematicStorage.saveSession(server, keyframes);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (handler.player.hasPermissionLevel(2)) {
                sync(handler.player);
            }
        });

        CamCommand.register();
    }

    public static List<Keyframe> getKeyframes() {
        return keyframes;
    }

    public static void sync(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (player.hasPermissionLevel(2)) {
                sync(player);
            }
        }
    }

    public static void sync(ServerPlayerEntity player) {
        SyncKeyframesPayload payload = new SyncKeyframesPayload(new ArrayList<>(keyframes));
        ServerPlayNetworking.send(player, payload);
    }

    public static void addKeyframe(double x, double y, double z, float yaw, float pitch, float duration, float shakeIntensity, float shakeSpeed, int targetEntityId) {
        keyframes.add(new Keyframe(x, y, z, yaw, pitch, duration, shakeIntensity, shakeSpeed, targetEntityId));
    }

    public static void removeKeyframe(int index) {
        if (index >= 0 && index < keyframes.size()) {
            keyframes.remove(index);
        }
    }

    public static void clearKeyframes() {
        keyframes.clear();
    }
}
