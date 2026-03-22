package dev.peter.client;

import dev.peter.util.Keyframe;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.List;

public class KeyframeRenderer {

    public static void render(WorldRenderContext context) {
        List<Keyframe> path = CinematicManager.getEditPath();
        if (path == null || path.isEmpty()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || !client.player.hasPermissionLevel(2)) return;

        MatrixStack matrices = context.matrixStack();
        Vec3d cameraPos = context.camera().getPos();
        VertexConsumerProvider consumers = context.consumers();
        if (consumers == null) return;

        VertexConsumer buffer = consumers.getBuffer(RenderLayer.getLines());

        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        if (path.size() >= 2) {
            renderPathSpline(buffer, matrices, path);
        }

        for (int i = 0; i < path.size(); i++) {
            Keyframe k = path.get(i);
            matrices.push();
            matrices.translate(k.x(), k.y(), k.z());
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - k.yaw()));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(k.pitch()));

            renderCameraIndicator(buffer, matrices.peek().getPositionMatrix(), i == 0);

            matrices.pop();
        }

        matrices.pop();
    }

    private static void renderPathSpline(VertexConsumer buffer, MatrixStack matrices, List<Keyframe> path) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        Vec3d lastPoint = null;

        for (int i = 0; i < path.size() - 1; i++) {
            for (float t = 0; t <= 1.0f; t += 0.05f) {
                Vec3d pos = interpolatePos(path, i, t);
                if (lastPoint != null) {
                    float alpha = (t * 20 % 2 < 1) ? 1.0f : 0.2f;
                    buffer.vertex(matrix, (float) lastPoint.x, (float) lastPoint.y, (float) lastPoint.z).color(1.0f, 1.0f, 0.0f, alpha).normal(0, 1, 0);
                    buffer.vertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z).color(1.0f, 1.0f, 0.0f, alpha).normal(0, 1, 0);
                }
                lastPoint = pos;
            }
        }
    }

    private static Vec3d interpolatePos(List<Keyframe> path, int index, float alpha) {
        Keyframe p1 = path.get(index);
        Keyframe p2 = path.get(index + 1);
        Keyframe p0 = index > 0 ? path.get(index - 1) : p1;
        Keyframe p3 = index < path.size() - 2 ? path.get(index + 2) : p2;

        return new Vec3d(
            catmullRom(p0.x(), p1.x(), p2.x(), p3.x(), alpha),
            catmullRom(p0.y(), p1.y(), p2.y(), p3.y(), alpha),
            catmullRom(p0.z(), p1.z(), p2.z(), p3.z(), alpha)
        );
    }

    private static double catmullRom(double p0, double p1, double p2, double p3, float t) {
        return 0.5 * ((2 * p1) +
                (-p0 + p2) * t +
                (2 * p0 - 5 * p1 + 4 * p2 - p3) * t * t +
                (-p0 + 3 * p1 - 3 * p2 + p3) * t * t * t);
    }

    private static void renderCameraIndicator(VertexConsumer buffer, Matrix4f matrix, boolean isFirst) {
        float w = 0.5f, h = 0.35f, d = 0.7f;
        float r = 1.0f, g = 1.0f, b = 0.0f;
        if (isFirst) g = 0.5f;

        line(buffer, matrix, -w, -h, -d, w, -h, -d, r, g, b);
        line(buffer, matrix, w, -h, -d, w, h, -d, r, g, b);
        line(buffer, matrix, w, h, -d, -w, h, -d, r, g, b);
        line(buffer, matrix, -w, h, -d, -w, -h, -d, r, g, b);

        line(buffer, matrix, 0, 0, 0, -w, -h, -d, r, g, b);
        line(buffer, matrix, 0, 0, 0, w, -h, -d, r, g, b);
        line(buffer, matrix, 0, 0, 0, w, h, -d, r, g, b);
        line(buffer, matrix, 0, 0, 0, -w, h, -d, r, g, b);

        line(buffer, matrix, -w, h, -d, 0, h + 0.2f, -d, r, g, b);
        line(buffer, matrix, w, h, -d, 0, h + 0.2f, -d, r, g, b);
    }

    private static void line(VertexConsumer buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b) {
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, 1.0f).normal(0, 1, 0);
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, 1.0f).normal(0, 1, 0);
    }
}
