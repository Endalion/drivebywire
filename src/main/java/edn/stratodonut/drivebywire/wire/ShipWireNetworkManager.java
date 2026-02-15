package edn.stratodonut.drivebywire.wire;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import edn.stratodonut.drivebywire.DriveByWireMod;
import edn.stratodonut.drivebywire.jackson.BlockFaceDeserializer;
import edn.stratodonut.drivebywire.jackson.BlockFaceSerializer;
import edn.stratodonut.drivebywire.util.BlockFace;
import edn.stratodonut.drivebywire.util.DicewareGenerator;
import edn.stratodonut.drivebywire.wire.graph.WireNetworkNode;
import edn.stratodonut.drivebywire.wire.graph.WireNetworkNode.WireNetworkSink;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;

import org.jetbrains.annotations.Contract;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Stream;

import static org.valkyrienskies.mod.common.util.VectorConversionsMCKt.toMinecraft;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY
)
public final class ShipWireNetworkManager {
    @Nonnull
    private String name;
    private long origin;

    @Nonnull
    public final Long shipId;

    private final Map<Long, Map<String, Set<WireNetworkSink>>> sinks = new HashMap<>();

    @JsonDeserialize(keyUsing = BlockFaceDeserializer.class)
    @JsonSerialize(keyUsing = BlockFaceSerializer.class)
    private final Map<BlockFace, WireNetworkNode> nodes = new HashMap<>();


    private ShipWireNetworkManager() {
        this(-1, 0L, "");
    }

    private ShipWireNetworkManager(long id, long origin, @Nonnull String name) {
        this.shipId = id;
        this.origin = origin;
        this.name = name;
    }

    public static ShipWireNetworkManager client() {
        return new ShipWireNetworkManager();
    }

    @Nonnull
    public static ShipWireNetworkManager getOrCreate(LoadedServerShip ship) {
        if (ship.getAttachment(ShipWireNetworkManager.class) == null) {
            ship.setAttachment(
                    new ShipWireNetworkManager(
                            ship.getId(),
                            BlockPos.containing(toMinecraft(ship.getTransform().getPositionInShip())).asLong(),
                            DicewareGenerator.generate(3)
                    )
            );
        }

        return ship.getAttachment(ShipWireNetworkManager.class);
    }

    @Nonnull
    public static Optional<ShipWireNetworkManager> get(LoadedServerShip ship) {
        return Optional.ofNullable(ship.getAttachment(ShipWireNetworkManager.class));
    }

    public static void loadIfNotExists(LoadedServerShip ship, Level level, CompoundTag nbt, BlockPos origin, Rotation rot) {
        ShipWireNetworkManager m = ship.getAttachment(ShipWireNetworkManager.class);
        if (m == null) {
            ship.setAttachment(new ShipWireNetworkManager(ship.getId(), 0L, ""));
            ship.getAttachment(ShipWireNetworkManager.class).deserialiseFromNbt(level, nbt, origin, rot);
        } else {
            if (nbt.contains("Network", Tag.TAG_COMPOUND))
                nbt.getCompound("Network").remove(m.name);
        }
    }

    public enum CONNECTION_RESULT {
        OK(""),
        FAIL_EXISTS("Connection already exists!"),
        FAIL_TOO_MANY_SOURCE("Exceeded source limit for this ship!"),
        FAIL_TOO_MANY_SINK("Exceeded sink limit for this source!"),
        FAIL_INVALID_SHIP("Invalid ships!"),
        FAIL_SAME_NAME("Cannot connect different networks with the same name!");

        CONNECTION_RESULT(String d) {
            this.literal = d;
        }

        private final String literal;
        public boolean isSuccess() {
            return this == OK;
        }
        public String getDescription() { return this.literal; }
    }
    public static CONNECTION_RESULT createConnection(Level level, BlockPos in, BlockPos out, Direction dir) {
        return ShipWireNetworkManager.createConnection(level, in, out, dir, WorldChannelSet.WORLD_REDSTONE_CHANNEL);
    }

    public static CONNECTION_RESULT createConnection(Level level, BlockPos in, BlockPos out, Direction dir, String channel) {
        Ship s1 = VSGameUtilsKt.getShipObjectManagingPos(level, in);
        Ship s2 = VSGameUtilsKt.getShipObjectManagingPos(level, out);
        if (s1 instanceof LoadedServerShip ss1 && s2 instanceof LoadedServerShip ss2) {
            ShipWireNetworkManager m1 = ShipWireNetworkManager.getOrCreate(ss1);
            ShipWireNetworkManager m2 = ShipWireNetworkManager.getOrCreate(ss2);

            if (!m1.equals(m2) && m1.name.equals(m2.name)) return CONNECTION_RESULT.FAIL_SAME_NAME;
            if (m1.size() >= 64) return CONNECTION_RESULT.FAIL_TOO_MANY_SOURCE;

            WireNetworkSink node = new WireNetworkSink(out, dir, m2.name, ss2.getId(), m2.origin);
            Set<WireNetworkSink> existingSinks = m1.getOrCreateSinksOnChannel(in, channel);
            if (existingSinks.size() >= 64) return CONNECTION_RESULT.FAIL_TOO_MANY_SINK;
            if (!existingSinks.add(node)) return CONNECTION_RESULT.FAIL_EXISTS;

            m2.nodes.put(BlockFace.of(out.asLong(), dir.get3DDataValue()), node);
            return CONNECTION_RESULT.OK;
        }
        return CONNECTION_RESULT.FAIL_INVALID_SHIP;
    }

