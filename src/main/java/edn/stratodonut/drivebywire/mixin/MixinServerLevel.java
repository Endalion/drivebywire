package edn.stratodonut.drivebywire.mixin;

import edn.stratodonut.drivebywire.wire.ShipWireNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel extends Level {
    protected MixinServerLevel(WritableLevelData p_270739_, ResourceKey<Level> p_270683_, RegistryAccess p_270200_, Holder<DimensionType> p_270240_, Supplier<ProfilerFiller> p_270692_, boolean p_270904_, boolean p_270470_, long p_270248_, int p_270466_) {
        super(p_270739_, p_270683_, p_270200_, p_270240_, p_270692_, p_270904_, p_270470_, p_270248_, p_270466_);
    }

    @Override
    public int getSignal(BlockPos pos, Direction direction) {
        int original = super.getSignal(pos, direction);
        BlockPos target = pos.relative(direction.getOpposite());
        
        Ship s = VSGameUtilsKt.getShipObjectManagingPos((ServerLevel)(Object) this, target);
        if (s instanceof LoadedServerShip serverShip) {
            int networkValue = Math.max(
                    original,
                    ShipWireNetworkManager.get(serverShip)
                            .map(m -> m.getSignalAt(target, direction))
                            .orElse(0)
            );
            return Mth.clamp(networkValue, 0, 15);
        }
        
        return original;
    }

    @Override
    public int getDirectSignal(BlockPos pos, Direction direction) {
        int original = super.getDirectSignal(pos, direction);
        BlockPos target = pos.relative(direction.getOpposite());

        Ship s = VSGameUtilsKt.getShipObjectManagingPos((ServerLevel)(Object) this, target);
        if (s instanceof LoadedServerShip serverShip) {
            int networkValue = Math.max(
                    original,
                    ShipWireNetworkManager.get(serverShip)
                            .map(m -> m.getSignalAt(target, direction))
                            .orElse(0)
            );
            return Mth.clamp(networkValue, 0, 15);
        }

        return original;
    }
}
