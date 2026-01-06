package edn.stratodonut.drivebywire.blocks;

import edn.stratodonut.drivebywire.WireSounds;
import edn.stratodonut.drivebywire.compat.TFMGEngineControllerWireServerHandler;
import edn.stratodonut.drivebywire.mixinducks.TFMGControllerDuck;
import edn.stratodonut.drivebywire.util.HubItem;
import edn.stratodonut.drivebywire.wire.MultiChannelWireSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TFMGEngineControllerHubBlock extends Block implements MultiChannelWireSource{
    private static final List<String> channels = List.of(
            TFMGEngineControllerWireServerHandler.STEER_LEFT,
            TFMGEngineControllerWireServerHandler.STEER_RIGHT,

            TFMGEngineControllerWireServerHandler.ENGINE_STARTED,

            TFMGEngineControllerWireServerHandler.SHIFT_REVERSE,
            TFMGEngineControllerWireServerHandler.SHIFT_NEUTRAL,
            TFMGEngineControllerWireServerHandler.SHIFT_1,
            TFMGEngineControllerWireServerHandler.SHIFT_2,
            TFMGEngineControllerWireServerHandler.SHIFT_3,
            TFMGEngineControllerWireServerHandler.SHIFT_4,
            TFMGEngineControllerWireServerHandler.SHIFT_5,
            TFMGEngineControllerWireServerHandler.SHIFT_6,

            TFMGEngineControllerWireServerHandler.PEDAL_CLUTCH,
            TFMGEngineControllerWireServerHandler.PEDAL_BRAKE,
            TFMGEngineControllerWireServerHandler.PEDAL_GAS
    );

    public TFMGEngineControllerHubBlock(Properties props) {
        super(props);
    }

    @Override
    public @NotNull InteractionResult use(
            @NotNull BlockState state,
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull Player player,
            @NotNull InteractionHand hand,
            @NotNull BlockHitResult hit
    ) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.getItem() instanceof TFMGControllerDuck) {
            HubItem.putHub(stack, pos);
            if (!level.isClientSide) {
                level.playSound(null, pos, WireSounds.PLUG_IN.get(), SoundSource.BLOCKS, 1, 1);
                player.displayClientMessage(
                        Component.literal("TFMG Controller connected!"), true
                );
            }
            return InteractionResult.SUCCESS;
        }

        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return ControllerHubBlock.BOTTOM_AABB;
    }

    @Override
    public List<String> wire$getChannels() {
        return channels;
    }

    @Override
    public @NotNull String wire$nextChannel(String current, boolean forward) {
        int idx = channels.indexOf(current);
        if (idx == -1) return channels.get(0);
        return channels.get(Math.floorMod(idx + (forward ? 1 : -1), channels.size()));
    }
}
