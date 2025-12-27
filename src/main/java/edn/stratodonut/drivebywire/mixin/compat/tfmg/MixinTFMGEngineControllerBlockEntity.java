package edn.stratodonut.drivebywire.mixin.compat.tfmg;

import com.drmangotea.tfmg.content.engines.engine_controller.EngineControllerBlockEntity;
import com.drmangotea.tfmg.content.engines.upgrades.TransmissionUpgrade.TransmissionState;
import edn.stratodonut.drivebywire.compat.TFMGEngineControllerWireServerHandler;
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

        var pos = self.getBlockPos();

        boolean steerLeft  = self.isPressed(2);
        boolean steerRight = self.isPressed(3);

        boolean engineStarted = self.engineStarted;

        boolean shiftReverse = self.shift == TransmissionState.REVERSE;
        boolean shiftNeutral = self.shift == TransmissionState.NEUTRAL;
        boolean shift1 = self.shift == TransmissionState.SHIFT_1;
        boolean shift2 = self.shift == TransmissionState.SHIFT_2;
        boolean shift3 = self.shift == TransmissionState.SHIFT_3;
        boolean shift4 = self.shift == TransmissionState.SHIFT_4;
        boolean shift5 = self.shift == TransmissionState.SHIFT_5;
        boolean shift6 = self.shift == TransmissionState.SHIFT_6;

        boolean pedalClutch = self.clutch;
        boolean pedalBrake = self.brake;
        boolean pedalGas = self.gas;

        TFMGEngineControllerWireServerHandler.set(level, pos, TFMGEngineControllerWireServerHandler.STEER_LEFT, steerLeft);
        TFMGEngineControllerWireServerHandler.set(level, pos, TFMGEngineControllerWireServerHandler.STEER_RIGHT, steerRight);

        TFMGEngineControllerWireServerHandler.set(level, pos, TFMGEngineControllerWireServerHandler.ENGINE_STARTED, engineStarted);

        TFMGEngineControllerWireServerHandler.set(level, pos, TFMGEngineControllerWireServerHandler.SHIFT_REVERSE, shiftReverse);
        TFMGEngineControllerWireServerHandler.set(level, pos, TFMGEngineControllerWireServerHandler.SHIFT_NEUTRAL, shiftNeutral);
        TFMGEngineControllerWireServerHandler.set(level, pos, TFMGEngineControllerWireServerHandler.SHIFT_1, shift1);
        TFMGEngineControllerWireServerHandler.set(level, pos, TFMGEngineControllerWireServerHandler.SHIFT_2, shift2);
        TFMGEngineControllerWireServerHandler.set(level, pos, TFMGEngineControllerWireServerHandler.SHIFT_3, shift3);
        TFMGEngineControllerWireServerHandler.set(level, pos, TFMGEngineControllerWireServerHandler.SHIFT_4, shift4);
        TFMGEngineControllerWireServerHandler.set(level, pos, TFMGEngineControllerWireServerHandler.SHIFT_5, shift5);
        TFMGEngineControllerWireServerHandler.set(level, pos, TFMGEngineControllerWireServerHandler.SHIFT_6, shift6);

        TFMGEngineControllerWireServerHandler.set(level, pos, TFMGEngineControllerWireServerHandler.PEDAL_CLUTCH, pedalClutch);
        TFMGEngineControllerWireServerHandler.set(level, pos, TFMGEngineControllerWireServerHandler.PEDAL_BRAKE, pedalBrake);
        TFMGEngineControllerWireServerHandler.set(level, pos, TFMGEngineControllerWireServerHandler.PEDAL_GAS, pedalGas);
    }

    @Inject(method = "remove", at = @At("HEAD"), remap = false)
    private void drivebywire$reset(CallbackInfo ci) {
        EngineControllerBlockEntity self = (EngineControllerBlockEntity)(Object)this;
        Level level = self.getLevel();
        if (level != null && !level.isClientSide) {
            TFMGEngineControllerWireServerHandler.reset(level, self.getBlockPos());
        }
    }
}
