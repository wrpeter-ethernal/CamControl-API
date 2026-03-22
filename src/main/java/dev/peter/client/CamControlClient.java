package dev.peter.client;

import dev.peter.network.StartCinematicPayload;
import dev.peter.network.StopCinematicPayload;
import dev.peter.network.SyncKeyframesPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class CamControlClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(StartCinematicPayload.ID, (payload, context) -> {
            context.client().execute(() -> CinematicManager.start(payload.keyframes()));
        });

        ClientPlayNetworking.registerGlobalReceiver(StopCinematicPayload.ID, (payload, context) -> {
            context.client().execute(CinematicManager::stop);
        });

        ClientPlayNetworking.registerGlobalReceiver(SyncKeyframesPayload.ID, (payload, context) -> {
            context.client().execute(() -> CinematicManager.setEditPath(payload.keyframes()));
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            KeyframeRenderer.render(context);
        });
    }
}
