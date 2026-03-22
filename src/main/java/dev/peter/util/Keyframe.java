package dev.peter.util;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.math.Vec3d;

public record Keyframe(double x, double y, double z, float yaw, float pitch, float duration, float shakeIntensity, float shakeSpeed, int targetEntityId, boolean orbital) {

    public static final PacketCodec<RegistryByteBuf, Keyframe> CODEC = new PacketCodec<>() {
        @Override
        public Keyframe decode(RegistryByteBuf buf) {
            return new Keyframe(
                buf.readDouble(), buf.readDouble(), buf.readDouble(),
                buf.readFloat(), buf.readFloat(), buf.readFloat(),
                buf.readFloat(), buf.readFloat(), buf.readInt(), buf.readBoolean()
            );
        }

        @Override
        public void encode(RegistryByteBuf buf, Keyframe value) {
            buf.writeDouble(value.x);
            buf.writeDouble(value.y);
            buf.writeDouble(value.z);
            buf.writeFloat(value.yaw);
            buf.writeFloat(value.pitch);
            buf.writeFloat(value.duration);
            buf.writeFloat(value.shakeIntensity);
            buf.writeFloat(value.shakeSpeed);
            buf.writeInt(value.targetEntityId);
            buf.writeBoolean(value.orbital);
        }
    };

    public Vec3d getPos() {
        return new Vec3d(x, y, z);
    }
}
