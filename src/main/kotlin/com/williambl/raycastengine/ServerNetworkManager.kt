package com.williambl.raycastengine

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

object ServerNetworkManager : ChannelInboundHandlerAdapter(), NetworkManager {

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

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        msg as ByteBuf
        try {
            msg.readString()
        } finally {
            msg.release()
        }
    }
}