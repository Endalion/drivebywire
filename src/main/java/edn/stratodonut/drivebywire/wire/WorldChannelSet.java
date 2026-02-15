package edn.stratodonut.drivebywire.wire;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.annotation.Nonnull;

public class WorldChannelSet implements IChannelSet {

    @JsonIgnore
    public static final String WORLD_REDSTONE_CHANNEL = "world";

    public static IChannelSet INSTANCE = new WorldChannelSet();

    private WorldChannelSet() {}

    @Nonnull
    @Override
    public String currentChannel() {
        return WORLD_REDSTONE_CHANNEL;
    }

    @Nonnull
    @Override
    public String nextChannel() {
        return WORLD_REDSTONE_CHANNEL;
    }

    @Nonnull
    @Override
    public String previousChannel() {
        return WORLD_REDSTONE_CHANNEL;
    }
}
