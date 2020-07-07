package com.williambl.raycastengine

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext

data class Packet(val ctx: ChannelHandlerContext, val buf: ByteBuf)