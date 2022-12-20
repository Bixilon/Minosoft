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
package de.bixilon.minosoft.data.entities.entities

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.random.RandomUtil.chance
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.Poses
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.registries.effects.attributes.DefaultStatusEffectAttributeNames
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.asRGBColor
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.spell.AmbientEntityEffectParticle
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.spell.EntityEffectParticle
import de.bixilon.minosoft.gui.rendering.util.VecUtil.horizontal
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

abstract class LivingEntity(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : Entity(connection, entityType, data, position, rotation) {
    private val entityEffectParticle = connection.registries.particleTypeRegistry[EntityEffectParticle]
    private val ambientEntityEffectParticle = connection.registries.particleTypeRegistry[AmbientEntityEffectParticle]

    private fun getLivingEntityFlag(bitMask: Int): Boolean {
        return data.getBitMask(FLAGS_DATA, bitMask, 0x00)
    }

    @get:SynchronizedEntityData
    val isHandActive: Boolean
        get() = getLivingEntityFlag(0x01)

    @get:SynchronizedEntityData
    open val activeHand: Hands?
        get() = if (getLivingEntityFlag(0x02)) Hands.OFF else Hands.MAIN

    @get:SynchronizedEntityData // aka using riptide
    val isSpinAttacking: Boolean
        get() = getLivingEntityFlag(0x04)

    @get:SynchronizedEntityData
    open val health: Double
        get() = data.get<Float?>(HEALTH_DATA, null)?.toDouble() ?: type.attributes[DefaultStatusEffectAttributeNames.GENERIC_MAX_HEALTH] ?: 1.0

    @get:SynchronizedEntityData
    val effectColor: RGBColor?
        get() = data.get<Int?>(EFFECT_COLOR_DATA, null)?.asRGBColor()

    @get:SynchronizedEntityData
    val effectAmbient: Boolean
        get() = data.getBoolean(EFFECT_AMBIENT_DATA, false)

    @get:SynchronizedEntityData
    val arrowCount: Int
        get() = data.get(ARROW_COUNT_DATA, 0)

    @get:SynchronizedEntityData
    val absorptionHearts: Int
        get() = data.get(ABSORPTION_HEARTS_DATA, 0)

    @get:SynchronizedEntityData
    val bedPosition: Vec3i?
        get() = data.get(BED_POSITION_DATA, null)

    open val isSleeping: Boolean
        get() = bedPosition != null

    override val pose: Poses?
        get() = when {
            isSleeping -> Poses.SLEEPING
            isSpinAttacking -> Poses.SPIN_ATTACK
            else -> super.pose
        }

    override val spawnSprintingParticles: Boolean
        get() = super.spawnSprintingParticles && health > 0.0

    private fun tickStatusEffects() {
        if (entityEffectParticle == null && ambientEntityEffectParticle == null) {
            return
        }
        val effectColor = effectColor ?: return
        if (effectColor == ChatColors.BLACK) {
            return
        }
        var spawnParticles = if (isInvisible) {
            random.nextInt(15) == 0
        } else {
            random.nextBoolean()
        }

        if (effectAmbient) {
            spawnParticles = spawnParticles && random.chance(20)
        }

        if (!spawnParticles) {
            return
        }

        val particlePosition = position + Vec3d.horizontal(
            { dimensions.x * ((2.0 * random.nextDouble() - 1.0) * 0.5) },
            dimensions.y * random.nextDouble()
        )
        if (effectAmbient) {
            ambientEntityEffectParticle ?: return
            connection.world += AmbientEntityEffectParticle(connection, particlePosition, effectColor, ambientEntityEffectParticle.default())
        } else {
            entityEffectParticle ?: return
            connection.world += EntityEffectParticle(connection, particlePosition, effectColor, entityEffectParticle.default())
        }
    }

    override fun tick() {
        super.tick()
        tickStatusEffects()

        if (isSleeping) {
            rotation = rotation.copy(pitch = 0.0f)
        }
    }

    companion object {
        private val FLAGS_DATA = EntityDataField("LIVING_ENTITY_FLAGS")
        private val HEALTH_DATA = EntityDataField("LIVING_ENTITY_HEALTH")
        private val EFFECT_COLOR_DATA = EntityDataField("LIVING_ENTITY_EFFECT_COLOR")
        private val EFFECT_AMBIENT_DATA = EntityDataField("LIVING_ENTITY_EFFECT_AMBIENCE")
        private val ARROW_COUNT_DATA = EntityDataField("LIVING_ENTITY_ARROW_COUNT")
        private val ABSORPTION_HEARTS_DATA = EntityDataField("LIVING_ENTITY_ABSORPTION_HEARTS")
        private val BED_POSITION_DATA = EntityDataField("LIVING_ENTITY_BED_POSITION")
    }
}
