package edn.stratodonut.drivebywire.blocks;

import com.simibubi.create.foundation.block.IBE;
import edn.stratodonut.drivebywire.WireBlockEntities;
import edn.stratodonut.drivebywire.WireSounds;
import edn.stratodonut.drivebywire.mixinducks.TweakedControllerDuck;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static edn.stratodonut.drivebywire.compat.TweakedControllerWireServerHandler.AXIS_TO_CHANNEL;
import static edn.stratodonut.drivebywire.compat.TweakedControllerWireServerHandler.BUTTON_TO_CHANNEL;

public class TweakedControllerHubBlock extends Block implements MultiChannelWireSource, IBE<ControllerHubBlockEntity> {
    @Unique
    private static final List<String> channels =
            Stream.concat(Arrays.stream(AXIS_TO_CHANNEL), Arrays.stream(BUTTON_TO_CHANNEL)).toList();

    public TweakedControllerHubBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState p_60503_, @NotNull Level level, @NotNull BlockPos blockPos,
                                          @NotNull Player player, @NotNull InteractionHand p_60507_, @NotNull BlockHitResult p_60508_) {
        ItemStack itemStack = player.getItemInHand(p_60507_);
        ControllerHubBlockEntity blockEntity = getBlockEntity(level, blockPos);
        if (itemStack.getItem() instanceof TweakedControllerDuck && blockEntity != null) {
            HubItem.putHub(itemStack, blockEntity.getUUID_VALUE());
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
        return ControllerHubBlock.BOTTOM_AABB;
    }

    @Unique
    @Override
    public List<String> wire$getChannels() {
        return channels;
    }

    @Unique
    @Override
    public @Nonnull String wire$nextChannel(String current, boolean forward) {
        int curIndex = channels.indexOf(current);
        if (curIndex == -1) {
            return channels.get(0);
        } else {
            return channels.get(Math.floorMod(curIndex + (forward ? 1 : -1), channels.size()));
        }
    }

    @Override
    public Class<ControllerHubBlockEntity> getBlockEntityClass() {
        return ControllerHubBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ControllerHubBlockEntity> getBlockEntityType() {
        return WireBlockEntities.CONTROLLER_HUB_BLOCK_ENTITY.get();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return IBE.super.newBlockEntity(p_153215_, p_153216_);
    }
}
