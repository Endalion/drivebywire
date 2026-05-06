package edn.stratodonut.drivebywire.compat;

import com.mojang.datafixers.util.Pair;
import edn.stratodonut.drivebywire.wire.ShipWireNetworkManager;
import net.createmod.catnip.data.WorldAttached;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.*;

public class TweakedControllerWireServerHandler {
    public static final String[] BUTTON_TO_CHANNEL = {
            "buttonA",
            "buttonB",
            "buttonX",
            "buttonY",
            "shoulderLeft",
            "shoulderRight",
            "buttonBack",
            "buttonStart",
            "buttonGuide",
            "leftJoyStickClick",
            "rightJoyStickClick",
            "dPadUp",
            "dPadRight",
            "dPadDown",
            "dPadLeft"
    };

    public static final String[] AXIS_TO_CHANNEL = {
            "axisLeftX+",
            "axisLeftX-",
            "axisLeftY+",
            "axisLeftY-",

            "axisRightX+",
            "axisRightX-",
            "axisRightY+",
            "axisRightY-",

            "axisTriggerLeft",
            "axisTriggerRight"
    };
    // ==============================================
    // 强绑定：内部名 → 多语言键（一一对应，永不乱序）
    // ==============================================
    public static final Map<String, String> CHANNEL_TO_LANG_KEY = Map.ofEntries(
            Map.entry("world","drivebywire.wire.channel.world"),
            // 按钮映射
            Map.entry("buttonA", "drivebywire.controller.button.a"),
            Map.entry("buttonB", "drivebywire.controller.button.b"),
            Map.entry("buttonX", "drivebywire.controller.button.x"),
            Map.entry("buttonY", "drivebywire.controller.button.y"),
            Map.entry("shoulderLeft", "drivebywire.controller.button.shoulder_left"),
            Map.entry("shoulderRight", "drivebywire.controller.button.shoulder_right"),
            Map.entry("buttonBack", "drivebywire.controller.button.back"),
            Map.entry("buttonStart", "drivebywire.controller.button.start"),
            Map.entry("buttonGuide", "drivebywire.controller.button.guide"),
            Map.entry("leftJoyStickClick", "drivebywire.controller.button.left_joystick_click"),
            Map.entry("rightJoyStickClick", "drivebywire.controller.button.right_joystick_click"),
            Map.entry("dPadUp", "drivebywire.controller.button.dpad_up"),
            Map.entry("dPadRight", "drivebywire.controller.button.dpad_right"),
            Map.entry("dPadDown", "drivebywire.controller.button.dpad_down"),
            Map.entry("dPadLeft", "drivebywire.controller.button.dpad_left"),

            // 轴映射
            Map.entry("axisLeftX+", "drivebywire.controller.axis.left_x_positive"),
            Map.entry("axisLeftX-", "drivebywire.controller.axis.left_x_negative"),
            Map.entry("axisLeftY+", "drivebywire.controller.axis.left_y_positive"),
            Map.entry("axisLeftY-", "drivebywire.controller.axis.left_y_negative"),
            Map.entry("axisRightX+", "drivebywire.controller.axis.right_x_positive"),
            Map.entry("axisRightX-", "drivebywire.controller.axis.right_x_negative"),
            Map.entry("axisRightY+", "drivebywire.controller.axis.right_y_positive"),
            Map.entry("axisRightY-", "drivebywire.controller.axis.right_y_negative"),
            Map.entry("axisTriggerLeft", "drivebywire.controller.axis.left_trigger"),
            Map.entry("axisTriggerRight", "drivebywire.controller.axis.right_trigger"),

            // 键盘按键映射
            Map.entry("keyUp",    "drivebywire.controller.key.up"),
            Map.entry("keyDown",  "drivebywire.controller.key.down"),
            Map.entry("keyLeft",  "drivebywire.controller.key.left"),
            Map.entry("keyRight", "drivebywire.controller.key.right"),
            Map.entry("keyJump",  "drivebywire.controller.key.jump"),
            Map.entry("keyShift", "drivebywire.controller.key.shift")
    );

    static final WorldAttached<Map<Pair<BlockPos, Integer>, Integer>> timeoutButtonMap = new WorldAttached<>(k -> new HashMap<>());
    static final WorldAttached<Map<Pair<BlockPos, Integer>, Integer>> timeoutAxisMap = new WorldAttached<>(k -> new HashMap<>());
    static final int TIMEOUT = 30;

    public static void tick(Level world) {
        Map<Pair<BlockPos, Integer>, Integer> tbm = timeoutButtonMap.get(world);
        for (Iterator<Map.Entry<Pair<BlockPos, Integer>, Integer>> iterator = tbm.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<Pair<BlockPos, Integer>, Integer> entry = iterator.next();
            int ttl = entry.getValue();
            entry.setValue(--ttl);
            if (ttl <= 0) {

                ShipWireNetworkManager.trySetSignalAt(world, entry.getKey().getFirst(),
                        BUTTON_TO_CHANNEL[entry.getKey().getSecond()], 0);
                iterator.remove();
            }
        }

        Map<Pair<BlockPos, Integer>, Integer> tam = timeoutAxisMap.get(world);
        for (Iterator<Map.Entry<Pair<BlockPos, Integer>, Integer>> iterator = tam.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<Pair<BlockPos, Integer>, Integer> entry = iterator.next();
            int ttl = entry.getValue();
            entry.setValue(--ttl);
            if (ttl <= 0) {

                ShipWireNetworkManager.trySetSignalAt(world, entry.getKey().getFirst(),
                        AXIS_TO_CHANNEL[entry.getKey().getSecond()], 0);
                iterator.remove();
            }
        }
    }

    public static void receiveAxis(Level world, BlockPos pos, List<Byte> axisStates) {
        for (int i = 0; i < axisStates.size(); i++) {
            Byte b = axisStates.get(i);
            ShipWireNetworkManager.trySetSignalAt(world, pos, AXIS_TO_CHANNEL[i], b);
            timeoutAxisMap.get(world).put(new Pair<>(pos, i), TIMEOUT);
        }
    }

    public static void receiveButton(Level world, BlockPos pos, List<Boolean> buttonStates) {
        for (int i = 0; i < buttonStates.size(); i++) {
            Boolean b = buttonStates.get(i);
            ShipWireNetworkManager.trySetSignalAt(world, pos, BUTTON_TO_CHANNEL[i], b ? 15 : 0);
            if (b) timeoutButtonMap.get(world).put(new Pair<>(pos, i), TIMEOUT);
        }
    }

    public static void reset(Level world, BlockPos pos) {
        for (int i = 0; i < AXIS_TO_CHANNEL.length; i++) {
            ShipWireNetworkManager.trySetSignalAt(world, pos, AXIS_TO_CHANNEL[i], 0);
            timeoutAxisMap.get(world).remove(new Pair<>(pos, i));
        }

        for (int i = 0; i < BUTTON_TO_CHANNEL.length; i++) {
            ShipWireNetworkManager.trySetSignalAt(world, pos, BUTTON_TO_CHANNEL[i], 0);
            timeoutButtonMap.get(world).remove(new Pair<>(pos, i));
        }
    }
}
