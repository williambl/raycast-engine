package com.williambl.raycastengine

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import java.util.*
import java.util.function.Supplier

object ServerNetworkManager : NetworkManager, Supplier<ChannelInboundHandlerAdapter> {

    val packetCallbackRegistry: MutableMap<String, (Packet) -> Unit> = mutableMapOf()

    val channels: BiMap<UUID, Channel> = HashBiMap.create()

    override fun addPacketCallback(id: String, callback: (Packet) -> Unit) {
        packetCallbackRegistry[id] = callback
    }

    override fun sendPacketToAll(id: String, data: ByteBuf) {
        val buf = Unpooled.buffer()
        buf.writeString(id)
        buf.writeBytes(data)
        channels.values.forEach {
            it.writeAndFlush(Unpooled.copiedBuffer(buf))
        }
    }

    override fun sendPacketToClient(id: String, data: ByteBuf, playerId: UUID) {
        val buf = Unpooled.buffer()
        buf.writeString(id)
        buf.writeBytes(data)
        channels[playerId]?.writeAndFlush(buf)
    }

    override fun sendPacketToServer(id: String, data: ByteBuf) {}

    override fun receivePacket(id: String, packet: Packet) {
        packetCallbackRegistry[id]?.invoke(packet)
    }

    override fun get(): ChannelInboundHandlerAdapter {
        return object : ChannelInboundHandlerAdapter() {
            override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
                msg as ByteBuf
                try {
                    receivePacket(msg.readString(), Packet(ctx, msg))
                } finally {
                    msg.release()
                }
            }

            override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
                println("exception: ${cause?.message}}")
            }

            override fun channelInactive(ctx: ChannelHandlerContext?) {
                super.channelInactive(ctx)
                if (ctx != null)
                    channels.remove(channels.map { it.value to it.key }.toMap()[ctx.channel()])
            }
        }
    }
}