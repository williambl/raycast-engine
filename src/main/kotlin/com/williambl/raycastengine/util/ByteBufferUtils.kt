package com.williambl.raycastengine.util

import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.io.InputStream
import java.nio.ByteBuffer

/*
 * This is all `Silk#6031`'s work.
 */


/**
 * Converts an input stream to a ByteBuffer
 */
internal fun InputStream.toByteBuffer(): ByteBuffer = ByteBuffer.wrap(this.readBytes()).asReadOnlyBuffer()!!

/**
 * Copies a native buffer onto a heap-based one
 */
internal fun ByteBuffer.toUnmanaged(rewind: Boolean = true): ByteBuffer {
    val unmanaged = ByteBuffer.allocate(this.capacity())
    this.copyOnto(unmanaged, rewind)
    return unmanaged
}

/**
 * Copies a buffer onto a native stack-based one, useful for a small amount of data.
 */
internal fun <R> ByteBuffer.toStackManaged(rewind: Boolean = true, block: (ByteBuffer) -> R): R {
    return MemoryStack.stackPush().use {
        val stackBuffer = it.malloc(this.capacity())
        this.copyOnto(stackBuffer, rewind)
        block(stackBuffer)
    }
}

/**
 * Copies a buffer onto a native memory one, which can store a lot of data.
 */
internal fun ByteBuffer.toMemoryManaged(rewind: Boolean = true): ByteBuffer {
    val managed = MemoryUtil.memAlloc(this.capacity())
    this.copyOnto(managed, rewind)
    return managed
}

/**
 * Frees a native memory buffer
 */
internal fun ByteBuffer.freeMemoryManaged() = if (this.isDirect) MemoryUtil.memFree(this) else throw IllegalArgumentException("Unable to free an unmanaged buffer '$this'")

/**
 * Copies a buffer onto another
 */
internal fun ByteBuffer.copyOnto(other: ByteBuffer, rewind: Boolean = true) {
    other.put(this)
    other.rewind()
    if (rewind) this.rewind()
}