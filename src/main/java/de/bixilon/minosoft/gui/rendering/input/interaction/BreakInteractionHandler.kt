/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.player.Hands
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.effects.DefaultStatusEffects
import de.bixilon.minosoft.data.registries.enchantment.DefaultEnchantments
import de.bixilon.minosoft.data.registries.fluid.DefaultFluids
import de.bixilon.minosoft.data.registries.items.tools.MiningToolItem
import de.bixilon.minosoft.data.registries.other.world.event.handlers.BlockDestroyedHandler
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.camera.target.targets.BlockTarget
import de.bixilon.minosoft.modding.event.events.LegacyBlockBreakAckEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.protocol.packets.c2s.play.PlayerActionC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.move.SwingArmC2SP
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class BreakInteractionHandler(
    val renderWindow: RenderWindow,
) {
    private val connection = renderWindow.connection

    private var breakPosition: Vec3i? = null
    private var breakBlockState: BlockState? = null
    var breakProgress = Double.NEGATIVE_INFINITY
        private set
    val breakingBlock: Boolean
        get() = breakPosition != null

    private var breakSelectedSlot: Int = -1

    private var breakSent = 0L
    private var lastSwing = 0L
    private var creativeLastHoldBreakTime = 0L

    private val legacyAcknowledgedBreakStarts: MutableMap<Vec3i, BlockState?> = synchronizedMapOf()

    private val efficiencyEnchantment = connection.registries.enchantmentRegistry[DefaultEnchantments.EFFICIENCY]
    private val aquaAffinityEnchantment = connection.registries.enchantmentRegistry[DefaultEnchantments.AQUA_AFFINITY]

    private val hasteStatusEffect = connection.registries.statusEffectRegistry[DefaultStatusEffects.HASTE]
    private val miningFatigueStatusEffect = connection.registries.statusEffectRegistry[DefaultStatusEffects.MINING_FATIGUE]

    private fun clearDigging() {
        breakPosition = null
        breakBlockState = null
        breakProgress = Double.NEGATIVE_INFINITY

        breakSelectedSlot = -1
    }

    private fun cancelDigging() {
        val breakPosition = breakPosition ?: return
        connection.sendPacket(PlayerActionC2SP(PlayerActionC2SP.Actions.CANCELLED_DIGGING, breakPosition))
        clearDigging()
    }

    private fun swingArm() {
        val currentTime = TimeUtil.millis
        if (currentTime - lastSwing <= ProtocolDefinition.TICK_TIME) {
            return
        }
        lastSwing = currentTime
        connection.sendPacket(SwingArmC2SP(Hands.MAIN))
    }

    private fun checkBreaking(isKeyDown: Boolean, deltaTime: Double): Boolean {
        val currentTime = TimeUtil.millis

        if (!isKeyDown) {
            creativeLastHoldBreakTime = 0L
            cancelDigging()
            return false
        }

        if (!connection.player.gamemode.canBreak) {
            cancelDigging()
            return false
        }
        val target = renderWindow.camera.targetHandler.target

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
            connection.world[target.blockPosition] = null

            BlockDestroyedHandler.handleDestroy(connection, target.blockPosition, target.blockState)
        }

        val canStartBreaking = currentTime - breakSent >= ProtocolDefinition.TICK_TIME


        val canInstantBreak = connection.player.baseAbilities.creative || connection.player.gamemode == Gamemodes.CREATIVE

        if (canInstantBreak) {
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

        val breakItemInHand = connection.player.inventory.getHotbarSlot()

        val isToolEffective = breakItemInHand?.item?.item?.let {
            return@let if (it is MiningToolItem) {
                it.isEffectiveOn(connection, target.blockState)
            } else {
                false
            }
        } ?: false
        val isBestTool = !target.blockState.requiresTool || isToolEffective

        var speedMultiplier = breakItemInHand?.let { it.item.item.getMiningSpeedMultiplier(connection, target.blockState, it) } ?: 1.0f

        if (isToolEffective) {
            breakItemInHand?._enchanting?.enchantments?.get(efficiencyEnchantment)?.let {
                speedMultiplier += it.pow(2) + 1.0f
            }
        }

        connection.player.activeStatusEffects[hasteStatusEffect]?.let {
            speedMultiplier *= (0.2f * (it.amplifier + 1)) + 1.0f
        }

        connection.player.activeStatusEffects[miningFatigueStatusEffect]?.let {
            speedMultiplier *= when (it.amplifier) {
                0 -> 0.3f
                1 -> 0.09f
                2 -> 0.0027f
                else -> 8.1E-4f
            }
        }

        if (connection.player.submergedFluid?.resourceLocation == DefaultFluids.WATER && connection.player.getEquipmentEnchant(aquaAffinityEnchantment) == 0) {
            speedMultiplier /= 5.0f
        }

        if (!connection.player.onGround) {
            speedMultiplier /= 5.0f
        }

        var damage = speedMultiplier / target.blockState.hardness

        damage /= if (isBestTool) {
            30
        } else {
            100
        }

        when {
            damage > 1.0f -> {
                breakProgress = 1.0
            }
            damage <= 0.0f -> {
                breakProgress = 0.0
            }
            else -> {
                val ticks = 1.0f / damage
                val seconds = (ticks / ProtocolDefinition.TICKS_PER_SECOND)
                val progress = ((1.0f / seconds) * deltaTime)
                // Log.log(LogMessageType.OTHER, LogLevels.WARN){ "Breaking progress at $breakPosition, total=$breakProgress, totalEstimated=$seconds"}
                breakProgress += progress
            }
        }

        if (breakProgress >= 1.0f) {
            finishDigging()
        }
        return true
    }

    fun init() {
        renderWindow.inputHandler.registerCheckCallback(DESTROY_BLOCK_KEYBINDING to KeyBinding(
            mapOf(
                KeyActions.CHANGE to setOf(KeyCodes.MOUSE_BUTTON_LEFT),
            ),
        ))

        connection.registerEvent(CallbackEventInvoker.of<LegacyBlockBreakAckEvent> {
            when (it.actions) {
                PlayerActionC2SP.Actions.START_DIGGING -> {
                    if (it.successful) {
                        legacyAcknowledgedBreakStarts[it.blockPosition] = it.blockState
                    } else {
                        if (it.blockPosition != breakPosition || it.blockState != breakBlockState) {
                            return@of
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
            }
        })

        // ToDo: Handle BlockBreakAck (not just legacy)
    }

    fun draw(deltaTime: Double) {
        val isKeyDown = renderWindow.inputHandler.isKeyBindingDown(DESTROY_BLOCK_KEYBINDING)
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
