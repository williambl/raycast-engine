package com.williambl.raycastengine.network

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import java.util.*

data class Packet(val ctx: ChannelHandlerContext, val buf: ByteBuf) {
    fun getId(): UUID? {
        return ServerNetworkManager.channels.inverse()[ctx.channel()]
    }
}