package dev.peter.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import dev.peter.CamControl;

public record ShakePayload(float intensity, float speed, float duration) implements CustomPayload {
    public static final Id<ShakePayload> ID = new Id<>(CamControl.id("shake"));
    public static final PacketCodec<RegistryByteBuf, ShakePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.FLOAT, ShakePayload::intensity,
            PacketCodecs.FLOAT, ShakePayload::speed,
            PacketCodecs.FLOAT, ShakePayload::duration,
            ShakePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
