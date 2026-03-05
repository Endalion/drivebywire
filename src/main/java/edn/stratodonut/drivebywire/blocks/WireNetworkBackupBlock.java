package edn.stratodonut.drivebywire.blocks;

import com.simibubi.create.foundation.block.IBE;
import edn.stratodonut.drivebywire.WireBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class WireNetworkBackupBlock extends HorizontalDirectionalBlock implements IBE<WireNetworkBackupBlockEntity> {
    public WireNetworkBackupBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    public void tick(BlockState state, ServerLevel serverLevel, BlockPos pos, RandomSource random) {
        this.withBlockEntityDo(serverLevel, pos, wnbbe -> {
            if (!wnbbe.tryLoadPendingData()) this.scheduleLoadAttempt(serverLevel, pos);
        });
    }

    public void scheduleLoadAttempt(ServerLevel serverLevel, BlockPos pos) {
        serverLevel.scheduleTick(pos, this, 10);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return IBE.super.newBlockEntity(p_153215_, p_153216_);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing;
        if (context.getClickedFace()
                .getAxis()
                .isVertical())
            facing = context.getHorizontalDirection()
                    .getOpposite();
        else {
            BlockState blockState = context.getLevel()
                    .getBlockState(context.getClickedPos()
                            .relative(context.getClickedFace()
                                    .getOpposite()));
            facing = context.getClickedFace();
        }
        return defaultBlockState().setValue(FACING, facing);
    }

    @Override
    public Class<WireNetworkBackupBlockEntity> getBlockEntityClass() {
        return WireNetworkBackupBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends WireNetworkBackupBlockEntity> getBlockEntityType() {
        return WireBlockEntities.BACKUP_BLOCK_ENTITY.get();
    }
}
