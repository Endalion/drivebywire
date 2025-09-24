package edn.stratodonut.drivebywire

import edn.stratodonut.drivebywire.compat.LinkedControllerWireServerHandler
import edn.stratodonut.drivebywire.compat.TweakedControllerWireServerHandler
import edn.stratodonut.drivebywire.wire.ShipWireNetworkManager
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionResult
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.api.ships.Ship
import org.valkyrienskies.mod.common.getShipManagingPos
import java.util.*

object ServerEvents {
    fun register() {
        ServerTickEvents.END_WORLD_TICK.register { world ->
            if (world is ServerLevel) {
                LinkedControllerWireServerHandler.tick(world)
                TweakedControllerWireServerHandler.tick(world)
            }
        }
    }

    fun onBlockUpdate(level: ServerLevel, pos: BlockPos, notifiedSides: Set<Direction>) {
        val ship = level.getShipManagingPos(pos)
        if (ship is ServerShip) {
            val state = level.getBlockState(pos)
            if (state.isSignalSource) {
                val maxSignal = Direction.values().maxOfOrNull { dir ->
                    state.getSignal(level, pos, dir)
                } ?: 0
                
                ShipWireNetworkManager.get(ship)?.setSource(
                    level, pos, ShipWireNetworkManager.WORLD_REDSTONE_CHANNEL, maxSignal
                )
            }

            for (dir in notifiedSides) {
                val neighborPos = pos.relative(dir)
                val neighborState = level.getBlockState(neighborPos)
                if (!neighborState.isSignalSource) {
                    ShipWireNetworkManager.get(ship)?.setSource(
                        level, neighborPos, ShipWireNetworkManager.WORLD_REDSTONE_CHANNEL,
                        level.getBestNeighborSignal(neighborPos)
                    )
                }
            }
        }
    }
}