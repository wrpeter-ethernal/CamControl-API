package dev.peter.network;

import dev.peter.util.Keyframe;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.List;

public record SyncKeyframesPayload(List<Keyframe> keyframes) implements CustomPayload {

    public static final Id<SyncKeyframesPayload> ID = new Id<>(Identifier.of("camcontrol", "sync_keyframes"));

    public static final PacketCodec<RegistryByteBuf, SyncKeyframesPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.collection(java.util.ArrayList::new, Keyframe.CODEC), SyncKeyframesPayload::keyframes,
            SyncKeyframesPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
