package dev.peter.util;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.Vec3d;

public record Keyframe(double x, double y, double z, float yaw, float pitch, float duration) {

    public static final PacketCodec<RegistryByteBuf, Keyframe> CODEC = PacketCodec.tuple(
            PacketCodecs.DOUBLE, Keyframe::x,
            PacketCodecs.DOUBLE, Keyframe::y,
            PacketCodecs.DOUBLE, Keyframe::z,
            PacketCodecs.FLOAT, Keyframe::yaw,
            PacketCodecs.FLOAT, Keyframe::pitch,
            PacketCodecs.FLOAT, Keyframe::duration,
            Keyframe::new
    );

    public Vec3d getPos() {
        return new Vec3d(x, y, z);
    }
}
