package dev.peter.mixin.client;

import dev.peter.client.CinematicManager;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {

    @Inject(method = "updateMouse", at = @At("HEAD"), cancellable = true)
    private void lockMouse(CallbackInfo ci) {
        if (CinematicManager.isActive()) {
            ci.cancel();
        }
    }
}
