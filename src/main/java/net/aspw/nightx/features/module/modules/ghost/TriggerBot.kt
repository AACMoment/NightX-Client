package net.aspw.nightx.features.module.modules.ghost

import net.aspw.nightx.event.EventTarget
import net.aspw.nightx.event.Render3DEvent
import net.aspw.nightx.features.module.Module
import net.aspw.nightx.features.module.ModuleCategory
import net.aspw.nightx.features.module.ModuleInfo
import net.aspw.nightx.utils.EntityUtils
import net.aspw.nightx.utils.timer.TimeUtils
import net.aspw.nightx.value.IntegerValue

@ModuleInfo(name = "TriggerBot", spacedName = "Trigger Bot", category = ModuleCategory.GHOST)
class TriggerBot : Module() {

    private val maxCPS: IntegerValue = object : IntegerValue("MaxCPS", 12, 1, 20) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = minCPS.get()
            if (i > newValue) set(i)
            delay = TimeUtils.randomClickDelay(minCPS.get(), this.get())
        }
    }

    private val minCPS: IntegerValue = object : IntegerValue("MinCPS", 10, 1, 20) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = maxCPS.get()
            if (i < newValue) set(i)
            delay = TimeUtils.randomClickDelay(this.get(), maxCPS.get())
        }
    }

    private var delay = TimeUtils.randomClickDelay(minCPS.get(), maxCPS.get())
    private var lastSwing = 0L

    @EventTarget
    fun onRender(event: Render3DEvent) {
        val objectMouseOver = mc.objectMouseOver

        if (objectMouseOver != null && System.currentTimeMillis() - lastSwing >= delay &&
            EntityUtils.isSelected(objectMouseOver.entityHit, true)
        ) {
            net.minecraft.client.settings.KeyBinding.onTick(mc.gameSettings.keyBindAttack.keyCode)

            lastSwing = System.currentTimeMillis()
            delay = TimeUtils.randomClickDelay(minCPS.get(), maxCPS.get())
        }
    }
}