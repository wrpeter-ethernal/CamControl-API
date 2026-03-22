package dev.peter.network;

import dev.peter.CamControl;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record SetModePayload(boolean isometric) implements CustomPayload {
    public static final Id<SetModePayload> ID = new Id<>(CamControl.id("set_mode"));
    public static final PacketCodec<RegistryByteBuf, SetModePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL, SetModePayload::isometric,
            SetModePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
