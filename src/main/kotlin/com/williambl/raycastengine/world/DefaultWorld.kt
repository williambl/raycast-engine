package com.williambl.raycastengine.world

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.williambl.raycastengine.Main
import com.williambl.raycastengine.collision.AABBQuadTree
import com.williambl.raycastengine.collision.CollisionProvider
import com.williambl.raycastengine.events.Tickable
import com.williambl.raycastengine.gameobject.Collidable
import com.williambl.raycastengine.gameobject.GameObject
import com.williambl.raycastengine.render.RenderUtils
import com.williambl.raycastengine.render.Texture
import com.williambl.raycastengine.util.network.*
import io.netty.buffer.ByteBuf
import java.util.*


class DefaultWorld(override val map: Array<IntArray>) : World, CollisionProvider {

    lateinit var wallTextures: Array<Texture>

    private val gameObjects: MutableList<GameObject> = mutableListOf()

    private val gameObjectsMutex = Any()

    val gameObjectsToAdd = mutableListOf<GameObject>()
    val gameObjectsToRemove = mutableListOf<GameObject>()

    var floorColor = Triple(0.0f, 0.0f, 0.0f)
    var skyColor = Triple(1.0f, 1.0f, 1.0f)

    override var isClient: Boolean = false

    override val serializer: WorldSerializer
        get() = Serializer

    override val quadTree: AABBQuadTree = AABBQuadTree()

    override fun onStart() {}

    override fun tick() {
        gameObjects.forEach {
            if (it is Tickable)
                it.tick()
        }

        updateGameObjectLists()
    }

    override fun addGameObject(gameObject: GameObject) {
        gameObject.world = this
        gameObjectsToAdd.add(gameObject)
        if (gameObject is Collidable)
            addAABB(gameObject.getAABB())
    }

    override fun removeGameObject(gameObject: GameObject) {
        gameObjectsToRemove.add(gameObject)
        if (gameObject is Collidable)
            removeAABB(gameObject.getAABB())
    }

    override fun <T : Any> getGameObjectsOfType(klass: Class<T>): List<T> {
        return getGameObjects().filterIsInstance(klass)
    }

    override fun getGameObjects(): List<GameObject> {
        synchronized(gameObjectsMutex) {
            return gameObjects.toList()
        }
    }

    fun updateGameObjectLists() {
        synchronized(gameObjectsMutex) {
            gameObjects.addAll(gameObjectsToAdd)
            gameObjectsToAdd.clear()

            gameObjects.removeAll(gameObjectsToRemove)
            gameObjectsToRemove.clear()
        }
    }

    companion object Serializer : WorldSerializer {
        override fun fromJson(json: JsonObject): DefaultWorld {
            val mapArray = json.array<JsonArray<Int>>("map")

            val world = DefaultWorld(mapArray!!.map { it.toIntArray() }.toTypedArray())

            val worldProperties = json.obj("worldProperties")
            if (worldProperties != null) {
                val floorColor = worldProperties.array<Float>("floorColor")
                val skyColor = worldProperties.array<Float>("skyColor")
                if (floorColor != null)
                    world.floorColor = Triple(floorColor[0], floorColor[1], floorColor[2])
                if (skyColor != null)
                    world.skyColor = Triple(skyColor[0], skyColor[1], skyColor[2])
            }

            world.wallTextures = json.array<String>("wallTextures")!!.map {
                RenderUtils.getOrCreateTexture(it)
            }.toTypedArray()

            val gameObjects = json.array<JsonObject>("gameObjects")
            if (gameObjects != null) {
                for (gameObjectRepresentation in gameObjects) {
                    val gameObject = getObjectFromJson(gameObjectRepresentation) as GameObject?
                    if (gameObject == null) {
                        println(gameObjectRepresentation.string("class") + " is not a valid gameObject, skipping")
                        continue
                    }
                    val data = gameObjectRepresentation["data"]
                    if (data is JsonObject) gameObject.fromJson(data)

                    world.addGameObject(gameObject)
                }
            }

            return world
        }

        override fun fromBytes(buf: ByteBuf): DefaultWorld {
            val world = DefaultWorld(buf.read2DIntArray())

            world.floorColor = buf.readFloatTriple()
            world.skyColor = buf.readFloatTriple()

            world.wallTextures = buf.readStrings().map { RenderUtils.getOrCreateTexture(it) }.toTypedArray()

            for (i in 0 until buf.readInt()) {
                world.addGameObject(buf.readGameObject())
            }

            return world
        }

        override fun toJson(world: World, json: JsonObject) {
            if (world !is DefaultWorld)
                throw IllegalArgumentException("Wrong world type sent to duplex")

            json["map"] = world.map
            json["worldProperties"] = mapOf(
                    Pair("floorColor", arrayOf(world.floorColor.first, world.floorColor.second, world.floorColor.third)),
                    Pair("skyColor", arrayOf(world.skyColor.first, world.skyColor.second, world.skyColor.third))
            )
            json["wallTextures"] = world.wallTextures.map { it.location }.toTypedArray()
            json["gameObjects"] = world.gameObjects.map {
                val gObjJson = writeObjectToJson(it.javaClass)
                it.toJson(gObjJson)
                gObjJson
            }
        }

        override fun toBytes(world: World, buf: ByteBuf, destinationId: UUID?) {
            if (world !is DefaultWorld)
                throw IllegalArgumentException("Wrong world type sent to duplex")

            world.updateGameObjectLists()

            buf.write2DIntArray(world.map)

            buf.writeFloatTriple(world.floorColor)
            buf.writeFloatTriple(world.skyColor)

            buf.writeStrings(world.wallTextures.map { it.location })

            val gameObjects = Main.world.getGameObjects()
            buf.writeInt(gameObjects.size)
            gameObjects.forEach {
                buf.writeGameObject(it)
            }
        }

    }

}