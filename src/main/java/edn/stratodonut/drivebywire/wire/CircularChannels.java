package edn.stratodonut.drivebywire.wire;

import javax.annotation.Nonnull;
import java.util.List;

public class CircularChannels implements IChannelSet {
    private final List<String> channels;
    public int current = 0;

    public CircularChannels(List<String> channels) {
        this.channels = channels;
    }

    @Nonnull
    public String currentChannel() {
        return channels.get(current);
    }

    @Nonnull
    public String nextChannel() {
        current = ++current % channels.size();
        return currentChannel();
    }

    @Nonnull
    public String previousChannel() {
        current = Math.floorMod(--current, channels.size());
        return currentChannel();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CircularChannels)) return false;
        return ((CircularChannels) obj).channels.equals(this.channels);
    }
}
