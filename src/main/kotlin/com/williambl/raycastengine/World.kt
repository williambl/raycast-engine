package com.williambl.raycastengine

import com.williambl.raycastengine.events.StartupListener
import com.williambl.raycastengine.events.Tickable
import com.williambl.raycastengine.gameobject.GameObject
import com.williambl.raycastengine.gameobject.MovingSprite
import com.williambl.raycastengine.render.Texture
import kotlin.reflect.jvm.kotlinFunction


class World(val map: Array<IntArray>) : StartupListener, Tickable {

    lateinit var wallTextures: Array<Texture>

    var gameObjects: ArrayList<GameObject> = arrayListOf()
        private set

    override fun onStart() {
        wallTextures = arrayOf(
                Texture(""),
                Texture("/brick.png"),
                Texture("/stone.png")
        )

        addGameObject(Main.player)

    }

    override fun tick() {
        gameObjects.forEach {
            if (it is Tickable)
                it.tick()
        }
    }

    fun addGameObject(gameObject: GameObject) {
        gameObjects.add(gameObject)
    }

    fun removeGameObject(gameObject: GameObject) {
        gameObjects.remove(gameObject)
    }

    fun <T : GameObject> getGameObjectsOfType(klass: Class<T>): List<T> {
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