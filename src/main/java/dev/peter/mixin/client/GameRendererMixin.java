package dev.peter.mixin.client;

import dev.peter.client.CinematicManager;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    private void hideHand(Camera camera, float tickDelta, Matrix4f matrix, CallbackInfo ci) {
        if (CinematicManager.isActive()) {
            ci.cancel();
        }
    }
}
