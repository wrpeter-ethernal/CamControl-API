package dev.peter;

import dev.peter.network.StartCinematicPayload;
import dev.peter.network.StopCinematicPayload;
import dev.peter.network.SyncKeyframesPayload;
import dev.peter.util.Keyframe;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class CamControl implements ModInitializer {

    private static final List<Keyframe> keyframes = new ArrayList<>();

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(StartCinematicPayload.ID, StartCinematicPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(StopCinematicPayload.ID, StopCinematicPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncKeyframesPayload.ID, SyncKeyframesPayload.CODEC);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (handler.player.hasPermissionLevel(2)) {
                sync(server);
            }
        });

        CamCommand.register();
    }

    public static List<Keyframe> getKeyframes() {
        return keyframes;
    }

    public static void sync(MinecraftServer server) {
        SyncKeyframesPayload payload = new SyncKeyframesPayload(new ArrayList<>(keyframes));
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (player.hasPermissionLevel(2)) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }

    public static void addKeyframe(double x, double y, double z, float yaw, float pitch, float duration) {
        keyframes.add(new Keyframe(x, y, z, yaw, pitch, duration));
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
