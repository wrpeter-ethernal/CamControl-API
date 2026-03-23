package dev.peter.mixin.client;

import dev.peter.client.CinematicManager;
import net.minecraft.client.render.Camera;
import net.minecraft.world.BlockView;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow protected abstract void setPos(double x, double y, double z);
    @Shadow protected abstract void setRotation(float yaw, float pitch);

    @Inject(method = "update", at = @At("TAIL"))
    private void overrideCamera(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        float shakeYaw = CinematicManager.getShakeYaw(tickDelta);
        float shakePitch = CinematicManager.getShakePitch(tickDelta);
        
        if (CinematicManager.isActive()) {
            CinematicManager.CinematicState state = CinematicManager.tick(tickDelta);
            if (state != null) {
                setPos(state.x(), state.y(), state.z());
                setRotation(state.yaw() + shakeYaw, state.pitch() + shakePitch);
            }
        } else if (shakeYaw != 0 || shakePitch != 0) {
            Camera camera = (Camera) (Object) this;
            setRotation(camera.getYaw() + shakeYaw, camera.getPitch() + shakePitch);
        }
    }

    @Inject(method = "isThirdPerson", at = @At("HEAD"), cancellable = true)
    private void forceThirdPerson(CallbackInfoReturnable<Boolean> cir) {
        if (CinematicManager.isActive()) {
            cir.setReturnValue(true);
        }
    }
}
