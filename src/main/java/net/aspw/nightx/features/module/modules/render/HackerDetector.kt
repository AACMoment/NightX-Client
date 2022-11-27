package net.aspw.nightx.features.module.modules.render

import net.aspw.nightx.NightX
import net.aspw.nightx.event.EventState
import net.aspw.nightx.event.EventTarget
import net.aspw.nightx.event.MotionEvent
import net.aspw.nightx.features.module.Module
import net.aspw.nightx.features.module.ModuleCategory
import net.aspw.nightx.features.module.ModuleInfo
import net.aspw.nightx.features.module.modules.misc.AntiBot
import net.aspw.nightx.ui.client.hud.element.elements.Notification
import net.minecraft.block.BlockAir
import net.minecraft.block.BlockHopper
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper
import net.minecraft.world.World

@ModuleInfo(name = "HackerDetector", spacedName = "Hacker Detector", category = ModuleCategory.RENDER)
class HackerDetector : Module() {
    private val hackers = ArrayList<EntityPlayer>()
    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (event.eventState !== EventState.PRE) return
        if (mc.thePlayer.ticksExisted <= 105) {
            hackers.clear()
            return
        }
        for (player in mc.theWorld.playerEntities) {
            if (player !== mc.thePlayer && player.ticksExisted >= 105 && !hackers.contains(player) && !AntiBot.isBot(
                    player
                )
                && !player.capabilities.isFlying
            ) {
                if (player.capabilities.isCreativeMode) {
                    continue
                }
                val playerSpeed = getBPS(player).toDouble()
                if ((player.isUsingItem || player.isBlocking) && player.onGround && playerSpeed >= 6.5) {
                    NightX.hud.addNotification(
                        Notification(
                            "${player.name} is using noslow",
                            Notification.Type.INFO
                        )
                    )
                    hackers.add(player)
                }
                if (player.isSprinting
                    && (player.moveForward < 0.0f || player.moveForward == 0.0f && player.moveStrafing != 0.0f)
                ) {
                    NightX.hud.addNotification(
                        Notification(
                            "${player.name} is using speed",
                            Notification.Type.INFO
                        )
                    )
                    hackers.add(player)
                }
                if (!mc.theWorld
                        .getCollidingBoundingBoxes(
                            player,
                            mc.thePlayer.entityBoundingBox.offset(0.0, player.motionY, 0.0)
                        )
                        .isEmpty() && player.motionY > 0.0 && playerSpeed > 10.0
                ) {
                    NightX.hud.addNotification(
                        Notification(
                            "${player.name} is using flight",
                            Notification.Type.INFO
                        )
                    )
                    hackers.add(player)
                }
                val y = Math.abs(player.posY.toInt()).toDouble()
                val lastY = Math.abs(player.lastTickPosY.toInt()).toDouble()
                val yDiff = if (y > lastY) y - lastY else lastY - y
                if (yDiff > 0.0 && mc.thePlayer.onGround && player.motionY == -0.0784000015258789) {
                    NightX.hud.addNotification(
                        Notification(
                            "${player.name} is using step",
                            Notification.Type.INFO
                        )
                    )
                    hackers.add(player)
                }
                if (player.hurtTime in 5..8 && mc.thePlayer.onGround && player.motionY == -0.0784000015258789 && player.motionX == 0.0 && player.motionZ == 0.0) {
                    NightX.hud.addNotification(
                        Notification(
                            "${player.name} is using velocity",
                            Notification.Type.INFO
                        )
                    )
                    hackers.add(player)
                }
                if (player.fallDistance != 0.0f || player.motionY >= -0.08 || InsideBlock(player) || player.onGround || !mc.thePlayer.isInWater) {
                    continue
                }
                NightX.hud.addNotification(
                    Notification(
                        "${player.name} is using nofall",
                        Notification.Type.INFO
                    )
                )
                hackers.add(player)
            }
        }
    }

    fun getBPS(entityIn: EntityLivingBase): Int {
        val bps = getLastDist(entityIn) * 10.0
        return bps.toInt()
    }

    fun getLastDist(entIn: EntityLivingBase): Double {
        val xDist = entIn.posX - entIn.prevPosX
        val zDist = entIn.posZ - entIn.prevPosZ
        return Math.sqrt(xDist * xDist + zDist * zDist)
    }

    fun InsideBlock(player: EntityPlayer): Boolean {
        for (x in MathHelper.floor_double(player.entityBoundingBox.minX) until MathHelper
            .floor_double(player.entityBoundingBox.maxX) + 1) {
            for (y in MathHelper.floor_double(player.entityBoundingBox.minY) until MathHelper
                .floor_double(player.entityBoundingBox.maxY) + 1) {
                for (z in MathHelper.floor_double(player.entityBoundingBox.minZ) until MathHelper
                    .floor_double(player.entityBoundingBox.maxZ) + 1) {
                    val block = mc.theWorld.getBlockState(BlockPos(x, y, z)).block
                    if (block != null && block !is BlockAir) {
                        var boundingBox = block.getCollisionBoundingBox(
                            mc.theWorld as World,
                            BlockPos(x, y, z), mc.theWorld.getBlockState(BlockPos(x, y, z))
                        )
                        if (block is BlockHopper) {
                            boundingBox = AxisAlignedBB(
                                x.toDouble(),
                                y.toDouble(),
                                z.toDouble(),
                                (x + 1).toDouble(),
                                (y + 1).toDouble(),
                                (z + 1).toDouble()
                            )
                        }
                        if (boundingBox != null && player.entityBoundingBox.intersectsWith(boundingBox)) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }
    init {
        state = true
    }
}