    public static void removeConnection(Level level, BlockPos in, BlockPos out, Direction dir) {
        ShipWireNetworkManager.removeConnection(level, in, out, dir, WorldChannelSet.WORLD_REDSTONE_CHANNEL);
    }

    public static void removeConnection(Level level, BlockPos in, BlockPos out, Direction dir, String channel) {
        Ship s1 = VSGameUtilsKt.getShipObjectManagingPos(level, in);
        Ship s2 = VSGameUtilsKt.getShipObjectManagingPos(level, out);
        if (s1 instanceof LoadedServerShip ss1 && s2 instanceof LoadedServerShip ss2) {
            Optional<ShipWireNetworkManager> o1 = ShipWireNetworkManager.get(ss1);
            Optional<ShipWireNetworkManager> o2 = ShipWireNetworkManager.get(ss2);
            if (o1.isEmpty() || o2.isEmpty()) return;

            ShipWireNetworkManager m1 = o1.get();
            ShipWireNetworkManager m2 = o2.get();
            WireNetworkSink node = new WireNetworkSink(out, dir, m2.name, ss2.getId(), m2.origin);
            Optional<Boolean> removed = Stream.of(m1.sinks.get(in.asLong()))
                    .map(allChannels -> allChannels.get(channel))
                    .map(allNodes -> allNodes.remove(node))
                    .findFirst();

            removed.ifPresent(b -> {
                if (b) {
                    node.setInput(level, channel, 0);
                }
                if (m1.sinks.get(in.asLong()).get(channel).isEmpty()) {
                    m1.sinks.get(in.asLong()).remove(channel);
                }
                if (m1.sinks.get(in.asLong()).isEmpty()) {
                    m1.sinks.remove(in.asLong());
                }
            });
        }
    }

    public static void removeAllFromSource(Level level, BlockPos in) {
        Ship s1 = VSGameUtilsKt.getShipObjectManagingPos(level, in);
        if (s1 instanceof LoadedServerShip ss1) {
            ShipWireNetworkManager.get(ss1).ifPresent(m -> {
                m.sinks.getOrDefault(in.asLong(), new HashMap<>())
                        .forEach((channel, subnet) -> subnet.forEach(node -> node.setInput(level, channel, 0)));
                m.sinks.remove(in.asLong());
            });
        }
    }

    public WireNetworkNode getOrCreateNodeAt(Long pos, int dir) {
        return nodes.computeIfAbsent(BlockFace.of(pos, dir), k -> new WireNetworkNode(pos, dir));
    }

    public void setSource(Level level, BlockPos src, String channel, int signal) {
        if (sinks.containsKey(src.asLong()) && sinks.get(src.asLong()).containsKey(channel))
            sinks.get(src.asLong()).get(channel).forEach(n -> n.setInput(level, channel, signal));
    }

    @Nonnull
    private Set<WireNetworkSink> getOrCreateSinksOnChannel(BlockPos source, String channel) {
        return sinks.computeIfAbsent(source.asLong(), k -> new HashMap<>()).computeIfAbsent(channel, k -> new HashSet<>());
    }

    private void updateWorld(Level level, BlockPos pos) {
        level.updateNeighborsAt(pos, level.getBlockState(pos).getBlock());
    }

    /**
     * No guarantee if it successfully works!
     */
    public static void trySetSignalAt(Level level, BlockPos in, String channel, int value) {
        Ship s = VSGameUtilsKt.getShipObjectManagingPos(level, in);
        if (!(s instanceof LoadedServerShip ss)) return;
        ShipWireNetworkManager.get(ss).ifPresent(m -> m.setSource(level, in, channel, value));
    }

    public int getSignalAt(BlockPos out, Direction side) {
        long o = out.asLong();
        BlockFace k = BlockFace.of(o, side.get3DDataValue());
        if (nodes.get(k) != null) return nodes.get(k).getSignal();
        return 0;
    }

    public void deserialiseFromNbt(Level level, CompoundTag nbt, BlockPos backupPos, Rotation rot) {
        if (this.size() > 0 && !level.isClientSide)
            DriveByWireMod.warn("Deserializing {} onto existing network {}!", nbt.getString("Name"), this.name);
        clearAndUpdateLevel(level);

        this.name = nbt.getString("Name");
        this.origin = backupPos.offset(BlockPos.of(nbt.getLong("BackupOffset"))).asLong();
        BlockPos originPos = BlockPos.of(this.origin);
        CompoundTag network = nbt.getCompound("Network");
        if (!network.contains(this.name, Tag.TAG_COMPOUND)) return;
        CompoundTag local = network.getCompound(this.name);
        for (String ch : local.getAllKeys()) {
            long[] inOutPairs = local.getLongArray(ch);
            for (int i = 0; i < inOutPairs.length - 2; i += 3) {
                BlockPos start = BlockPos.of(inOutPairs[i]).offset(originPos);
                BlockPos end = BlockPos.of(inOutPairs[i + 1]).offset(originPos);
                Direction dir = Direction.from3DDataValue((int) inOutPairs[i + 2]);

                WireNetworkSink node = new WireNetworkSink(end, dir, this.name, this.shipId, this.origin);
                this.getOrCreateSinksOnChannel(start, ch).add(node);
                this.nodes.put(BlockFace.of(end.asLong(), dir.get3DDataValue()), node);
            }
        }
        network.remove(this.name);
    }

