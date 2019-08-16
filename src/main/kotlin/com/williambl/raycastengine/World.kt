package com.williambl.raycastengine


class World(val map: Array<Array<Int>>): StartupListener {

    lateinit var wallTextures: Array<Texture>

    var lights: ArrayList<Light> = arrayListOf()

    override fun onStart() {
        wallTextures = arrayOf(
                Texture(""),
                Texture("/brick.png"),
                Texture("/stone.png")
        )

        lights.add(Light(2.0, 2.0, Triple(5.0, 3.0, 0.0)))
        lights.add(Light(7.0, 8.0, 5.0))
    }
}