package com.williambl.raycastengine

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import java.util.*

object ClientNetworkManager : ChannelInboundHandlerAdapter(), NetworkManager {

    val packetCallbackRegistry: MutableMap<String, (Packet) -> Unit> = mutableMapOf()

    var channel: Channel? = null

    override fun addPacketCallback(id: String, callback: (Packet) -> Unit) {
        packetCallbackRegistry[id] = callback
    }

    override fun sendPacketToAll(id: String, data: ByteBuf) {}

    override fun sendPacketToClient(id: String, data: ByteBuf, playerId: UUID) {}

    override fun sendPacketToServer(id: String, data: ByteBuf) {
        val buf = Unpooled.buffer()
        buf.writeString(id)
        buf.writeBytes(data)
        channel?.writeAndFlush(buf)
    }

    override fun receivePacket(id: String, packet: Packet) {
        packetCallbackRegistry[id]?.invoke(packet)
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        msg as ByteBuf
        try {
            println(msg.readableBytes())
            receivePacket(msg.readString(), Packet(ctx, msg.readBytes(msg.readableBytes())))
        } finally {
            msg.release()
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        println("exception: ${cause?.message}}\n${cause?.stackTrace}")
    }

    override fun channelActive(ctx: ChannelHandlerContext?) {
        super.channelActive(ctx)
        channel = ctx?.channel()
        sendPacketToServer("login", Unpooled.buffer().writeUUID(Main.myId))
    }

    override fun channelInactive(ctx: ChannelHandlerContext?) {
        super.channelInactive(ctx)
        if (ctx != null)
            channel = null
    }
}