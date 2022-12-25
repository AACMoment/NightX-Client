package net.aspw.nightx.features.module.modules.render

import net.aspw.nightx.NightX
import net.aspw.nightx.event.EventState
import net.aspw.nightx.event.EventTarget
import net.aspw.nightx.event.MotionEvent
import net.aspw.nightx.features.module.Module
import net.aspw.nightx.features.module.ModuleCategory
import net.aspw.nightx.features.module.ModuleInfo
import net.aspw.nightx.features.module.modules.combat.KillAura
import net.aspw.nightx.value.BoolValue
import net.aspw.nightx.value.FloatValue
import net.aspw.nightx.value.IntegerValue
import net.aspw.nightx.value.ListValue

@ModuleInfo(name = "Animations", category = ModuleCategory.RENDER, array = false)
class Animations : Module() {
    override fun onInitialize() {
        state = true
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        val killAura = NightX.moduleManager.getModule(KillAura::class.java)
        if (event.eventState === EventState.POST && mc.thePlayer.isSwingInProgress && smoothAnimValue.get() && !mc.thePlayer.isBlocking && killAura!!.target == null) {
            mc.thePlayer.renderArmPitch = 100f + mc.thePlayer.rotationPitch
        }

        if (event.eventState === EventState.POST && mc.thePlayer.isSwingInProgress && blockingEquipValue.get() && mc.thePlayer.isBlocking || event.eventState === EventState.POST && blockingEquipValue.get() && mc.thePlayer.isSwingInProgress && killAura?.target != null) {
            mc.thePlayer.renderArmPitch = -120 + mc.thePlayer.rotationPitch
        }
    }

    companion object {
        // some ListValue
        @JvmField
        val Sword = ListValue(
            "Style", arrayOf(
                "1.8",
                "LiquidBounce",
                "SlideLow",
                "SlideMedium",
                "SlideFull",
                "SlidePut",
                "Push",
                "Dash",
                "Swing",
                "Swank",
                "Swong",
                "Swang",
                "Swaing",
                "Stella",
                "Smart",
                "Sloth",
                "Autumn",
                "Astolfo",
                "ETB",
                "Moon",
                "MoonPush",
                "Smooth",
                "Leaked",
                "Ninja",
                "Jigsaw",
                "Avatar",
                "Sigma3",
                "Sigma4",
                "Reverse",
                "Old",
                "OldFull",
                "Flux1",
                "Flux2",
                "Flux3",
                "DortwareNew",
                "Dortware1",
                "Dortware2",
                "Funny",
                "Zoom",
                "Rotate",
                "Spin",
                "Spinny"
            ), "Swing"
        )

        // item general scale
        @JvmField
        val Scale = FloatValue("Scale", 0.4f, 0f, 4f)

        // normal item position
        @JvmField
        val itemPosX = FloatValue("ItemPosX", 0f, -1f, 1f)

        @JvmField
        val itemPosY = FloatValue("ItemPosY", 0f, -1f, 1f)

        @JvmField
        val itemPosZ = FloatValue("ItemPosZ", 0f, -1f, 1f)

        @JvmField
        val itemFovX = FloatValue("ItemFovX", 1f, -10f, 10f)

        @JvmField
        val itemFovY = FloatValue("ItemFovY", 1f, -10f, 10f)

        @JvmField
        val itemFovZ = FloatValue("ItemFovZ", 1f, -10f, 10f)

        // change Position Blocking Sword
        @JvmField
        val blockPosX = FloatValue("BlockPosX", 0f, -1f, 1f)

        @JvmField
        val blockPosY = FloatValue("BlockPosY", 0f, -1f, 1f)

        @JvmField
        val blockPosZ = FloatValue("BlockPosZ", 0f, -1f, 1f)

        // modify item swing and rotate
        @JvmField
        val SpeedSwing = IntegerValue("Swing-Speed", 0, -9, 5)

        @JvmField
        val Equip = IntegerValue("Equip-Motion", 2, -5, 5) {
            Sword.get().equals("push", ignoreCase = true) || Sword.get()
                .equals("swank", ignoreCase = true) || Sword.get().equals("swong", ignoreCase = true) || Sword.get()
                .equals("swang", ignoreCase = true) ||
                    Sword.get().equals("swaing", ignoreCase = true) || Sword.get()
                .equals("dash", ignoreCase = true) || Sword.get().equals("smart", ignoreCase = true) || Sword.get()
                .equals("moon", ignoreCase = true) || Sword.get().equals("dortware1", ignoreCase = true) || Sword.get()
                .equals("dortware2", ignoreCase = true)
        }

        @JvmField
        val RotateItems = BoolValue("Rotate-Items", false)

        @JvmField
        val SpeedRotate = FloatValue("Rotate-Speed", 1f, 0f, 10f) {
            RotateItems.get() || Sword.get().equals("spinny", ignoreCase = true) || Sword.get()
                .equals("rotate", ignoreCase = true)
        }

        @JvmField
        val SpinSpeed = FloatValue("Spin-Speed", 5f, 0f, 50f) { Sword.get().equals("spin", ignoreCase = true) }

        // transform rotation
        @JvmField
        val transformFirstPersonRotate =
            ListValue("RotateMode", arrayOf("RotateY", "RotateXY", "Custom", "None"), "RotateY")

        // custom item rotate
        @JvmField
        val customRotate1 = FloatValue("RotateXAxis", 0f, -180f, 180f) {
            RotateItems.get() && transformFirstPersonRotate.get().equals("custom", ignoreCase = true)
        }

        @JvmField
        val customRotate2 = FloatValue("RotateYAxis", 0f, -180f, 180f) {
            RotateItems.get() && transformFirstPersonRotate.get().equals("custom", ignoreCase = true)
        }

        @JvmField
        val customRotate3 = FloatValue("RotateZAxis", 0f, -180f, 180f) {
            RotateItems.get() && transformFirstPersonRotate.get().equals("custom", ignoreCase = true)
        }

        // fake blocking
        @JvmField
        val fakeBlock = BoolValue("Always-Block", true)

        // gui animations
        @JvmField
        val guiAnimations = ListValue("Container-Animation", arrayOf("None", "Zoom", "Slide", "Smooth"), "None")

        @JvmField
        val vSlideValue = ListValue("Slide-Vertical", arrayOf("None", "Upward", "Downward"), "None") {
            guiAnimations.get().equals("slide", ignoreCase = true)
        }

        @JvmField
        val hSlideValue = ListValue("Slide-Horizontal", arrayOf("None", "Right", "Left"), "Left") {
            guiAnimations.get().equals("slide", ignoreCase = true)
        }

        @JvmField
        val animTimeValue =
            IntegerValue("Container-AnimTime", 200, 0, 3000) { !guiAnimations.get().equals("none", ignoreCase = true) }

        @JvmField
        val tabAnimations = ListValue("Tab-Animation", arrayOf("None", "Zoom", "Slide"), "None")

        // block break
        @JvmField
        val noBlockParticles = BoolValue("NoBlockParticles", false)

        @JvmField
        val blockingEquipValue = BoolValue("BlockingEquip", true)

        @JvmField
        val swingAnimValue = BoolValue("FluxSwingAnimation", false)
        val smoothAnimValue = BoolValue("SmoothSwingAnimation", false)
    }
}