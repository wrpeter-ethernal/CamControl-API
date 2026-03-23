package dev.peter.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import dev.peter.CamControl;

public record ShakePayload(boolean active, float intensity) implements CustomPayload {
    public static final Id<ShakePayload> ID = new Id<>(CamControl.id("shake_v2"));
    public static final PacketCodec<RegistryByteBuf, ShakePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL, ShakePayload::active,
            PacketCodecs.FLOAT, ShakePayload::intensity,
            ShakePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
