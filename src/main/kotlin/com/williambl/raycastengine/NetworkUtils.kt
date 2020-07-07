package com.williambl.raycastengine

import io.netty.buffer.ByteBuf

@ExperimentalStdlibApi
fun ByteBuf.writeString(string: String) {
    val bytes = string.encodeToByteArray()
    writeInt(bytes.size)
    writeBytes(bytes)
}

fun ByteBuf.readString(): String = String(readBytes(readInt()).array())
