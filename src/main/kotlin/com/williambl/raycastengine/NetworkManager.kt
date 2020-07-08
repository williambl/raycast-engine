package com.williambl.raycastengine

import io.netty.buffer.ByteBuf
import java.util.*

interface NetworkManager {

    fun addPacketCallback(id: String, callback: (Packet) -> Unit)

    fun sendPacketToAll(id: String, data: ByteBuf)
    fun sendPacketToClient(id: String, data: ByteBuf, playerId: UUID)
    fun sendPacketToServer(id: String, data: ByteBuf)
    fun receivePacket(id: String, packet: Packet)
}