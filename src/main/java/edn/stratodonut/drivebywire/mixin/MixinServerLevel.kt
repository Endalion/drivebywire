package edn.stratodonut.drivebywire.mixin

import edn.stratodonut.drivebywire.wire.ShipWireNetworkManager
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.mod.common.getShipManagingPos
import kotlin.math.max

@Mixin(ServerLevel::class)
class MixinServerLevel {
    @Inject(method = ["getSignal"], at = [At("RETURN")], cancellable = true)
    private fun onGetSignal(pos: BlockPos, direction: Direction, cir: CallbackInfoReturnable<Int>) {
        val level = this as ServerLevel
        val original = cir.returnValue
        val target = pos.relative(direction.opposite)
        
        val ship = level.getShipManagingPos(target)
        if (ship is ServerShip) {
            val wireSignal = ShipWireNetworkManager.get(ship)?.getSignalAt(target, direction) ?: 0
            cir.returnValue = max(original, wireSignal)
        }
    }
}