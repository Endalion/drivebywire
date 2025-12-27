package edn.stratodonut.drivebywire.compat;

import edn.stratodonut.drivebywire.wire.ShipWireNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class TFMGEngineControllerWireServerHandler {

    public static final String STEER_LEFT  = "tfmg_steer_left";
    public static final String STEER_RIGHT = "tfmg_steer_right";

    public static final String ENGINE_STARTED = "tfmg_engine_started";

    public static final String SHIFT_REVERSE = "tfmg_shift_reverse";
    public static final String SHIFT_NEUTRAL = "tfmg_shift_neutral";
    public static final String SHIFT_1 = "tfmg_shift_1";
    public static final String SHIFT_2 = "tfmg_shift_2";
    public static final String SHIFT_3 = "tfmg_shift_3";
    public static final String SHIFT_4 = "tfmg_shift_4";
    public static final String SHIFT_5 = "tfmg_shift_5";
    public static final String SHIFT_6 = "tfmg_shift_6";

    public static final String PEDAL_CLUTCH = "tfmg_pedal_clutch";
    public static final String PEDAL_BRAKE = "tfmg_pedal_brake";
    public static final String PEDAL_GAS = "tfmg_pedal_gas";

    public static void set(Level level, BlockPos pos, String channel, boolean active) {
        ShipWireNetworkManager.trySetSignalAt(
                level,
                pos,
                channel,
                active ? 15 : 0
        );
    }

    public static void reset(Level level, BlockPos pos) {
        set(level, pos, STEER_LEFT, false);
        set(level, pos, STEER_RIGHT, false);

        set(level, pos, SHIFT_REVERSE, false);
        set(level, pos, SHIFT_NEUTRAL, false);
        set(level, pos, SHIFT_1, false);
        set(level, pos, SHIFT_2, false);
        set(level, pos, SHIFT_3, false);
        set(level, pos, SHIFT_4, false);
        set(level, pos, SHIFT_5, false);
        set(level, pos, SHIFT_6, false);

        set(level, pos, PEDAL_CLUTCH, false);
        set(level, pos, PEDAL_BRAKE, false);
        set(level, pos, PEDAL_GAS, false);
    }

}
