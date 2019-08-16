package com.williambl.raycastengine


class World(val map: Array<Array<Int>>): StartupListener {

    lateinit var wallTextures: Array<Texture>

    override fun onStart() {
        wallTextures = arrayOf(
                Texture(""),
                Texture("/brick.png"),
                Texture("/stone.png")
        )
    }
}