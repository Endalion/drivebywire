package edn.stratodonut.drivebywire.blocks;

import edn.stratodonut.drivebywire.DriveByWireMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class ControllerHubBlockEntity  extends BlockEntity {
    private String UUID_VALUE = UUID.randomUUID().toString();


    public String getUUID_VALUE() {
        return UUID_VALUE;
    }

    public ControllerHubBlockEntity(BlockEntityType<?> p_155228_, BlockPos p_155229_, BlockState p_155230_) {
        super(p_155228_, p_155229_, p_155230_);

        DriveByWireMod.hubs.put(UUID_VALUE, this.getBlockPos());
    }

    @Override
    protected void saveAdditional(CompoundTag p_187471_) {
        super.saveAdditional(p_187471_);
        if (level != null) {
            p_187471_.putString(DriveByWireMod.UUID_KEY, UUID_VALUE);
        }
    }

    @Override
    public void load(CompoundTag p_155245_) {
        super.load(p_155245_);
        if (p_155245_.contains(DriveByWireMod.UUID_KEY, Tag.TAG_STRING)) {
            UUID_VALUE = p_155245_.getString(DriveByWireMod.UUID_KEY);
        }

        DriveByWireMod.hubs.put(UUID_VALUE, this.getBlockPos());
    }
}
