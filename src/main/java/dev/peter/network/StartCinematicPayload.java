package dev.peter.network;

import dev.peter.util.Keyframe;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.List;

public record StartCinematicPayload(List<Keyframe> keyframes) implements CustomPayload {

    public static final Id<StartCinematicPayload> ID = new Id<>(Identifier.of("camcontrol", "start_cinematic"));

    public static final PacketCodec<RegistryByteBuf, StartCinematicPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.collection(java.util.ArrayList::new, Keyframe.CODEC), StartCinematicPayload::keyframes,
            StartCinematicPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
