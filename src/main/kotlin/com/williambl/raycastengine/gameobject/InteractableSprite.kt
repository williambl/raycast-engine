package com.williambl.raycastengine.gameobject

import com.beust.klaxon.JsonObject
import com.williambl.raycastengine.Main
import com.williambl.raycastengine.collision.AxisAlignedBoundingBox
import com.williambl.raycastengine.render.gui.Gui
import com.williambl.raycastengine.util.network.writeGameObject
import com.williambl.raycastengine.util.raytrace.RaytraceResult
import imgui.ImGui
import io.netty.buffer.Unpooled

class InteractableSprite(textureLoc: String = "", x: Double = 0.0, y: Double = 0.0) : Sprite(textureLoc, x, y), Interactable, Collidable {

    val gui = MyGui()

    inner class MyGui : Gui {
        var message = ""
        var shouldBeOpen = false

        override fun renderGui(): ImGui.() -> Any? = {
            if (shouldBeOpen) {
                begin("Hi there!", ::shouldBeOpen)
                text("As JSON, I am:")
                textWrapped(message)
                end()
            }
        }
    }

    init {
        Main.renderTickables.add(gui)
    }

    override fun interact(player: Player, raytraceResult: RaytraceResult<*>) {
        gui.shouldBeOpen = true
        val json = JsonObject()
        toJson(json)
        gui.message = json.toJsonString(prettyPrint = true)
    }

    override fun getAABB() = AxisAlignedBoundingBox(x - 0.5, y - 0.5, x + 0.5, y + 0.5, this)
}