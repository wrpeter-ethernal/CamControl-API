package dev.peter.client;

import dev.peter.util.Keyframe;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class CinematicManager {

    private static List<Keyframe> currentPath = null;
    private static List<Keyframe> editPath = null;
    private static boolean active = false;
    private static boolean isometric = false;
    private static long startTime = 0;
    private static float totalDuration = 0;

    public static void setIsometric(boolean value) {
        isometric = value;
    }

    public static boolean isIsometric() {
        return isometric;
    }

    public static void setEditPath(List<Keyframe> path) {
        editPath = path;
    }

    public static List<Keyframe> getEditPath() {
        return editPath;
    }

    public static void start(List<Keyframe> path) {
        currentPath = path;
        startTime = System.currentTimeMillis();
        active = true;
        totalDuration = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            totalDuration += path.get(i).duration();
        }
    }

    public static void stop() {
        active = false;
        currentPath = null;
    }

    public static boolean isActive() {
        return active;
    }

    public static CinematicState tick(float tickDelta) {
        if (!active || currentPath == null || currentPath.size() < 2) {
            stop();
            return null;
        }

        float elapsed = (System.currentTimeMillis() - startTime) / 1000.0f;

        if (elapsed >= totalDuration) {
            stop();
            return null;
        }

        return getPathState(elapsed, tickDelta);
    }

    private static CinematicState getPathState(float elapsed, float tickDelta) {
        float accumulated = 0;
        for (int i = 0; i < currentPath.size() - 1; i++) {
            float segmentDuration = currentPath.get(i).duration();
            if (elapsed <= accumulated + segmentDuration) {
                float localAlpha = (elapsed - accumulated) / segmentDuration;
                return interpolate(i, localAlpha, tickDelta);
            }
            accumulated += segmentDuration;
        }
        return null;
    }

    private static CinematicState interpolate(int index, float alpha, float tickDelta) {
        Keyframe p1 = currentPath.get(index);
        Keyframe p2 = currentPath.get(index + 1);
        Keyframe p0 = index > 0 ? currentPath.get(index - 1) : p1;
        Keyframe p3 = index < currentPath.size() - 2 ? currentPath.get(index + 2) : p2;

        Vec3d v0 = getTruePos(p0, tickDelta);
        Vec3d v1 = getTruePos(p1, tickDelta);
        Vec3d v2 = getTruePos(p2, tickDelta);
        Vec3d v3 = getTruePos(p3, tickDelta);

        double x = catmullRom(v0.x, v1.x, v2.x, v3.x, alpha);
        double y = catmullRom(v0.y, v1.y, v2.y, v3.y, alpha);
        double z = catmullRom(v0.z, v1.z, v2.z, v3.z, alpha);

        float yaw = lerpAngle(p1.yaw(), p2.yaw(), alpha);
        float pitch = lerpAngle(p1.pitch(), p2.pitch(), alpha);

        float shakeI = lerp(p1.shakeIntensity(), p2.shakeIntensity(), alpha);
        float shakeS = lerp(p1.shakeSpeed(), p2.shakeSpeed(), alpha);

        if (shakeI > 0) {
            double time = System.currentTimeMillis() / 1000.0 * shakeS;
            x += Math.sin(time) * shakeI * 0.1;
            y += Math.cos(time * 1.2) * shakeI * 0.1;
            z += Math.sin(time * 0.7) * shakeI * 0.1;
        }

        if (p1.targetEntityId() != -1) {
            var client = net.minecraft.client.MinecraftClient.getInstance();
            if (client.world != null) {
                var target = client.world.getEntityById(p1.targetEntityId());
                if (target != null) {
                    Vec3d targetPos = target.getLerpedPos(tickDelta);
                    double dx = targetPos.x - x;
                    double dy = targetPos.y - y;
                    double dz = targetPos.z - z;
                    double dh = Math.sqrt(dx * dx + dz * dz);
                    yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
                    pitch = (float) -Math.toDegrees(Math.atan2(dy, dh));
                }
            }
        }

        return new CinematicState(x, y, z, yaw, pitch);
    }

    private static Vec3d getTruePos(Keyframe k, float tickDelta) {
        if (k.targetEntityId() != -1 && k.orbital()) {
            var client = net.minecraft.client.MinecraftClient.getInstance();
            if (client.world != null) {
                var target = client.world.getEntityById(k.targetEntityId());
                if (target != null) {
                    return target.getLerpedPos(tickDelta).add(k.x(), k.y(), k.z());
                }
            }
        }
        return k.getPos();
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static double catmullRom(double p0, double p1, double p2, double p3, float t) {
        return 0.5 * ((2 * p1) +
                (-p0 + p2) * t +
                (2 * p0 - 5 * p1 + 4 * p2 - p3) * t * t +
                (-p0 + 3 * p1 - 3 * p2 + p3) * t * t * t);
    }

    private static float lerpAngle(float start, float end, float t) {
        float diff = ((end - start + 180) % 360 + 360) % 360 - 180;
        return start + diff * t;
    }

    public record CinematicState(double x, double y, double z, float yaw, float pitch) {
    }
}
