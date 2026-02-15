package edn.stratodonut.drivebywire.server;

import net.minecraft.core.BlockPos;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TFMGSteeringInputCache {

    private static final Map<BlockPos, State> CACHE = new ConcurrentHashMap<>();

    public static void update(BlockPos pos, boolean left, boolean right) {
        CACHE.put(pos, new State(left, right));
    }

    public static State get(BlockPos pos) {
        return CACHE.getOrDefault(pos, State.NONE);
    }

    public static void clear(BlockPos pos) {
        CACHE.remove(pos);
    }

    public record State(boolean left, boolean right) {
        public static final State NONE = new State(false, false);
    }
}
