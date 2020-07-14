package com.williambl.raycastengine

import com.beust.klaxon.JsonBase
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.williambl.raycastengine.gameobject.GameObject
import io.netty.buffer.ByteBuf
import java.util.*

@UseExperimental(ExperimentalStdlibApi::class)
fun ByteBuf.writeString(string: String): ByteBuf {
    val bytes = string.encodeToByteArray()
    writeInt(bytes.size)
    writeBytes(bytes)
    return this
}

fun ByteBuf.readString(): String = String(readBytes(readInt()).toByteArray())

fun ByteBuf.toByteArray(): ByteArray {
    return if (this.hasArray())
        array()
    else {
        val bytes = mutableListOf<Byte>()
        while (readableBytes() > 0)
            bytes.add(readByte())
        bytes.toByteArray()
    }
}

fun ByteBuf.writeStrings(collection: Collection<String>) {
    writeInt(collection.size)
    collection.forEach { writeString(it) }
}

fun ByteBuf.readStrings(): Array<String> = Array(readInt()) { readString() }

fun ByteBuf.writeUUID(uuid: UUID): ByteBuf = writeString(uuid.toString())

fun ByteBuf.readUUID() = UUID.fromString(readString())

fun ByteBuf.writeDoublePair(pair: Pair<Double, Double>): ByteBuf {
    writeDouble(pair.first)
    writeDouble(pair.second)
    return this
}

fun ByteBuf.readDoublePair(): Pair<Double, Double> = Pair(readDouble(), readDouble())

fun ByteBuf.writeDoubleTriple(triple: Triple<Double, Double, Double>): ByteBuf {
    writeDouble(triple.first)
    writeDouble(triple.second)
    writeDouble(triple.third)
    return this
}

fun ByteBuf.readDoubleTriple(): Triple<Double, Double, Double> = Triple(readDouble(), readDouble(), readDouble())

fun ByteBuf.writeFloatTriple(triple: Triple<Float, Float, Float>): ByteBuf {
    writeFloat(triple.first)
    writeFloat(triple.second)
    writeFloat(triple.third)
    return this
}

fun ByteBuf.readFloatTriple(): Triple<Float, Float, Float> = Triple(readFloat(), readFloat(), readFloat())

fun ByteBuf.writeJson(json: JsonBase) = writeString(json.toJsonString())

fun ByteBuf.readJson(): Any = Parser.default().parse(readString().reader())

fun ByteBuf.writeObject(klass: Class<*>?, constructor: Int, vararg args: Any?): ByteBuf {
    writeString(writeObjectToJson(klass, args).toJsonString())
    return this
}

fun ByteBuf.readObject(): Any? {
    return getObjectFromJson(Parser.default().parse(readString().reader()) as JsonObject)
}

fun ByteBuf.writeGameObject(gameObject: GameObject): ByteBuf {
    writeString(gameObject::class.java.canonicalName)
    gameObject.toBytes(this)
    return this
}

fun ByteBuf.readGameObject(): GameObject {
    val gameObject = Class.forName(readString()).constructors.first { it.parameterCount == 0 }.newInstance() as GameObject
    gameObject.fromBytes(this)
    return gameObject
}

fun ByteBuf.writeIntArray(array: IntArray) {
    writeInt(array.size)
    array.forEach { writeInt(it) }
}

fun ByteBuf.readIntArray(): IntArray {
    return IntArray(readInt()) { readInt() }
}

fun ByteBuf.write2DIntArray(array: Array<IntArray>) {
    writeInt(array.size)
    array.forEach { writeIntArray(it) }
}

fun ByteBuf.read2DIntArray(): Array<IntArray> {
    return Array(readInt()) { readIntArray() }
}