package edn.stratodonut.drivebywire.compat.cc;

import com.mojang.datafixers.util.Pair;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import edn.stratodonut.drivebywire.DriveByWireMod;
import edn.stratodonut.drivebywire.wire.CircularChannels;
import edn.stratodonut.drivebywire.wire.IChannelSet;
import edn.stratodonut.drivebywire.wire.MultiChannelWireSource;
import edn.stratodonut.drivebywire.wire.ShipWireNetworkManager;
import edn.stratodonut.drivebywire.wire.graph.WireNetworkNode;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.*;

public class ControllerHubPeripheral implements IPeripheral {
    public static final String TYPE = ResourceLocation.fromNamespaceAndPath(DriveByWireMod.MOD_ID, "controller_hub").toString();


    private final Set<IComputerAccess> computers = new HashSet<>();

    private final Level level;
    private final BlockPos pos;

    public ControllerHubPeripheral(Level level, BlockPos pos) {
        this.level = level;
        this.pos = pos.immutable();

        if (!(level instanceof ServerLevel serverLevel)) return;

        LoadedServerShip ship = VSGameUtilsKt.getLoadedShipManagingPos(serverLevel, pos);
        if (ship == null) return;

        ShipWireNetworkManager manager = ShipWireNetworkManager.getOrCreate(ship);
        manager.addChangeListener(this::sourcesChanged);
    }

    private void sourcesChanged(Pair<String, Integer> stringIntegerPair) {
        for (IComputerAccess computer : computers) {
            computer.queueEvent("controller_changed", stringIntegerPair.getFirst(), stringIntegerPair.getSecond());
        }
    }

    @Override
    public void attach(IComputerAccess computer) {
        computers.add(computer);
    }

    @Override
    public void detach(IComputerAccess computer) {
        computers.remove(computer);
    }

    @Override
    public String getType() {
        return TYPE;
    }


    @Nullable
    private MultiChannelWireSource source() {
        if (level.getBlockState(pos).getBlock() instanceof MultiChannelWireSource s) return s;
        return null;
    }

    @LuaFunction
    public final List<String> listChannels() {
        MultiChannelWireSource s = source();
        if (s == null) return List.of();

        IChannelSet set = s.wire$getChannelSet();
        if (set instanceof CircularChannels circularChannels) {
            return circularChannels.allChannels();
        }

        return List.of();
    }

    @LuaFunction(mainThread = true)
    public final int getChannelValue(String channel) throws LuaException {
        if (!(level instanceof ServerLevel serverLevel)) return 0;

        LoadedServerShip ship = VSGameUtilsKt.getLoadedShipManagingPos(serverLevel, pos);
        if (ship == null) throw new LuaException("Must be on a ship!");

        ShipWireNetworkManager manager = ShipWireNetworkManager.getOrCreate(ship);

        return manager.getStaticSinks().getOrDefault(pos.asLong(), Map.of(channel, 0)).get(channel);
    }

    @LuaFunction(mainThread = true)
    public final void setChannelValue(String channel, int value) throws LuaException {
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (value < 0 || value > 15) throw new LuaException("value must be in range 0..15");

        LoadedServerShip ship = VSGameUtilsKt.getLoadedShipManagingPos(serverLevel, pos);
        if (ship == null) return;

        ShipWireNetworkManager manager = ShipWireNetworkManager.getOrCreate(ship);

        var sinks = manager.getNetwork();

        // Shouldn't be possible
        if (!(sinks.containsKey(pos.asLong()))) return;

        if (!(sinks.get(pos.asLong()).containsKey(channel))) throw new LuaException("Channel '"+channel+"' was unknown, or does not have a cable connected (This is a limitation of this peripheral)");

        manager.setSource(level, pos, channel, value);
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        if (this == other) return true;
        if (!(other instanceof ControllerHubPeripheral o)) return false;
        return o.level == this.level && o.pos.equals(this.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, pos);
    }
}
