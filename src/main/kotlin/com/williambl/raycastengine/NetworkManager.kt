package com.williambl.raycastengine

import io.netty.buffer.ByteBuf

interface NetworkManager {

    fun addPacketCallback(id: String, callback: (ByteBuf) -> Unit)

    fun sendPacket(id: String, data: ByteBuf)
    fun recievePacket(id: String, data: ByteBuf)
}