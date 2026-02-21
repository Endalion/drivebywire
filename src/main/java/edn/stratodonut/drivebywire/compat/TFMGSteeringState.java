package edn.stratodonut.drivebywire.compat;

import net.minecraft.core.BlockPos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TFMGSteeringState {

    private static final Map<BlockPos, Boolean> LEFT  = new ConcurrentHashMap<>();
    private static final Map<BlockPos, Boolean> RIGHT = new ConcurrentHashMap<>();

    public static void set(BlockPos pos, boolean left, boolean right) {
        LEFT.put(pos, left);
        RIGHT.put(pos, right);
    }

    public static boolean isLeft(BlockPos pos) {
        return LEFT.getOrDefault(pos, false);
    }

    public static boolean isRight(BlockPos pos) {
        return RIGHT.getOrDefault(pos, false);
    }

    public static void clear(BlockPos pos) {
        LEFT.remove(pos);
        RIGHT.remove(pos);
    }
}
