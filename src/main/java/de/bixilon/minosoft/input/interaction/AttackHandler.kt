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

package de.bixilon.minosoft.input.interaction

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.rate.RateLimiter
import de.bixilon.minosoft.camera.target.targets.EntityTarget
import de.bixilon.minosoft.data.Tickable
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.container.equipment.EquipmentSlots
import de.bixilon.minosoft.data.entities.entities.LivingEntity
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.registries.effects.vision.VisionEffect
import de.bixilon.minosoft.data.registries.enchantment.tool.weapon.WeaponEnchantment
import de.bixilon.minosoft.data.registries.fluid.fluids.WaterFluid
import de.bixilon.minosoft.gui.rendering.particle.types.norender.emitter.EntityEmitterParticle
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.damage.CritParticle
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.damage.EnchantedHitParticle
import de.bixilon.minosoft.physics.parts.climbing.ClimbingPhysics.isClimbing
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.interact.EntityAttackC2SP

class AttackHandler(
    val interactions: InteractionManager,
) : Tickable {
    private val player = interactions.connection.player
    private val rateLimiter = RateLimiter()
    private var cooldown = 0

    fun tryAttack(target: EntityTarget? = interactions.camera.target.target?.nullCast()) {
        if (target == null) return
        rateLimiter.perform { attack(target) }
    }

    private fun setCooldown() {
        if (player.gamemode == Gamemodes.CREATIVE) {
            return
        }
        cooldown = COOLDOWN
    }

    private fun forceAttack(target: EntityTarget) {
        if (cooldown > 0) {
            return
        }

        if (target.distance >= ATTACK_DISTANCE) {
            setCooldown()
            return
        }

        val entity = target.entity
        if (!entity.onAttack(player)) {
            return
        }

        interactions.connection.sendPacket(EntityAttackC2SP(target.entity.id ?: return, player.isSneaking))
        if (player.gamemode == Gamemodes.SPECTATOR) {
            return
        }

        val sharpnessLevel = player.equipment[EquipmentSlots.MAIN_HAND]?._enchanting?.enchantments?.get(WeaponEnchantment.Sharpness) ?: 0

        val critical = (cooldown / COOLDOWN.toFloat()) > 0.9f && player.physics.fallDistance != 0.0f && !player.physics.onGround && !player.physics().isClimbing() && (player.physics.submersion[WaterFluid]) <= 0.0f && player.effects[VisionEffect.Blindness] == null && player.attachment.vehicle == null && entity is LivingEntity
        // TODO: use attack speed entity attribute


        if (critical) {
            interactions.connection.world.addParticle(EntityEmitterParticle(interactions.connection, entity, CritParticle))
        }

        if (sharpnessLevel > 0) {
            // ToDo: Entity animations
            interactions.connection.world.addParticle(EntityEmitterParticle(interactions.connection, entity, EnchantedHitParticle))
        }
    }


    private fun attack(target: EntityTarget) {
        if (player.activelyRiding || interactions.breaking.digging.status != null || interactions.use.long.isUsing) {
            return
        }
        forceAttack(target)
        interactions.swingHand(Hands.MAIN)
    }

    override fun tick() {
        cooldown--
    }

    companion object {
        const val ATTACK_DISTANCE = 3.0
        const val COOLDOWN = 10
    }
}
