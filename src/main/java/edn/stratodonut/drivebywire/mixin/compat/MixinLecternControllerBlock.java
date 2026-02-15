package edn.stratodonut.drivebywire.mixin.compat;

import com.simibubi.create.content.redstone.link.controller.LecternControllerBlock;
import edn.stratodonut.drivebywire.wire.CircularChannels;
import edn.stratodonut.drivebywire.wire.IChannelSet;
import edn.stratodonut.drivebywire.wire.MultiChannelWireSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nonnull;
import java.util.Arrays;

import static edn.stratodonut.drivebywire.compat.LinkedControllerWireServerHandler.KEY_TO_CHANNEL;

@Mixin(LecternControllerBlock.class)
public abstract class MixinLecternControllerBlock implements MultiChannelWireSource {
    @Unique
    private static final IChannelSet wire$channels = new CircularChannels(Arrays.stream(KEY_TO_CHANNEL).toList());

    @Nonnull
    @Override
    public IChannelSet wire$getChannelSet() {
        return wire$channels;
    }
}
