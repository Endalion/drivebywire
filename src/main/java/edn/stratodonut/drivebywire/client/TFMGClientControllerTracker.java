package edn.stratodonut.drivebywire.client;

import net.minecraft.core.BlockPos;

public final class TFMGClientControllerTracker {

    private static BlockPos current;

    public static void set(BlockPos pos) {
        current = pos;
    }

    public static void clear() {
        current = null;
    }

    public static BlockPos get() {
        return current;
    }
}
