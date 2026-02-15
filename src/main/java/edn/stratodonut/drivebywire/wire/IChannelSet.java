package edn.stratodonut.drivebywire.wire;

import javax.annotation.Nonnull;

public interface IChannelSet {
    @Nonnull
    String currentChannel();
    @Nonnull
    String nextChannel();
    @Nonnull
    String previousChannel();
}
