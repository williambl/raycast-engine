package com.williambl.raycastengine.gameobject

import com.beust.klaxon.JsonObject
import com.williambl.raycastengine.util.network.readUUID
import com.williambl.raycastengine.util.network.writeUUID
import com.williambl.raycastengine.util.synced
import com.williambl.raycastengine.world.World
import io.netty.buffer.ByteBuf
import java.util.*

/**
 * An object in the game world.
 * This is similar to an 'entity' in Minecraft or a 'GameObject' in Unity.
 *
 * A GameObject is added to the world with [World.addGameObject]
 * and removed from the world with [World.removeGameObject]
 * (Note that this is not currently synced between players)
 *
 * Making GameObjects multiplayer-safe requires some work:
 *
 *  - To have a property auto-synced across from server->client on change, use the [synced] delegate.
 *  - Any properties that must be synced from server->client on initial connect **must** be handled in [toBytes] and [fromBytes]
 *  - (This includes auto-synced properties)
 *  - **All constructor parameters must have a default value**. (If you're writing in Java, your GameObject must have a 0-arg constructor).
 *  This is because on the client, GameObjects are first created with a 0-arg constructor and their data is given to them with [fromBytes].
 */
open class GameObject(xIn: Double = 0.0, yIn: Double = 0.0) {

    var x: Double by synced(xIn, ::id, ByteBuf::writeDouble, ByteBuf::readDouble)
    var y: Double by synced(yIn, ::id, ByteBuf::writeDouble, ByteBuf::readDouble)

    lateinit var world: World

    open var id: UUID = UUID.randomUUID()

    open fun toBytes(byteBuf: ByteBuf) {
        byteBuf.writeUUID(id)
        byteBuf.writeDouble(x)
        byteBuf.writeDouble(y)
    }

    open fun fromBytes(byteBuf: ByteBuf) {
        id = byteBuf.readUUID()
        x = byteBuf.readDouble()
        y = byteBuf.readDouble()
    }

    open fun toJson(json: JsonObject) {
        json["id"] = id.toString()
        json["x"] = x
        json["y"] = y
    }

    open fun fromJson(json: JsonObject) {
        id = UUID.fromString(json["uuid"].toString())
        x = json["x"] as Double
        y = json["y"] as Double
    }
}