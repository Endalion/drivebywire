package edn.stratodonut.drivebywire

import edn.stratodonut.drivebywire.network.*
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.util.Identifier

object WirePackets {
    val FULL_SYNC = DriveByWireMod.getResource("full_sync")
    val CREATE_CONNECTION = DriveByWireMod.getResource("create_connection")
    val REMOVE_CONNECTION = DriveByWireMod.getResource("remove_connection")
    val REQUEST_SYNC = DriveByWireMod.getResource("request_sync")
    val LINK_NETWORKS = DriveByWireMod.getResource("link_networks")

    fun registerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(CREATE_CONNECTION) { server, player, handler, buf, responseSender ->
            WireAddConnectionPacket.handle(server, player, buf)
        }
        
        ServerPlayNetworking.registerGlobalReceiver(REMOVE_CONNECTION) { server, player, handler, buf, responseSender ->
            WireRemoveConnectionPacket.handle(server, player, buf)
        }
        
        ServerPlayNetworking.registerGlobalReceiver(REQUEST_SYNC) { server, player, handler, buf, responseSender ->
            WireNetworkRequestSyncPacket.handle(server, player, buf)
        }
        
        ServerPlayNetworking.registerGlobalReceiver(LINK_NETWORKS) { server, player, handler, buf, responseSender ->
            WireLinkNetworksPacket.handle(server, player, buf)
        }
    }
}