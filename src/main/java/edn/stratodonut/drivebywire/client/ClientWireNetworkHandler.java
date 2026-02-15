package edn.stratodonut.drivebywire.client;

import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import edn.stratodonut.drivebywire.WireItems;
import edn.stratodonut.drivebywire.WirePackets;
import edn.stratodonut.drivebywire.blocks.WireNetworkBackupBlock;
import edn.stratodonut.drivebywire.network.WireAddConnectionPacket;
import edn.stratodonut.drivebywire.network.WireLinkNetworksPacket;
import edn.stratodonut.drivebywire.network.WireNetworkRequestSyncPacket;
import edn.stratodonut.drivebywire.network.WireRemoveConnectionPacket;
import edn.stratodonut.drivebywire.util.BlockFace;
import edn.stratodonut.drivebywire.util.FaceOutlines;
import edn.stratodonut.drivebywire.util.ImmutableHashMap;
import edn.stratodonut.drivebywire.wire.IChannelSet;
import edn.stratodonut.drivebywire.wire.MultiChannelWireSource;
import edn.stratodonut.drivebywire.wire.ShipWireNetworkManager;
import edn.stratodonut.drivebywire.wire.WorldChannelSet;
import edn.stratodonut.drivebywire.wire.graph.WireNetworkNode.WireNetworkSink;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.theme.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientWireNetworkHandler {
    private static final ImmutableHashMap<Long, Map<String, Set<WireNetworkSink>>> EMPTY_MAP = new ImmutableHashMap<>();
    @Nonnull
    static ImmutableHashMap<Long, Map<String, Set<WireNetworkSink>>> currentNetwork = new ImmutableHashMap<>();
    @Nullable
    static BlockPos selectedSource;
    @Nonnull
    static IChannelSet channelSet = WorldChannelSet.INSTANCE;

    static long shipId = -1;
    @Nonnull
    static Map<Long, ShipWireNetworkManager> clientManagers = new HashMap<>();
    static int syncCooldown = 0;

    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event) {
        clearSource();
    }

    @SubscribeEvent()
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player == null || player.isSpectator())
            return;

        ItemStack heldItemMainhand = player.getMainHandItem();
        if (!WireItems.WIRE.isIn(heldItemMainhand)) {
            return;
        }

        BlockPos pos = event.getPos();
        Direction side = event.getFace();
        if (side == null) side = Direction.UP;
        Level world = event.getLevel();
        Ship s = VSGameUtilsKt.getShipManagingPos(world, pos);
        if (!world.isClientSide)
            return;

        if (selectedSource == null) {
            if (s == null) return;
            // If no source selected select as source
            selectedSource = pos;
            shipId = s.getId();
            changeChannel(world.getBlockState(selectedSource).getBlock(), true);
            if (clientManagers.containsKey(shipId)) {
                currentNetwork = new ImmutableHashMap<>(clientManagers.get(shipId).getNetwork());

                if (world.getBlockState(selectedSource).getBlock() instanceof WireNetworkBackupBlock) {
                    player.displayClientMessage(Component.literal(String.format("Relinking from %s", clientManagers.get(shipId).getName())), true);
                }
            } else {
                syncManager(shipId);
            }
        } else {
            if (selectedSource.equals(pos)) {
                clearSource();
            } else {
                // LINK NETWORKS
                if (world.getBlockState(selectedSource).getBlock() instanceof WireNetworkBackupBlock &&
                        world.getBlockState(pos).getBlock() instanceof WireNetworkBackupBlock ) {
                    WirePackets.getChannel().sendToServer(new WireLinkNetworksPacket(selectedSource, pos));
                } else {
                    Map<String, Set<WireNetworkSink>> currentSelection = currentNetwork.get(selectedSource.asLong());
                    WireNetworkSink node = new WireNetworkSink(pos, side);
                    if (currentSelection != null &&
                            (currentSelection.containsKey(channelSet.currentChannel()) && currentSelection.get(channelSet.currentChannel()).contains(node))) {
                        WirePackets.getChannel().sendToServer(new WireRemoveConnectionPacket(
                                shipId, selectedSource, pos, side, channelSet.currentChannel()));
                    } else {
                        WirePackets.getChannel().sendToServer(new WireAddConnectionPacket(
                                shipId, selectedSource, pos, side, channelSet.currentChannel()));
                    }
                }
            }
        }

        event.setCancellationResult(InteractionResult.CONSUME);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;

        Player player = Minecraft.getInstance().player;
        if (player == null)
            return;

        ItemStack heldItemMainhand = player.getMainHandItem();
        if (!WireItems.WIRE.isIn(heldItemMainhand) && !WireItems.WIRE_CUTTER.isIn(heldItemMainhand)) {
            clearSource();
            return;
        }

        Ship s = null;
        if (selectedSource == null) {
            HitResult hitResult = Minecraft.getInstance().hitResult;
            if (hitResult instanceof BlockHitResult bhr)
                s = VSGameUtilsKt.getShipManagingPos(player.level(), bhr.getBlockPos());
        } else {
            s = VSGameUtilsKt.getShipManagingPos(player.level(), selectedSource);
        }

        if (s == null) return;

        if (s.getId() != shipId) {
            clearSource();
            shipId = s.getId();
            syncManager(shipId);
            return;
        } else {
            syncCooldown--;
            if (syncCooldown <= 0) {
                syncManager(shipId);
                syncCooldown = 20;
            }
        }

        if (selectedSource != null) drawOutline(player.level(), selectedSource, LineColor.SOURCE.SELECTED.getColor());
        if (clientManagers.containsKey(shipId)) drawOutlines(player.level(), selectedSource, clientManagers.get(shipId).getNetwork(), channelSet.currentChannel());
    }

    @SubscribeEvent
    public static void onInputEvent(InputEvent.MouseScrollingEvent event) {
        double delta = event.getScrollDelta();
        Player p = Minecraft.getInstance().player;
        if (p == null || selectedSource == null) return;

        ItemStack heldItemMainhand = p.getMainHandItem();
        if (!WireItems.WIRE.isIn(heldItemMainhand)) {
            return;
        }

        changeChannel(p.level().getBlockState(selectedSource).getBlock(), delta > 0);
        event.setCanceled(true);
    }

    public static void changeChannel(Block source, boolean forward) {
        if (source instanceof MultiChannelWireSource channelWireSource) {
            if (!channelSet.equals(channelWireSource.wire$getChannelSet())) channelSet = channelWireSource.wire$getChannelSet();
            if (forward) {
                channelSet.nextChannel();
            } else {
                channelSet.previousChannel();
            }
        } else {
            channelSet = WorldChannelSet.INSTANCE;
        }
        Player p = Minecraft.getInstance().player;
        if (p != null) p.displayClientMessage(Component.literal("Selected Channel: " + channelSet.currentChannel()), true);
    }

    public static void clearSource() {
        currentNetwork = EMPTY_MAP;
        selectedSource = null;
        channelSet = WorldChannelSet.INSTANCE;
        shipId = -1;
    }

    @Nullable
    public static ShipWireNetworkManager getClientManagers(long id) {
        return clientManagers.get(id);
    }

    public static void syncManager(long shipId) {
        WirePackets.getChannel().sendToServer(new WireNetworkRequestSyncPacket(shipId));
    }

    public static void loadFrom(Level level, long id, CompoundTag nbt) {
        ShipWireNetworkManager incoming = clientManagers.computeIfAbsent(id, k ->
                ShipWireNetworkManager.client());
        incoming.deserialiseFromNbt(level, nbt, BlockPos.ZERO, Rotation.NONE);
        incoming.deserialiseForeignOnClient(level, nbt, BlockPos.ZERO, Rotation.NONE);
        if (selectedSource != null && id == shipId && clientManagers.containsKey(shipId))
            currentNetwork = new ImmutableHashMap<>(clientManagers.get(shipId).getNetwork());
    }

    public interface LineColor {
        int getColor();
        enum WIRE implements LineColor {
            SELECTED(Color.RED.getRGB()),
            SAME_SOURCE_DIFFERENT_CHANNEL(ChatFormatting.DARK_GRAY.getColor());
            private int color;

            WIRE(int c) {
                this.color = c;
            }

            @Override
            public int getColor() {
                return color;
            }
        }

        enum SOURCE implements LineColor {
            SELECTED(ArmInteractionPoint.Mode.TAKE.getColor()),
            SAME_NETWORK(0x577278); // Darker version of Selected
            private int color;

            SOURCE(int c) {
                this.color = c;
            }

            @Override
            public int getColor() {
                return color;
            }
        }

        enum SINK implements LineColor {
            SELECTED(ArmInteractionPoint.Mode.DEPOSIT.getColor()),
            SAME_SOURCE_DIFFERENT_CHANNEL(ChatFormatting.DARK_GRAY.getColor());
            private int color;

            SINK(int c) {
                this.color = c;
            }

            @Override
            public int getColor() {
                return color;
            }
        }
    }

    private static void drawOutlines(Level level, @Nullable BlockPos selectedSource,
                                     @Nonnull Map<Long, Map<String, Set<WireNetworkSink>>> network, @Nullable String currentChannel) {
        for (long source : network.keySet()) {
            Map<String, Set<WireNetworkSink>> netFromSource = network.get(source);

            if (selectedSource != null && source == selectedSource.asLong()) {
                for (String channel : netFromSource.keySet()) {
                    if (channel.equals(currentChannel)) continue;
                    for (WireNetworkSink sinks : netFromSource.get(channel)) {
                        drawConnection(level, selectedSource, BlockPos.of(sinks.getPosition()), Direction.from3DDataValue(sinks.getDirection()),
                                LineColor.SINK.SAME_SOURCE_DIFFERENT_CHANNEL.getColor(), LineColor.WIRE.SAME_SOURCE_DIFFERENT_CHANNEL.getColor());
                    }
                }

                if (netFromSource.containsKey(currentChannel)) {
                    for (WireNetworkSink sinks : netFromSource.get(currentChannel)) {
                        drawConnection(level, selectedSource, BlockPos.of(sinks.getPosition()), Direction.from3DDataValue(sinks.getDirection()),
                                LineColor.SINK.SELECTED.getColor(), LineColor.WIRE.SELECTED.getColor());
                    }
                }
            } else {
                drawOutline(level, BlockPos.of(source), LineColor.SOURCE.SAME_NETWORK.getColor());
            }
        }
    }

    // TODO: Give keys to all these calls to OUTLINER so it doesn't clash
    private static void drawConnection(Level level, BlockPos start, BlockPos end, Direction dir, int faceColor, int wireColor) {
        drawOutlineFace(level, end, dir, faceColor);

        Outliner.getInstance().showLine(
                Pair.of("wireConnection", Pair.of(end, dir)),
                VSGameUtilsKt.toWorldCoordinates(level, Vec3.atCenterOf(start)),
                VSGameUtilsKt.toWorldCoordinates(level, Vec3.atCenterOf(end).add(new Vec3(dir.step().mul(0.5f))))
        ).colored(wireColor);
    }

    private static void drawOutlineFace(Level level, BlockPos pos, Direction dir, int color) {
        Outliner.getInstance().showAABB(Pair.of("wireFace", BlockFace.of(pos, dir)), FaceOutlines.getOutline(dir).move(pos))
                .colored(color)
                .lineWidth(1 / 16f);
    }

    private static final AABB UNIT_CUBE = AABB.unitCubeFromLowerCorner(Vec3.ZERO);
    private static void drawOutline(Level level, BlockPos pos, int color) {
        BlockState state = level.getBlockState(pos);
        AABB box = state.getShape(level, pos).isEmpty() ? UNIT_CUBE : state.getShape(level, pos).bounds();

        Outliner.getInstance().showAABB(Pair.of("wireBlock",pos), box.move(pos))
                .colored(color)
                .lineWidth(1 / 16f);
    }

    public static @Nonnull String getCurrentChannel() {
        return channelSet.currentChannel();
    }
}
