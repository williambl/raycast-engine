package com.williambl.raycastengine.world

import com.williambl.raycastengine.events.StartupListener
import com.williambl.raycastengine.events.Tickable
import com.williambl.raycastengine.gameobject.GameObject

interface World: StartupListener, Tickable {

    val map: Array<IntArray>

    fun addGameObject(gameObject: GameObject)

    fun removeGameObject(gameObject: GameObject)

    fun <T : Any> getGameObjectsOfType(klass: Class<T>): List<T>
}