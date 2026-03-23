package dev.peter.mixin.client;

import dev.peter.client.CinematicManager;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
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
        Vec3d shake = CinematicManager.getShakeOffset();
        if (CinematicManager.isActive()) {
            CinematicManager.CinematicState state = CinematicManager.tick(tickDelta);
            if (state != null) {
                setPos(state.x() + shake.x, state.y() + shake.y, state.z() + shake.z);
                setRotation(state.yaw(), state.pitch());
            }
        } else if (!shake.equals(Vec3d.ZERO)) {
            Camera camera = (Camera) (Object) this;
            Vec3d pos = camera.getPos();
            setPos(pos.x + shake.x, pos.y + shake.y, pos.z + shake.z);
        }
    }

    @Inject(method = "isThirdPerson", at = @At("HEAD"), cancellable = true)
    private void forceThirdPerson(CallbackInfoReturnable<Boolean> cir) {
        if (CinematicManager.isActive()) {
            cir.setReturnValue(true);
        }
    }
}
