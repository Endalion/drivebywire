package edn.stratodonut.drivebywire.wire;

import javax.annotation.Nonnull;

public interface MultiChannelWireSource {
    @Nonnull
    IChannelSet wire$getChannelSet();
}
