package com.williambl.raycastengine


class World(val map: Array<Array<Int>>) {

    val wallTextures = arrayOf(
            Texture(""),
            Texture("brick.png"),
            Texture("stone.png")
    )
}