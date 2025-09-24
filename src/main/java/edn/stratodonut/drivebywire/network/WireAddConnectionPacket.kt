package edn.stratodonut.drivebywire.network

import com.simibubi.create.foundation.utility.Components
import edn.stratodonut.drivebywire.WireSounds
import edn.stratodonut.drivebywire.wire.ShipWireNetworkManager
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Style
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.api.ships.Ship
import org.valkyrienskies.mod.common.getShipManagingPos

object WireAddConnectionPacket {
    fun create(shipId: Long, start: BlockPos, end: BlockPos, dir: Direction, channel: String): FriendlyByteBuf {
        val buf = PacketByteBufs.create()
        buf.writeLong(shipId)
        buf.writeLong(start.asLong())
        buf.writeLong(end.asLong())
        buf.writeInt(dir.get3DDataValue())
        buf.writeUtf(channel)
        return buf
    }

    fun handle(server: MinecraftServer, player: ServerPlayer, buf: FriendlyByteBuf) {
        val shipId = buf.readLong()
        val start = buf.readLong()
        val end = buf.readLong()
        val dir = buf.readInt()
        val channel = buf.readUtf()

        server.execute {
            val ship = player.level().getShipManagingPos(BlockPos.of(start))
            if (ship is ServerShip) {
                val manager = ShipWireNetworkManager.getOrCreate(ship)
                val result = manager.createConnection(
                    player.level(),
                    BlockPos.of(start),
                    BlockPos.of(end),
                    Direction.from3DDataValue(dir),
                    channel
                )
                
                if (result.isSuccess()) {
                    player.level().playSound(
                        null,
                        BlockPos.of(end),
                        WireSounds.PLUG_IN,
                        SoundSource.BLOCKS,
                        1.0f,
                        1.0f
                    )
                } else {
                    player.displayClientMessage(
                        Components.literal(result.description)
                            .withStyle(Style.EMPTY.withColor(ChatFormatting.RED)),
                        true
                    )
                }
            }
        }
    }
}