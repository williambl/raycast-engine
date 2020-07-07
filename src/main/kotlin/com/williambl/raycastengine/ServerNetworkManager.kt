package com.williambl.raycastengine

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import java.util.function.Supplier

object ServerNetworkManager : NetworkManager, Supplier<ChannelInboundHandlerAdapter> {

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


    override fun get(): ChannelInboundHandlerAdapter {
        return object : ChannelInboundHandlerAdapter() {
            override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
                msg as ByteBuf
                try {
                    recievePacket(msg.readString(), msg.readBytes(msg.readableBytes()))
                } finally {
                    msg.release()
                }
            }

            override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
                println("exception: ${cause?.message}}")
            }
        }
    }
}