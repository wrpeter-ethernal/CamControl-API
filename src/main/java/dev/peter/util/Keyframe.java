package dev.peter.util;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.math.Vec3d;

public record Keyframe(double x, double y, double z, float yaw, float pitch, float duration, float shakeIntensity, float shakeSpeed, int targetEntityId) {

    public static final PacketCodec<RegistryByteBuf, Keyframe> CODEC = new PacketCodec<>() {
        @Override
        public void encode(RegistryByteBuf buf, Keyframe keyframe) {
            buf.writeDouble(keyframe.x());
            buf.writeDouble(keyframe.y());
            buf.writeDouble(keyframe.z());
            buf.writeFloat(keyframe.yaw());
            buf.writeFloat(keyframe.pitch());
            buf.writeFloat(keyframe.duration());
            buf.writeFloat(keyframe.shakeIntensity());
            buf.writeFloat(keyframe.shakeSpeed());
            buf.writeInt(keyframe.targetEntityId());
        }

        @Override
        public Keyframe decode(RegistryByteBuf buf) {
            return new Keyframe(
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readInt()
            );
        }
    };

    public Vec3d getPos() {
        return new Vec3d(x, y, z);
    }
}
