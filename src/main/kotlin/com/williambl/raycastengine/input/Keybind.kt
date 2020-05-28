package com.williambl.raycastengine.input

class Keybind(val name: String, var key: Int) {
    var pressed: Boolean = false
        private set

    fun down() {
        pressed = true
    }

    fun up() {
        pressed = false
    }
}