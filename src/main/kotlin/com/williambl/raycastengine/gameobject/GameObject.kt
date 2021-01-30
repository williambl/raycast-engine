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

    /**
     * The x coordinate of the GameObject. Synced.
     */
    var x: Double by synced(xIn, ::id, ByteBuf::writeDouble, ByteBuf::readDouble)

    /**
     * The y coordinate of the GameObject. Synced.
     */
    var y: Double by synced(yIn, ::id, ByteBuf::writeDouble, ByteBuf::readDouble)

    /**
     * The world this GameObject belongs to.
     */
    lateinit var world: World

    /**
     * This GameObject's UUID, which can be used to uniquely identify it, even across different clients.
     */
    open var id: UUID = UUID.randomUUID()

    /**
     * Serialise the GameObject to bytes.
     *
     * The data must be stored in the same order it's read in [fromBytes]
     */
    open fun toBytes(byteBuf: ByteBuf) {
        byteBuf.writeUUID(id)
        byteBuf.writeDouble(x)
        byteBuf.writeDouble(y)
    }

    /**
     * Deserialise the GameObject from bytes.
     *
     * The data must be read back in the same order it's written in [toBytes]
     */
    open fun fromBytes(byteBuf: ByteBuf) {
        id = byteBuf.readUUID()
        x = byteBuf.readDouble()
        y = byteBuf.readDouble()
    }

    /**
     * Serialise the GameObject to JSON.
     *
     * This is currently not used for anything; it may be used in future for a level editor.
     */
    open fun toJson(json: JsonObject) {
        json["id"] = id.toString()
        json["x"] = x
        json["y"] = y
    }

    /**
     * Deserialise an object from JSON.
     *
     * This can be used in world files where `data` is used to specify GameObject data rather than `args`.
     */
    open fun fromJson(json: JsonObject) {
        id = UUID.fromString(json["uuid"].toString())
        x = json["x"] as Double
        y = json["y"] as Double
    }
}