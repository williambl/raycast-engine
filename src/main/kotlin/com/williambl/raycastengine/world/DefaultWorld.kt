package com.williambl.raycastengine.world

import com.williambl.raycastengine.collision.AABBQuadTree
import com.williambl.raycastengine.collision.CollisionProvider
import com.williambl.raycastengine.events.Tickable
import com.williambl.raycastengine.gameobject.Collidable
import com.williambl.raycastengine.gameobject.GameObject
import com.williambl.raycastengine.render.Texture


class DefaultWorld(override val map: Array<IntArray>) : World, CollisionProvider {

    lateinit var wallTextures: Array<Texture>

    var gameObjects: ArrayList<GameObject> = arrayListOf()
        private set

    var floorColor = Triple(0.0f, 0.0f, 0.0f)
    var skyColor = Triple(1.0f, 1.0f, 1.0f)

    override val quadTree: AABBQuadTree = AABBQuadTree()

    override fun onStart() {}

    override fun tick() {
        gameObjects.forEach {
            if (it is Tickable)
                it.tick()
        }
    }

    override fun addGameObject(gameObject: GameObject) {
        gameObject.world = this
        gameObjects.add(gameObject)
        if (gameObject is Collidable)
            addAABB(gameObject.getAABB())
    }

    override fun removeGameObject(gameObject: GameObject) {
        gameObjects.remove(gameObject)
        if (gameObject is Collidable)
            removeAABB(gameObject.getAABB())
    }

    override fun <T : Any> getGameObjectsOfType(klass: Class<T>): List<T> {
        return gameObjects.filterIsInstance(klass)
    }

}