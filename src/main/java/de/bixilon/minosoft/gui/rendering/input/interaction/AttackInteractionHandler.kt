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

import de.bixilon.kutil.rate.RateLimiter
import de.bixilon.kutil.time.Cooldown
import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.container.InventorySlots
import de.bixilon.minosoft.data.entities.entities.LivingEntity
import de.bixilon.minosoft.data.registries.effects.DefaultStatusEffects
import de.bixilon.minosoft.data.registries.enchantment.DefaultEnchantments
import de.bixilon.minosoft.data.registries.fluid.DefaultFluids
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.camera.target.targets.EntityTarget
import de.bixilon.minosoft.gui.rendering.particle.types.norender.emitter.EntityEmitterParticle
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.damage.CritParticle
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.damage.EnchantedHitParticle
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.interact.EntityAttackC2SP
import de.bixilon.minosoft.util.KUtil.setTicks
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class AttackInteractionHandler(
    val renderWindow: RenderWindow,
    val interactionManager: InteractionManager,
) {
    private val sharpness = renderWindow.connection.registries.enchantmentRegistry[DefaultEnchantments.SHARPNESS]
    private val blindness = renderWindow.connection.registries.statusEffectRegistry[DefaultStatusEffects.BLINDNESS]
    private val player = renderWindow.connection.player
    private val rateLimiter = RateLimiter()
    val cooldown = Cooldown()

    fun init() {
        renderWindow.inputHandler.registerKeyCallback(ATTACK_ENTITY_KEYBINDING, KeyBinding(
                KeyActions.PRESS to setOf(KeyCodes.MOUSE_BUTTON_LEFT),
        ), false) { tryAttack() }
    }

    fun draw(delta: Double) {

    }

    fun tryAttack() {
        rateLimiter.perform { attack() }
    }

    private fun setCooldown() {
        if (player.gamemode == Gamemodes.CREATIVE) {
            return
        }
        cooldown.setTicks(10)
    }

    fun attack() {
        if (player.activelyRiding) {
            return
        }
        val target = renderWindow.camera.targetHandler.target
        if (target !is EntityTarget || target.distance >= player.reachDistance) {
            return // ToDo: set cooldown
        }

        renderWindow.connection.sendPacket(EntityAttackC2SP(target.entity.id ?: return, player.isSneaking))
        if (player.gamemode == Gamemodes.SPECTATOR) {
            return
        }

        // ToDo set cooldown

        val entity = target.entity
        if (!entity.onAttack(player)) {
            return
        }
        val sharpnessLevel = player.equipment[InventorySlots.EquipmentSlots.MAIN_HAND]?._enchanting?.enchantments?.get(sharpness) ?: 0

        val critical = cooldown.progress > 0.9f && player.fallDistance != 0.0 && !player.onGround && !player.isClimbing && (player.fluidHeights[DefaultFluids.WATER] ?: 0.0f) <= 0.0f && player.activeStatusEffects[blindness] == null && player.vehicle == null && entity is LivingEntity

        if (critical) {
            renderWindow.connection.world.addParticle(EntityEmitterParticle(renderWindow.connection, entity, CritParticle))
        }

        if (sharpnessLevel > 0) {
            // ToDo: Entity animations
            renderWindow.connection.world.addParticle(EntityEmitterParticle(renderWindow.connection, entity, EnchantedHitParticle))
        }

    }

    companion object {
        private val ATTACK_ENTITY_KEYBINDING = "minosoft:attack_entity".toResourceLocation()
    }
}
