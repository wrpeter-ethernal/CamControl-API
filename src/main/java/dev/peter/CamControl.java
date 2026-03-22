package dev.peter;

import dev.peter.network.StartCinematicPayload;
import dev.peter.network.StopCinematicPayload;
import dev.peter.util.Keyframe;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

import java.util.ArrayList;
import java.util.List;

public class CamControl implements ModInitializer {

    private static final List<Keyframe> keyframes = new ArrayList<>();

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(StartCinematicPayload.ID, StartCinematicPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(StopCinematicPayload.ID, StopCinematicPayload.CODEC);

        CamCommand.register();
    }

    public static List<Keyframe> getKeyframes() {
        return keyframes;
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
