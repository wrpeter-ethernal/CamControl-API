package dev.peter.mixin.client;

import dev.peter.client.CinematicManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    private void hideHand(Camera camera, float tickDelta, Matrix4f matrix, CallbackInfo ci) {
        if (CinematicManager.isActive()) {
            ci.cancel();
        }
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void disableBobView(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (CinematicManager.isActive()) {
            ci.cancel();
        }
    }

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void lockFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
        if (CinematicManager.isActive()) {
            cir.setReturnValue((double) this.client.options.getFov().getValue());
        }
    }
}
