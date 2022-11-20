package net.aspw.nightx.features.module.modules.render

import net.aspw.nightx.event.EventTarget
import net.aspw.nightx.event.Render3DEvent
import net.aspw.nightx.features.module.Module
import net.aspw.nightx.features.module.ModuleCategory
import net.aspw.nightx.features.module.ModuleInfo
import net.aspw.nightx.utils.RenderWings
import net.aspw.nightx.value.BoolValue

@ModuleInfo(name = "Wings", category = ModuleCategory.RENDER, array = false)
class Wings : Module() {
    private val onlyThirdPerson = BoolValue("OnlyThirdPerson", true)

    @EventTarget
    fun onRenderPlayer(event: Render3DEvent) {
        if (onlyThirdPerson.get() && mc.gameSettings.thirdPersonView == 0) return
        val renderWings = RenderWings()
        renderWings.renderWings(event.partialTicks)
    }
}