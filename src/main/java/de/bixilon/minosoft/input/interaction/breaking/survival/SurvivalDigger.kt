/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.input.interaction.breaking.survival

import de.bixilon.kotlinglm.pow
import de.bixilon.minosoft.camera.target.targets.BlockTarget
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.PixLyzerBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.hardness.UnbreakableBlock
import de.bixilon.minosoft.data.registries.effects.mining.MiningEffect
import de.bixilon.minosoft.data.registries.enchantment.armor.ArmorEnchantment
import de.bixilon.minosoft.data.registries.enchantment.tool.MiningEnchantment
import de.bixilon.minosoft.data.registries.fluid.fluids.WaterFluid
import de.bixilon.minosoft.data.registries.item.items.tool.MiningTool
import de.bixilon.minosoft.data.registries.item.items.tool.properties.requirement.ToolRequirement
import de.bixilon.minosoft.input.interaction.breaking.BreakHandler
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.player.PlayerActionC2SP

class SurvivalDigger(
    private val breaking: BreakHandler,
) {
    private val connection = breaking.interactions.connection

    var status: BlockDigStatus? = null
        private set


    fun tryCancel() {
        status?.let { cancel(it) }
    }

    private fun cancel(status: BlockDigStatus) {
        breaking.executor.cancel()
        connection.sendPacket(PlayerActionC2SP(PlayerActionC2SP.Actions.CANCELLED_DIGGING, status.position, sequence = 0))
        this.status = null
    }

    private fun finish(status: BlockDigStatus, instant: Boolean) {
        val sequence = breaking.executor.finish()
        this.status = null
        if (!instant) {
            connection.sendPacket(PlayerActionC2SP(PlayerActionC2SP.Actions.FINISHED_DIGGING, status.position, status.direction, sequence))
            breaking.addCooldown()
        }
        breaking.interactions.swingHand(Hands.MAIN)
    }

    // thanks to https://minecraft.fandom.com/wiki/Breaking#Calculation
    private fun tick(status: BlockDigStatus?, target: BlockTarget, slot: Int) {
        val stack = connection.player.items.inventory.getHotbarSlot()

        var speed = 1.0f
        var toolSpeed = 1.0f
        val block = target.state.block
        val toolRequired = if (block is PixLyzerBlock) block.requiresTool else block is ToolRequirement
        var isBestTool = !toolRequired

        if (stack != null && stack.item.item is MiningTool) {
            isBestTool = isBestTool || stack.item.item.isSuitableFor(connection, target.state, stack)
            toolSpeed = stack.item.item.getMiningSpeed(connection, target.state, stack)
            speed *= toolSpeed
        }


        if (toolSpeed > 1.0f) {
            stack?._enchanting?.enchantments?.get(MiningEnchantment.Efficiency)?.let {
                speed += it.pow(2) + 1.0f
            }
        }

        connection.player.effects[MiningEffect.Haste]?.let {
            speed *= (0.2f * (it.amplifier + 1)) + 1.0f
        }

        connection.player.effects[MiningEffect.MiningFatigue]?.let { speed *= MiningEffect.MiningFatigue.calculateSpeed(it.amplifier) }

        if (connection.player.physics.submersion.eye is WaterFluid && connection.player.equipment[ArmorEnchantment.AquaAffinity] == 0) {
            speed /= 5.0f
        }

        if (!connection.player.physics.onGround) {
            speed /= 5.0f
        }

        var damage = speed / target.state.block.hardness

        damage /= if (isBestTool) 30 else 100
        val instant = damage >= 1.0f

        val progress = when {
            damage <= 0.0f || block is UnbreakableBlock -> 0.0f
            damage > 1.0f -> 1.0f
            else -> damage
        }

        val productivity = when {
            damage <= 0.0f || block is UnbreakableBlock -> BlockBreakProductivity.USELESS
            !isBestTool || toolSpeed == 1.0f -> BlockBreakProductivity.INEFFECTIVE
            else -> BlockBreakProductivity.EFFECTIVE
        }


        val nextStatus: BlockDigStatus
        if (status != null) {
            nextStatus = status
            status.progress += progress
        } else {
            nextStatus = BlockDigStatus(target.blockPosition, target.state, slot, productivity, target.direction)
            val sequence = breaking.executor.start(target.blockPosition, target.state)
            connection.sendPacket(PlayerActionC2SP(PlayerActionC2SP.Actions.START_DIGGING, target.blockPosition, target.direction, sequence))
        }

        if (instant || nextStatus.progress >= 1.0f) {
            return finish(nextStatus, instant)
        }
        breaking.interactions.swingHand(Hands.MAIN)
        this.status = nextStatus
    }

    fun dig(target: BlockTarget?) {
        var status = this.status
        if (target == null) {
            status?.let { cancel(it) }
            return
        }
        val slot = connection.player.items.hotbar

        if (status != null && (target.blockPosition != status.position || target.state != status.state || slot != status.slot)) {
            cancel(status)
            status = null
        }
        tick(status, target, slot)
    }
}
