package com.williambl.raycastengine.tater

import com.williambl.raycastengine.Main
import com.williambl.raycastengine.collision.AxisAlignedBoundingBox
import com.williambl.raycastengine.gameobject.Collidable
import com.williambl.raycastengine.gameobject.Interactable
import com.williambl.raycastengine.gameobject.Player
import com.williambl.raycastengine.gameobject.Sprite
import com.williambl.raycastengine.render.gui.Gui
import com.williambl.raycastengine.util.raytrace.RaytraceResult
import imgui.ImGui

class TaterNPC(textureLoc: String, x: Double, y: Double) : Sprite(textureLoc, x, y), Interactable, Collidable {
    val gui = TaterGui()

    init {
        Main.renderTickables.add(gui)
    }

    override fun getAABB(): AxisAlignedBoundingBox = AxisAlignedBoundingBox(x - 0.5, y - 0.5, x + 0.5, y + 0.5, this)

    override fun interact(player: Player, raytraceResult: RaytraceResult<*>) {
        gui.shouldBeOpen = true
    }

    class TaterGui: Gui {
        var shouldBeOpen = false
        var currentIndex = 0
        val texts = listOf(
            "Hello traveler",
            "It is I, the mighty tater.",
            "jtekrnhgjkrnjkd",
            "I'm pretty Neat, aren't I?"
        )
        override fun renderGui(): ImGui.() -> Any? = {
            if (shouldBeOpen) {
                begin("Hi There!", ::shouldBeOpen)
                text(texts[currentIndex])
                if (button("Next")) { if (currentIndex == texts.size-1) shouldBeOpen = false else currentIndex++ }
            }
        }
    }
}