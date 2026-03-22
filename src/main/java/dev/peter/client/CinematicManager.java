package dev.peter.client;

import dev.peter.util.Keyframe;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class CinematicManager {

    private static List<Keyframe> currentPath = null;
    private static List<Keyframe> editPath = null;
    private static boolean active = false;
    private static long startTime = 0;
    private static float totalDuration = 0;

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

    public static CinematicState tick() {
        if (!active || currentPath == null || currentPath.size() < 2) {
            stop();
            return null;
        }

        float elapsed = (System.currentTimeMillis() - startTime) / 1000.0f;

        if (elapsed >= totalDuration) {
            stop();
            return null;
        }

        return getPathState(elapsed);
    }

    private static CinematicState getPathState(float elapsed) {
        float accumulated = 0;
        for (int i = 0; i < currentPath.size() - 1; i++) {
            float segmentDuration = currentPath.get(i).duration();
            if (elapsed <= accumulated + segmentDuration) {
                float localAlpha = (elapsed - accumulated) / segmentDuration;
                return interpolate(i, localAlpha);
            }
            accumulated += segmentDuration;
        }
        return null;
    }

    private static CinematicState interpolate(int index, float alpha) {
        Keyframe p1 = currentPath.get(index);
        Keyframe p2 = currentPath.get(index + 1);
        Keyframe p0 = index > 0 ? currentPath.get(index - 1) : p1;
        Keyframe p3 = index < currentPath.size() - 2 ? currentPath.get(index + 2) : p2;

        Vec3d v0 = p0.getPos();
        Vec3d v1 = p1.getPos();
        Vec3d v2 = p2.getPos();
        Vec3d v3 = p3.getPos();

        double x = catmullRom(v0.x, v1.x, v2.x, v3.x, alpha);
        double y = catmullRom(v0.y, v1.y, v2.y, v3.y, alpha);
        double z = catmullRom(v0.z, v1.z, v2.z, v3.z, alpha);

        float yaw = lerpAngle(p1.yaw(), p2.yaw(), alpha);
        float pitch = lerpAngle(p1.pitch(), p2.pitch(), alpha);

        return new CinematicState(x, y, z, yaw, pitch);
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
