package dev.peter.mixin.client;

import dev.peter.client.CinematicManager;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void blockMovement(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (CinematicManager.isActive()) {
            if (key == 87 || key == 65 || key == 83 || key == 68 || key == 32 || key == 340) {
                ci.cancel();
            }
        }
    }
}
