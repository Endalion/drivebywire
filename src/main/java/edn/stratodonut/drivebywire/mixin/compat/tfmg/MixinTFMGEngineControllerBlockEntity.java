package edn.stratodonut.drivebywire.mixin.compat.tfmg;

import com.drmangotea.tfmg.content.engines.engine_controller.EngineControllerBlockEntity;
import com.drmangotea.tfmg.content.engines.upgrades.TransmissionUpgrade.TransmissionState;
import edn.stratodonut.drivebywire.compat.TFMGEngineControllerWireServerHandler;
import edn.stratodonut.drivebywire.compat.TFMGSteeringState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(EngineControllerBlockEntity.class)
public abstract class MixinTFMGEngineControllerBlockEntity {

    @Inject(method = "tick", at = @At("TAIL"), remap = false)
    private void drivebywire$emitSignals(CallbackInfo ci) {
        EngineControllerBlockEntity self = (EngineControllerBlockEntity)(Object)this;
        Level level = self.getLevel();
        if (level == null || level.isClientSide) return;

        BlockPos pos = self.getBlockPos();

        boolean steerLeft  = TFMGSteeringState.isLeft(pos);
        boolean steerRight = TFMGSteeringState.isRight(pos);

        TFMGEngineControllerWireServerHandler.set(level, pos, TFMGEngineControllerWireServerHandler.STEER_LEFT, steerLeft);
        TFMGEngineControllerWireServerHandler.set(level, pos, TFMGEngineControllerWireServerHandler.STEER_RIGHT, steerRight);

        TFMGEngineControllerWireServerHandler.set(level, pos, TFMGEngineControllerWireServerHandler.ENGINE_STARTED, self.engineStarted);

        TransmissionState s = self.shift;
        TFMGEngineControllerWireServerHandler.set(level, pos, TFMGEngineControllerWireServerHandler.SHIFT_REVERSE, s == TransmissionState.REVERSE);
        TFMGEngineControllerWireServerHandler.set(level, pos, TFMGEngineControllerWireServerHandler.SHIFT_NEUTRAL, s == TransmissionState.NEUTRAL);
        TFMGEngineControllerWireServerHandler.set(level, pos, TFMGEngineControllerWireServerHandler.SHIFT_1, s == TransmissionState.SHIFT_1);
        TFMGEngineControllerWireServerHandler.set(level, pos, TFMGEngineControllerWireServerHandler.SHIFT_2, s == TransmissionState.SHIFT_2);
        TFMGEngineControllerWireServerHandler.set(level, pos, TFMGEngineControllerWireServerHandler.SHIFT_3, s == TransmissionState.SHIFT_3);
        TFMGEngineControllerWireServerHandler.set(level, pos, TFMGEngineControllerWireServerHandler.SHIFT_4, s == TransmissionState.SHIFT_4);
        TFMGEngineControllerWireServerHandler.set(level, pos, TFMGEngineControllerWireServerHandler.SHIFT_5, s == TransmissionState.SHIFT_5);
        TFMGEngineControllerWireServerHandler.set(level, pos, TFMGEngineControllerWireServerHandler.SHIFT_6, s == TransmissionState.SHIFT_6);

        TFMGEngineControllerWireServerHandler.set(level, pos, TFMGEngineControllerWireServerHandler.PEDAL_CLUTCH, self.clutch);
        TFMGEngineControllerWireServerHandler.set(level, pos, TFMGEngineControllerWireServerHandler.PEDAL_BRAKE, self.brake);
        TFMGEngineControllerWireServerHandler.set(level, pos, TFMGEngineControllerWireServerHandler.PEDAL_GAS, self.gas);
    }

    @Inject(method = "remove", at = @At("HEAD"), remap = false)
    private void drivebywire$cleanup(CallbackInfo ci) {
        EngineControllerBlockEntity self = (EngineControllerBlockEntity)(Object)this;
        Level level = self.getLevel();
        if (level != null && !level.isClientSide) {
            TFMGSteeringState.clear(self.getBlockPos());
            TFMGEngineControllerWireServerHandler.reset(level, self.getBlockPos());
        }
    }
}
