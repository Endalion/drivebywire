package edn.stratodonut.drivebywire.wire.graph;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import edn.stratodonut.drivebywire.wire.ShipWireNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WireNetworkNode {
    @Nonnull
    private final Map<String, Integer> inputs = new HashMap<>();
    private long position;
    private int direction;
    private WireNetworkNode() {}

    public WireNetworkNode(BlockPos pos, Direction dir) {
        this(pos.asLong(), dir.get3DDataValue());
    }

    public WireNetworkNode(long pos, int dir) {
        this.position = pos;
        this.direction = dir;
    }

    private boolean setInput(String channel, int signal) {
        return !Objects.equals(this.inputs.put(channel, signal), signal);
    }

    public int getSignal() {
        return this.inputs.values().stream().max(Comparator.naturalOrder()).orElse(0);
    }

    public long getPosition() {
        return position;
    }


    public int getDirection() {
        return direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.position, this.direction);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof WireNetworkNode n
                && this.position == n.position
                && this.direction == n.direction;
    }

    @JsonAutoDetect(
            fieldVisibility = JsonAutoDetect.Visibility.ANY
    )
    public static class WireNetworkSink extends WireNetworkNode {
        private String parentNetworkName;
        private long parentShipId;
        private long parentOrigin;

        @JsonIgnore
        @Nonnull
        private WeakReference<ShipWireNetworkManager> parent = new WeakReference<>(null);

        private WireNetworkSink() {
            super();
        }

        public WireNetworkSink(BlockPos pos, Direction dir) {
            super(pos, dir);
            this.parentNetworkName = "";
            this.parentShipId = -1L;
            this.parentOrigin = 0L;
        }

        public WireNetworkSink(BlockPos pos, Direction dir, String parent, long parentShipId, long parentOrigin) {
            super(pos, dir);
            this.parentNetworkName = parent;
            this.parentShipId = parentShipId;
            this.parentOrigin = parentOrigin;
        }

        public String getParentNetworkName() {
            return parentNetworkName;
        }

        public void setInput(Level level, String channel, int signal) {
            ShipWireNetworkManager parentManager = parent.get();
            if (parentManager == null) {
                if (VSGameUtilsKt.getShipObjectWorld(level).getLoadedShips().getById(this.parentShipId) instanceof LoadedServerShip ss &&
                        ShipWireNetworkManager.get(ss).isPresent()) {
                    parentManager = ShipWireNetworkManager.get(ss).get();
                    parent = new WeakReference<>(parentManager);
                } else {
                    return;
                }
            }

            WireNetworkNode n = parentManager.getOrCreateNodeAt(getPosition(), getDirection());
            BlockPos to = BlockPos.of(getPosition());
            BlockPos from = BlockPos.of(getPosition()).relative(Direction.from3DDataValue(getDirection()));
            if (n.setInput(channel, signal)) {
                level.updateNeighborsAt(from, level.getBlockState(from).getBlock());
                level.updateNeighborsAt(to, level.getBlockState(to).getBlock());
            }
        }

        public long getRelativePositionInParent() {
            return BlockPos.of(this.getPosition()).subtract(BlockPos.of(parentOrigin)).asLong();
        }
    }
}
