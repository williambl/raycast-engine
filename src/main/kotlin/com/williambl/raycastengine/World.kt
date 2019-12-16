package com.williambl.raycastengine

import com.williambl.raycastengine.events.StartupListener
import com.williambl.raycastengine.events.Tickable
import com.williambl.raycastengine.gameobject.GameObject
import com.williambl.raycastengine.render.Texture


class World(val map: Array<IntArray>) : StartupListener, Tickable {

    lateinit var wallTextures: Array<Texture>

    var gameObjects: ArrayList<GameObject> = arrayListOf()
        private set

    var floorColor = Triple(0.0, 0.0, 0.0)
    var skyColor = Triple(1.0, 1.0, 1.0)

    override fun onStart() {}

    override fun tick() {
        gameObjects.forEach {
            if (it is Tickable)
                it.tick()
        }
    }

    fun addGameObject(gameObject: GameObject) {
        gameObject.world = this
        gameObjects.add(gameObject)
    }

    fun removeGameObject(gameObject: GameObject) {
        gameObjects.remove(gameObject)
    }

    fun <T : Any> getGameObjectsOfType(klass: Class<T>): List<T> {
        return gameObjects.filterIsInstance(klass)
    }

    /*
     * Creates a new gameObject from the classname and arguments. Only works if all the arguments are primitives.
     */
    fun createGameObject(className: String, constructor: Int, vararg args: Any): GameObject? {
        println(Main.gameObjectClasses[className]?.constructors?.contentToString())
        return Main.gameObjectClasses[className]?.constructors?.get(constructor)?.newInstance(*args) as GameObject?
    }
}