package edn.stratodonut.drivebywire.mixin.compat.tweaked;

import com.getitemfromblock.create_tweaked_controllers.block.TweakedLecternControllerBlock;
import edn.stratodonut.drivebywire.wire.CircularChannels;
import edn.stratodonut.drivebywire.wire.IChannelSet;
import edn.stratodonut.drivebywire.wire.MultiChannelWireSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.stream.Stream;

import static edn.stratodonut.drivebywire.compat.TweakedControllerWireServerHandler.AXIS_TO_CHANNEL;
import static edn.stratodonut.drivebywire.compat.TweakedControllerWireServerHandler.BUTTON_TO_CHANNEL;

@Pseudo
@Mixin(TweakedLecternControllerBlock.class)
public abstract class MixinLecternTweakedBlock implements MultiChannelWireSource {
    @Unique
    private static final IChannelSet wire$channels =
            new CircularChannels(Stream.concat(Arrays.stream(AXIS_TO_CHANNEL), Arrays.stream(BUTTON_TO_CHANNEL)).toList());

    @Nonnull
    @Override
    public IChannelSet wire$getChannelSet() {
        return wire$channels;
    }
}
