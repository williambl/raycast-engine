package com.williambl.raycastengine.render.gui

import com.williambl.raycastengine.events.RenderTickable
import imgui.ImGui

interface Gui: RenderTickable {
    override fun renderTick() {
        ImGui.run(renderGui())
    }

    fun renderGui(): ImGui.() -> Any?
}
