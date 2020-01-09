package com.williambl.raycastengine.world

import com.williambl.raycastengine.Main
import com.williambl.raycastengine.events.Tickable
import com.williambl.raycastengine.gameobject.GameObject
import com.williambl.raycastengine.render.Texture


class DefaultWorld(override val map: Array<IntArray>) : World {

    lateinit var wallTextures: Array<Texture>

    var gameObjects: ArrayList<GameObject> = arrayListOf()
        private set

    var floorColor = Triple(0.0f, 0.0f, 0.0f)
    var skyColor = Triple(1.0f, 1.0f, 1.0f)

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
    }

    override fun removeGameObject(gameObject: GameObject) {
        gameObjects.remove(gameObject)
    }

    override fun <T : Any> getGameObjectsOfType(klass: Class<T>): List<T> {
        return gameObjects.filterIsInstance(klass)
    }

    /*
     * Creates a new gameObject from the classname and arguments. Only works if all the arguments are primitives.
     */
    override fun createGameObject(className: String, constructor: Int, vararg args: Any): GameObject? {
        println(Main.gameObjectClasses[className]?.constructors?.contentToString())
        return Main.gameObjectClasses[className]?.constructors?.get(constructor)?.newInstance(*args) as GameObject?
    }
}