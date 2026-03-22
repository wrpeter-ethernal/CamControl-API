package dev.peter.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record StopCinematicPayload() implements CustomPayload {

    public static final Id<StopCinematicPayload> ID = new Id<>(Identifier.of("camcontrol", "stop_cinematic"));

    public static final PacketCodec<RegistryByteBuf, StopCinematicPayload> CODEC = PacketCodec.unit(new StopCinematicPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
