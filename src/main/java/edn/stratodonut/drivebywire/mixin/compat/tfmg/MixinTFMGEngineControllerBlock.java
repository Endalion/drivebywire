package edn.stratodonut.drivebywire.mixin.compat.tfmg;

import com.drmangotea.tfmg.content.engines.engine_controller.EngineControllerBlock;
import edn.stratodonut.drivebywire.compat.TFMGEngineControllerWireServerHandler;
import edn.stratodonut.drivebywire.wire.MultiChannelWireSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import javax.annotation.Nonnull;
import java.util.List;

@Pseudo
@Mixin(EngineControllerBlock.class)
public abstract class MixinTFMGEngineControllerBlock implements MultiChannelWireSource {

    @Unique
    private static final List<String> wire$channels = List.of(
            TFMGEngineControllerWireServerHandler.STEER_LEFT,
            TFMGEngineControllerWireServerHandler.STEER_RIGHT,

            TFMGEngineControllerWireServerHandler.ENGINE_STARTED,

            TFMGEngineControllerWireServerHandler.SHIFT_REVERSE,
            TFMGEngineControllerWireServerHandler.SHIFT_NEUTRAL,
            TFMGEngineControllerWireServerHandler.SHIFT_1,
            TFMGEngineControllerWireServerHandler.SHIFT_2,
            TFMGEngineControllerWireServerHandler.SHIFT_3,
            TFMGEngineControllerWireServerHandler.SHIFT_4,
            TFMGEngineControllerWireServerHandler.SHIFT_5,
            TFMGEngineControllerWireServerHandler.SHIFT_6,

            TFMGEngineControllerWireServerHandler.PEDAL_CLUTCH,
            TFMGEngineControllerWireServerHandler.PEDAL_BRAKE,
            TFMGEngineControllerWireServerHandler.PEDAL_GAS
    );

    @Override
    public List<String> wire$getChannels() {
        return wire$channels;
    }

    @Override
    public @Nonnull String wire$nextChannel(String current, boolean forward) {
        int i = wire$channels.indexOf(current);
        if (i == -1) return wire$channels.get(0);
        return wire$channels.get(
                Math.floorMod(i + (forward ? 1 : -1), wire$channels.size())
        );
    }
}
