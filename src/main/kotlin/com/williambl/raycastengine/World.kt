package com.williambl.raycastengine

import com.williambl.raycastengine.events.StartupListener
import com.williambl.raycastengine.events.Tickable
import com.williambl.raycastengine.gameobject.GameObject
import com.williambl.raycastengine.gameobject.Light
import com.williambl.raycastengine.gameobject.MovingSprite
import com.williambl.raycastengine.render.Texture
import kotlin.reflect.jvm.kotlinFunction


class World(val map: Array<Array<Int>>) : StartupListener, Tickable {

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

        addGameObject(createGameObject("com.williambl.raycastengine.gameobject.Light", 2.0, 2.0, 5.0, 3.0, 0.0)!!)
        addGameObject(Light(7.0, 8.0, 5.0))

        addGameObject(MovingSprite(Texture("/face.png"), 5.0, 5.0))
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
    fun createGameObject(className: String, vararg args: Any): GameObject? {
        val argClasses = args.map {
            it::class.javaPrimitiveType
        }.toTypedArray()

        return Main.gameObjectClasses[className]?.getConstructor(*argClasses)?.kotlinFunction?.call(*args)
    }
}