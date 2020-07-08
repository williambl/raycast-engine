package com.williambl.raycastengine.world

import com.williambl.raycastengine.*
import com.williambl.raycastengine.collision.AABBQuadTree
import com.williambl.raycastengine.collision.CollisionProvider
import com.williambl.raycastengine.events.Tickable
import com.williambl.raycastengine.gameobject.Collidable
import com.williambl.raycastengine.gameobject.GameObject
import com.williambl.raycastengine.render.Texture
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import java.util.*


class DefaultWorld(override val map: Array<IntArray>) : World, CollisionProvider {

    lateinit var wallTextures: Array<Texture>

    var gameObjects: ArrayList<GameObject> = arrayListOf()
        private set

    val gameObjectsToAdd = mutableListOf<GameObject>()
    val gameObjectsToRemove = mutableListOf<GameObject>()

    var floorColor = Triple(0.0f, 0.0f, 0.0f)
    var skyColor = Triple(1.0f, 1.0f, 1.0f)

    override var isClient: Boolean = false

    override val quadTree: AABBQuadTree = AABBQuadTree()

    override fun onStart() {}

    override fun tick() {
        gameObjects.forEach {
            if (it is Tickable)
                it.tick()
        }

        updateGameObjectLists()

        if (!isClient) {
            gameObjects.forEach {
                if (it.isDirty) {
                    val buf = Unpooled.buffer()
                    it.toBytes(buf)
                    ServerNetworkManager.sendPacketToAll("gameObjectUpdate", buf)
                }
            }
        }
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
        return gameObjects.filterIsInstance(klass)
    }

    override fun toBytes(buf: ByteBuf, destinationId: UUID?) {
        updateGameObjectLists()

        buf.write2DIntArray(map)

        buf.writeFloatTriple(floorColor)
        buf.writeFloatTriple(skyColor)

        buf.writeStrings(wallTextures.map { it.location })

        val gameObjects = Main.world.getGameObjectsOfType(GameObject::class.java)
        buf.writeInt(gameObjects.size)
        gameObjects.forEach {
            buf.writeGameObject(it)
        }
    }

    fun updateGameObjectLists() {
        gameObjects.addAll(gameObjectsToAdd)
        gameObjectsToAdd.clear()

        gameObjects.removeAll(gameObjectsToRemove)
        gameObjectsToRemove.clear()
    }

}