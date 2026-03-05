package edn.stratodonut.drivebywire.blocks;

import edn.stratodonut.drivebywire.client.ClientWireNetworkHandler;
import edn.stratodonut.drivebywire.wire.ShipWireNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class WireNetworkBackupBlockEntity extends BlockEntity {
    @Nullable
    private CompoundTag pendingBackupData;

    public WireNetworkBackupBlockEntity(BlockEntityType<?> p_155228_, BlockPos p_155229_, BlockState p_155230_) {
        super(p_155228_, p_155229_, p_155230_);
    }

    public boolean tryLoadPendingData() {
        if (pendingBackupData == null || this.level.isClientSide) return true;
        if (!(VSGameUtilsKt.getLoadedShipManagingPos(this.level, this.getBlockPos()) instanceof LoadedServerShip ss)) return false;

        ShipWireNetworkManager.loadIfNotExists(ss, this.level, pendingBackupData,
                this.getBlockPos(), Rotation.NONE);
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag p_187471_) {
        super.saveAdditional(p_187471_);
        // If level is clientside, then we still want to save because schematics save on client thread, which is such a mess
        if (level == null) return;

        Ship s = VSGameUtilsKt.getLoadedShipManagingPos(level, this.getBlockPos());
        if (s instanceof LoadedServerShip ss) {
            ShipWireNetworkManager.get(ss).ifPresent(
                    m -> {
                        if (pendingBackupData == null) pendingBackupData = new CompoundTag();
                        pendingBackupData.merge(m.serialiseToNbt(level, this.getBlockPos()));
                    }
            );
        } else if (s != null && level.isClientSide) {
            List<ShipWireNetworkManager> t = new ArrayList<>();
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> t.add(ClientWireNetworkHandler.getClientManagers(s.getId())));
            if (t.size() == 1 && t.get(0) != null) {
                p_187471_.put("WireNetwork", t.get(0).serialiseToNbt(level, this.getBlockPos()));
            }
        }

        if (pendingBackupData != null) p_187471_.put("WireNetwork", pendingBackupData);
    }

    @Override
    public void load(CompoundTag p_155245_) {
        super.load(p_155245_);
        if (!p_155245_.contains("WireNetwork", Tag.TAG_COMPOUND)) return;
        if (VSGameUtilsKt.getShipObjectManagingPos(this.level, this.getBlockPos()) instanceof LoadedServerShip ss) {
            ShipWireNetworkManager.loadIfNotExists(ss, this.level, p_155245_.getCompound("WireNetwork"),
                    this.getBlockPos(), Rotation.NONE);
        } else {
            Block b = this.getBlockState().getBlock();
            if (this.level instanceof ServerLevel sll && b instanceof WireNetworkBackupBlock wnbe) {
                wnbe.scheduleLoadAttempt(sll, this.worldPosition);
            }
        }
        pendingBackupData = p_155245_.getCompound("WireNetwork");
    }
}
