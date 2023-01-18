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

package de.bixilon.minosoft.gui.rendering.input.interaction

import de.bixilon.kotlinglm.pow
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.collections.CollectionUtil.synchronizedMapOf
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.properties.UnbreakableBlock
import de.bixilon.minosoft.data.registries.effects.mining.MiningEffect
import de.bixilon.minosoft.data.registries.enchantment.armor.ArmorEnchantment
import de.bixilon.minosoft.data.registries.enchantment.tool.MiningEnchantment
import de.bixilon.minosoft.data.registries.fluid.DefaultFluids
import de.bixilon.minosoft.data.registries.item.items.tool.MiningTool
import de.bixilon.minosoft.data.registries.item.items.tool.ToolRequirement
import de.bixilon.minosoft.data.registries.misc.event.world.handler.BlockDestroyedHandler
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.camera.target.targets.BlockTarget
import de.bixilon.minosoft.modding.event.events.LegacyBlockBreakAckEvent
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.packets.c2s.play.PlayerActionC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.move.SwingArmC2SP
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class BreakInteractionHandler(
    val context: RenderContext,
) {
    private val connection = context.connection

    private var breakPosition: Vec3i? = null
    private var breakBlockState: BlockState? = null
    var breakProgress = Double.NEGATIVE_INFINITY
        private set
    var status: BlockBreakStatus? = null
        private set

    private var breakSelectedSlot: Int = -1

    private var breakSent = 0L
    private var lastSwing = 0L
    private var creativeLastHoldBreakTime = 0L

    private val legacyAcknowledgedBreakStarts: MutableMap<Vec3i, BlockState?> = synchronizedMapOf()

    private fun clearDigging() {
        breakPosition = null
        breakBlockState = null
        breakProgress = Double.NEGATIVE_INFINITY
        status = null

        breakSelectedSlot = -1
    }

    private fun cancelDigging() {
        val breakPosition = breakPosition ?: return
        connection.sendPacket(PlayerActionC2SP(PlayerActionC2SP.Actions.CANCELLED_DIGGING, breakPosition))
        clearDigging()
    }

    private fun swingArm() {
        val currentTime = millis()
        if (currentTime - lastSwing <= ProtocolDefinition.TICK_TIME) {
            return
        }
        lastSwing = currentTime
        connection.sendPacket(SwingArmC2SP(Hands.MAIN))
    }

    private fun checkBreaking(isKeyDown: Boolean, deltaTime: Double): Boolean {
        val currentTime = millis()

        if (!isKeyDown) {
            creativeLastHoldBreakTime = 0L
            cancelDigging()
            return false
        }

        if (!connection.player.gamemode.canBreak) {
            cancelDigging()
            return false
        }
        val target = context.camera.targetHandler.target

        if (target !is BlockTarget) {
            cancelDigging()
            return false
        }

        if (target.distance >= connection.player.reachDistance) {
            cancelDigging()
            return false
        }

        if (!connection.world.isPositionChangeable(target.blockPosition)) {
            cancelDigging()
            return false
        }

        // check if we look at another block or our inventory changed
        if (breakPosition != target.blockPosition || breakBlockState != target.blockState || breakSelectedSlot != connection.player.selectedHotbarSlot) {
            cancelDigging()
        }


        fun startDigging() {
            if (breakPosition != null) {
                return
            }
            connection.sendPacket(PlayerActionC2SP(PlayerActionC2SP.Actions.START_DIGGING, target.blockPosition, target.direction))

            breakPosition = target.blockPosition
            breakBlockState = target.blockState
            breakProgress = 0.0

            breakSelectedSlot = connection.player.selectedHotbarSlot
        }

        fun finishDigging() {
            connection.sendPacket(PlayerActionC2SP(PlayerActionC2SP.Actions.FINISHED_DIGGING, target.blockPosition, target.direction))
            clearDigging()
            DefaultThreadPool += { connection.world[target.blockPosition] = null }

            BlockDestroyedHandler.handleDestroy(connection, target.blockPosition, target.blockState)
        }

        val canStartBreaking = currentTime - breakSent >= ProtocolDefinition.TICK_TIME



        if (connection.player.gamemode == Gamemodes.CREATIVE) {
            if (!canStartBreaking) {
                return true
            }
            // creative
            if (currentTime - creativeLastHoldBreakTime <= ProtocolDefinition.TICK_TIME * 5) {
                return true
            }

            swingArm()
            startDigging()
            finishDigging()
            creativeLastHoldBreakTime = currentTime
            breakSent = currentTime
            return true
        }

        if (breakPosition == null && !canStartBreaking) {
            return true
        }

        breakSent = currentTime

        startDigging()

        swingArm()

        // thanks to https://minecraft.fandom.com/wiki/Breaking#Calculation

        val stack = connection.player.inventory.getHotbarSlot()

        var speed = 1.0f
        var toolSpeed: Float? = null
        val block = target.blockState.block
        val toolRequired = block is ToolRequirement
        var isBestTool = !toolRequired

        if (stack != null) {
            if (stack.item.item is MiningTool) {
                toolSpeed = stack.item.item.getMiningSpeed(connection, target.blockState, stack)
                isBestTool = true
            }
        }
        toolSpeed?.let { speed *= it }


        if (toolSpeed != null) {
            stack?._enchanting?.enchantments?.get(MiningEnchantment.Efficiency)?.let {
                speed += it.pow(2) + 1.0f
            }
        }

        connection.player.effects[MiningEffect.Haste]?.let {
            speed *= (0.2f * (it.amplifier + 1)) + 1.0f
        }

        connection.player.effects[MiningEffect.MiningFatigue]?.let {
            speed *= when (it.amplifier) {
                0 -> 0.3f
                1 -> 0.09f
                2 -> 0.0027f
                else -> 8.1E-4f
            }
        }

        if (connection.player.submergedFluid?.identifier == DefaultFluids.WATER && connection.player.getEquipmentEnchant(ArmorEnchantment.AquaAffinity) == 0) {
            speed /= 5.0f
        }

        if (!connection.player.onGround) {
            speed /= 5.0f
        }

        var damage = speed / target.blockState.block.hardness

        damage /= if (isBestTool) {
            30
        } else {
            100
        }

        when {
            damage <= 0.0f || block is UnbreakableBlock -> {
                breakProgress = 0.0
            }

            damage > 1.0f -> {
                breakProgress = 1.0
            }


            else -> {
                val ticks = 1.0f / damage
                val seconds = (ticks / ProtocolDefinition.TICKS_PER_SECOND)
                val progress = ((1.0f / seconds) * deltaTime)
                // Log.log(LogMessageType.OTHER, LogLevels.WARN){ "Breaking progress at $breakPosition, total=$breakProgress, totalEstimated=$seconds"}
                breakProgress += progress
            }
        }

        // TODO: properly set slow status if block drops without tool
        this.status = when {
            damage <= 0.0f || block is UnbreakableBlock -> BlockBreakStatus.USELESS
            toolRequired && toolSpeed == null -> BlockBreakStatus.INEFFECTIVE
            toolSpeed == null -> BlockBreakStatus.SLOW
            else -> BlockBreakStatus.EFFECTIVE
        }

        if (breakProgress >= 1.0f) {
            finishDigging()
        }
        return true
    }

    fun init() {
        context.inputHandler.registerCheckCallback(
            DESTROY_BLOCK_KEYBINDING to KeyBinding(
                KeyActions.CHANGE to setOf(KeyCodes.MOUSE_BUTTON_LEFT),
            )
        )

        connection.events.listen<LegacyBlockBreakAckEvent> {
            when (it.actions) {
                PlayerActionC2SP.Actions.START_DIGGING -> {
                    if (it.successful) {
                        legacyAcknowledgedBreakStarts[it.blockPosition] = it.blockState
                    } else {
                        if (it.blockPosition != breakPosition || it.blockState != breakBlockState) {
                            return@listen
                        }
                        breakProgress = Double.NEGATIVE_INFINITY
                    }
                }

                PlayerActionC2SP.Actions.FINISHED_DIGGING -> {
                    if (legacyAcknowledgedBreakStarts[it.blockPosition] == null) {
                        // start was not acknowledged, undoing
                        connection.world[it.blockPosition] = it.blockState
                    }
                    legacyAcknowledgedBreakStarts.remove(it.blockPosition)
                }

                else -> Unit
            }
        }

        // ToDo: Handle BlockBreakAck (not just legacy)
    }

    fun draw(deltaTime: Double) {
        val isKeyDown = context.inputHandler.isKeyBindingDown(DESTROY_BLOCK_KEYBINDING)
        // ToDo: Entity attacking
        val consumed = checkBreaking(isKeyDown, deltaTime)

        if (!isKeyDown) {
            return
        }
        if (consumed) {
            return
        }
        swingArm() // ToDo: Only once
    }

    companion object {
        private val DESTROY_BLOCK_KEYBINDING = "minosoft:destroy_block".toResourceLocation()
    }
}