    public void deserialiseForeignOnClient(Level level, CompoundTag nbt, BlockPos backupPos, Rotation rot) {
        CompoundTag network = nbt.getCompound("Network");
        for (String foreignName : network.getAllKeys()) {
            CompoundTag foreignNet = network.getCompound(foreignName);
            for (String channel : foreignNet.getAllKeys()) {
                long[] inOutPairs = foreignNet.getLongArray(channel);
                for (int i = 0; i < inOutPairs.length - 2; i += 3) {
                    BlockPos start = BlockPos.of(inOutPairs[i]).offset(BlockPos.of(this.origin));
                    BlockPos end = BlockPos.of(inOutPairs[i + 1]).offset(backupPos);
                    Direction dir = Direction.from3DDataValue((int) inOutPairs[i + 2]);

                    WireNetworkSink node = new WireNetworkSink(end, dir, foreignName, -1L, backupPos.asLong());
                    this.getOrCreateSinksOnChannel(start, channel).add(node);
                }
            }
            network.remove(foreignName);
        }
    }

    public CompoundTag serialiseToNbt(Level level, BlockPos backupPos) {
        return serialiseToNbt(level, backupPos, false);
    }

    public CompoundTag serialiseToNbt(Level level, BlockPos backupPos, boolean foreignAbsolutePos) {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("Name", this.name);
        BlockPos originPos = BlockPos.of(this.origin);
        nbt.putLong("BackupOffset", originPos.subtract(backupPos).asLong());

        Map<String, Map<String, List<Long>>> networks = new HashMap<>();
        sinks.forEach((source, fromSource) -> fromSource.forEach((channel, inChannel) -> inChannel.forEach(
                n -> {
                    List<Long> l = networks.computeIfAbsent(n.getParentNetworkName(), k -> new HashMap<>())
                            .computeIfAbsent(channel, k -> new ArrayList<>());

                    l.add(BlockPos.of(source).subtract(originPos).asLong());
                    if (!n.getParentNetworkName().equals(this.name) && foreignAbsolutePos) {
                        l.add(n.getPosition());
                    } else {
                        l.add(n.getRelativePositionInParent());
                    }
                    l.add((long) n.getDirection());
                }
        )));

        CompoundTag networkTag = new CompoundTag();
        for (Map.Entry<String, Map<String, List<Long>>> subnet : networks.entrySet()) {
            CompoundTag subnetTag = new CompoundTag();
            for (Map.Entry<String, List<Long>> channels : subnet.getValue().entrySet()) {
                subnetTag.putLongArray(channels.getKey(), channels.getValue());
            }
            networkTag.put(subnet.getKey(), subnetTag);
        }
        nbt.put("Network", networkTag);

        return nbt;
    }

    public void linkNetwork(ShipWireNetworkManager other, long foreignShipId, CompoundTag foreignNet) {
        BlockPos foreignOrigin = BlockPos.of(other.origin);
        for (String channel : foreignNet.getAllKeys()) {
            long[] inOutPairs = foreignNet.getLongArray(channel);
            for (int i = 0; i < inOutPairs.length - 2; i += 3) {
                BlockPos start = BlockPos.of(inOutPairs[i]).offset(BlockPos.of(this.origin));
                BlockPos end = BlockPos.of(inOutPairs[i + 1]).offset(foreignOrigin);
                Direction dir = Direction.from3DDataValue((int) inOutPairs[i + 2]);

                WireNetworkSink node = new WireNetworkSink(end, dir, other.name, foreignShipId, other.origin);
                this.getOrCreateSinksOnChannel(start, channel).add(node);
                other.nodes.putIfAbsent(BlockFace.of(end.asLong(), dir.get3DDataValue()), node);
            }
        }
    }

    public void clearAndUpdateLevel(Level level) {
        sinks.values().stream()
                .<WireNetworkNode>mapMulti((map, c) -> map.forEach((k,v) -> v.forEach(c)))
                .forEach(n -> this.updateWorld(level, BlockPos.of(n.getPosition())
                        .relative(Direction.from3DDataValue(n.getDirection()))));
        this.clear();
    }

    public int size() {
        return sinks.size();
    }

    public void clear() {
        sinks.clear();
        nodes.clear();
    }

    @Contract(mutates = "this")
    public Map<Long, Map<String, Set<WireNetworkNode.WireNetworkSink>>> getNetwork() {
        return sinks;
    }

    @Nonnull
    public String getName() {
        return name;
    }
}
