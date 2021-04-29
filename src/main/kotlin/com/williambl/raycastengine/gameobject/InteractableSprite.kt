package com.williambl.raycastengine.gameobject

import com.beust.klaxon.JsonObject
import com.williambl.raycastengine.collision.AxisAlignedBoundingBox
import com.williambl.raycastengine.render.RenderSystem
import com.williambl.raycastengine.render.gui.Gui
import com.williambl.raycastengine.util.network.toByteArray
import com.williambl.raycastengine.util.raytrace.RaytraceResult
import imgui.ImGui
import io.netty.buffer.Unpooled

class InteractableSprite(textureLoc: String = "", x: Double = 0.0, y: Double = 0.0) : Sprite(textureLoc, x, y), Interactable, Collidable {

    val gui = MyGui()

    class MyGui : Gui {
        var json = ""
        var bytes = ""
        var shouldBeOpen = false

        override fun renderGui(): ImGui.() -> Any? = {
            if (shouldBeOpen) {
                begin("Hi there!", ::shouldBeOpen)
                text("As JSON, I am:")
                textWrapped(json)
                text("And as bytes, I am:")
                textWrapped(bytes)
                end()
            }
        }
    }

    init {
        RenderSystem.addRenderTickable(gui)
    }

    override fun interact(player: Player, raytraceResult: RaytraceResult<*>) {
        gui.shouldBeOpen = true
        val json = JsonObject()
        toJson(json)
        gui.json = json.toJsonString(prettyPrint = true)
        val buf = Unpooled.buffer()
        toBytes(buf)
        gui.bytes = buf.toByteArray().map { it.toString(2) }.reduce { acc, s -> acc+s }
    }

    override fun getAABB() = AxisAlignedBoundingBox(x - 0.5, y - 0.5, x + 0.5, y + 0.5, this)
}