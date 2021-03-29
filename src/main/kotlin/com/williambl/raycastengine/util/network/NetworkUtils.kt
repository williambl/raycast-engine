package com.williambl.raycastengine.util.network

import com.beust.klaxon.JsonBase
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.williambl.raycastengine.gameobject.GameObject
import io.netty.buffer.ByteBuf
import java.util.*

/**
 * Writes a string to the buffer.
 *
 * The string is written as length-prefixed UTF-8.
 */
@OptIn(ExperimentalStdlibApi::class)
fun ByteBuf.writeString(string: String): ByteBuf {
    val bytes = string.encodeToByteArray()
    writeInt(bytes.size)
    writeBytes(bytes)
    return this
}

/**
 * Reads a length-prefixed UTF-8 string from the buffer.
 */
fun ByteBuf.readString(): String = String(readBytes(readInt()).toByteArray())

/**
 * Converts the buffer to a [ByteArray].
 *
 * If this buffer is array-backed, it simply returns the array. Else, it copies its contents into the returned array.
 *
 * The buffer is [released][io.netty.util.ReferenceCounted] afterwards, so don't use it.
 */
fun ByteBuf.toByteArray(): ByteArray {
    val result = if (this.hasArray()) {
        array()
    } else {
        val bytes = mutableListOf<Byte>()
        while (readableBytes() > 0)
            bytes.add(readByte())
        bytes.toByteArray()
    }
    this.release()
    return result
}

/**
 * Writes a collection of strings to the buffer.
 */
fun ByteBuf.writeStrings(collection: Collection<String>) {
    writeInt(collection.size)
    collection.forEach { writeString(it) }
}

/**
 * Reads a [List] of strings from the buffer.
 */
fun ByteBuf.readStrings(): List<String> = List(readInt()) { readString() }

/**
 * Writes a [UUID] to the buffer, in string form.
 */
fun ByteBuf.writeUUID(uuid: UUID): ByteBuf = writeString(uuid.toString())

/**
 * Reads a [UUID] from the buffer.
 */
fun ByteBuf.readUUID(): UUID = UUID.fromString(readString())

/**
 * Writes a [Pair] of [Double]s to the buffer.
 */
fun ByteBuf.writeDoublePair(pair: Pair<Double, Double>): ByteBuf {
    writeDouble(pair.first)
    writeDouble(pair.second)
    return this
}

/**
 * Reads a [Pair] of [Double]s from the buffer.
 */
fun ByteBuf.readDoublePair(): Pair<Double, Double> = Pair(readDouble(), readDouble())

/**
 * Writes a [Triple] of [Double]s to the buffer.
 */
fun ByteBuf.writeDoubleTriple(triple: Triple<Double, Double, Double>): ByteBuf {
    writeDouble(triple.first)
    writeDouble(triple.second)
    writeDouble(triple.third)
    return this
}

/**
 * Reads a [Triple] of [Double]s from the buffer.
 */
fun ByteBuf.readDoubleTriple(): Triple<Double, Double, Double> = Triple(readDouble(), readDouble(), readDouble())

/**
 * Writes a [Triple] of [Float]s to the buffer.
 */
fun ByteBuf.writeFloatTriple(triple: Triple<Float, Float, Float>): ByteBuf {
    writeFloat(triple.first)
    writeFloat(triple.second)
    writeFloat(triple.third)
    return this
}

/**
 * Reads a [Triple] of [Float]s from the buffer.
 */
fun ByteBuf.readFloatTriple(): Triple<Float, Float, Float> = Triple(readFloat(), readFloat(), readFloat())

/**
 * Writes a [JSON Object or Array][JsonBase] as a string to the buffer.
 */
fun ByteBuf.writeJson(json: JsonBase) = writeString(json.toJsonString())

/**
 * Reads a JSON Object from the buffer
 */
fun ByteBuf.readJson(): Any = Parser.default().parse(readString().reader())

/**
 * Writes [Any object][Any]'s class and constructor args to the buffer.
 *
 * @param klass the [Class] of the object
 * @param args the constructor arguments to be used to create the object
 *
 * @throws [IllegalArgumentException] if any of [args] are not JSON-representable
 *
 * @see writeObjectToJson
 * @see readObject
 */
fun ByteBuf.writeObject(klass: Class<*>?, vararg args: Any?): ByteBuf {
    writeJson(writeObjectToJson(klass, args))
    return this
}

/**
 * Creates any object from the buffer.
 *
 * @see getObjectFromJson
 * @see writeObject
 */
fun ByteBuf.readObject(): Any? {
    return getObjectFromJson(readJson() as JsonObject)
}

/**
 * Writes a [GameObject] to the buffer.
 *
 * The class name is written first, followed by the result of gameobject's [GameObject.toBytes].
 *
 * @see readGameObject
 */
fun ByteBuf.writeGameObject(gameObject: GameObject): ByteBuf {
    writeString(gameObject::class.java.canonicalName)
    gameObject.toBytes(this)
    return this
}

/**
 * Reads a [GameObject] from the buffer.
 *
 * The class name is read, the zero-param constructor is called, and then [GameObject.fromBytes] is called.
 *
 * @see writeGameObject
 */
fun ByteBuf.readGameObject(): GameObject {
    val gameObject = Class.forName(readString()).constructors.first { it.parameterCount == 0 }.newInstance() as GameObject
    gameObject.fromBytes(this)
    return gameObject
}

/**
 * Writes an [IntArray] to the buffer.
 */
fun ByteBuf.writeIntArray(array: IntArray) {
    writeInt(array.size)
    array.forEach { writeInt(it) }
}

/**
 * Reads an [IntArray] from the buffer.
 */
fun ByteBuf.readIntArray(): IntArray {
    return IntArray(readInt()) { readInt() }
}

/**
 * Writes a 2-dimensional int array to the buffer.
 */
fun ByteBuf.write2DIntArray(array: Array<IntArray>) {
    writeInt(array.size)
    array.forEach { writeIntArray(it) }
}

/**
 * Reads a 2-dimensional int array from the buffer.
 */
fun ByteBuf.read2DIntArray(): Array<IntArray> {
    return Array(readInt()) { readIntArray() }
}