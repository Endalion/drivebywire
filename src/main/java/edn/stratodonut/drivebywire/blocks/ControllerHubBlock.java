package edn.stratodonut.drivebywire.blocks;

import com.simibubi.create.AllItems;
import edn.stratodonut.drivebywire.WireSounds;
import edn.stratodonut.drivebywire.wire.CircularChannels;
import edn.stratodonut.drivebywire.util.HubItem;
import edn.stratodonut.drivebywire.wire.IChannelSet;
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

import javax.annotation.Nonnull;
import java.util.Arrays;

import static edn.stratodonut.drivebywire.compat.LinkedControllerWireServerHandler.KEY_TO_CHANNEL;

public class ControllerHubBlock extends Block implements MultiChannelWireSource {
    public static final VoxelShape BOTTOM_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);

    private static final CircularChannels channels = new CircularChannels(Arrays.stream(KEY_TO_CHANNEL).toList());

    public ControllerHubBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState p_60503_, @NotNull Level level, @NotNull BlockPos blockPos,
                                          @NotNull Player player, @NotNull InteractionHand p_60507_, @NotNull BlockHitResult p_60508_) {
        ItemStack itemStack = player.getItemInHand(p_60507_);
        if (AllItems.LINKED_CONTROLLER.is(itemStack.getItem())) {
            HubItem.putHub(itemStack, blockPos);
            if (!level.isClientSide) {
                level.playSound(null, blockPos, WireSounds.PLUG_IN.get(), SoundSource.BLOCKS, 1, 1);
                player.displayClientMessage(Component.literal("Controller connected!"), true);
            }

            return InteractionResult.SUCCESS;
        }

        return super.use(p_60503_, level, blockPos, player, p_60507_, p_60508_);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return BOTTOM_AABB;
    }

    @Nonnull
    @Override
    public IChannelSet wire$getChannelSet() {
        return channels;
    }
}
