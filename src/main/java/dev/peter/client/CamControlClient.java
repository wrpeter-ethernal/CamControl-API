package dev.peter.client;

import dev.peter.network.ShakePayload;
import dev.peter.network.StartCinematicPayload;
import dev.peter.network.StopCinematicPayload;
import dev.peter.network.SyncKeyframesPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
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

        ClientPlayNetworking.registerGlobalReceiver(ShakePayload.ID, (payload, context) -> {
            context.client().execute(() -> CinematicManager.setContinuousShake(payload.active(), payload.intensity()));
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            CinematicManager.clientTick();
        });

        // test

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            KeyframeRenderer.render(context);
        });
    }
}
