package edn.stratodonut.drivebywire.compat;

import net.minecraft.core.BlockPos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TFMGSteeringState {

    private static final Map<BlockPos, Float> STEER  = new ConcurrentHashMap<>();

    public static void set(BlockPos pos, float steer) {
        STEER.put(pos, steer);
    }

    public static float getSteer(BlockPos pos) {
        return STEER.getOrDefault(pos, 0f);
    }

    public static void clear(BlockPos pos) {
        STEER.remove(pos);
    }
}
