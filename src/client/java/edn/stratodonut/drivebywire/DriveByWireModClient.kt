package edn.stratodonut.drivebywire

import net.fabricmc.api.ClientModInitializer

object DriveByWireModClient : ClientModInitializer {
    override fun onInitializeClient() {
        WirePonders.register()
    }
}