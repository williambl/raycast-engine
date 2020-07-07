package com.williambl.raycastengine.client

import com.williambl.raycastengine.NetworkManager
import io.netty.buffer.ByteBuf

object ClientNetworkManager : NetworkManager {

    val packetCallbackRegistry: MutableMap<String, (ByteBuf) -> Unit> = mutableMapOf()

    override fun addPacketCallback(id: String, callback: (ByteBuf) -> Unit) {
        packetCallbackRegistry[id] = callback
    }

    override fun sendPacket(id: String, data: ByteBuf) {
        //TODO
    }

    override fun recievePacket(id: String, data: ByteBuf) {
        packetCallbackRegistry[id]?.invoke(data)
    }
}