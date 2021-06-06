/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
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

import de.bixilon.minosoft.data.entities.EntityMetaDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.Poses
import de.bixilon.minosoft.data.mappings.effects.attributes.DefaultStatusEffectAttributeNames
import de.bixilon.minosoft.data.mappings.entities.EntityType
import de.bixilon.minosoft.data.player.Hands
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.data.text.RGBColor.Companion.asRGBColor
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.spell.AmbientEntityEffectParticle
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.spell.EntityEffectParticle
import de.bixilon.minosoft.gui.rendering.util.VecUtil.horizontal
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.util.KUtil.chance
import glm_.vec3.Vec3d
import glm_.vec3.Vec3i

abstract class LivingEntity(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation) : Entity(connection, entityType, position, rotation) {
    private val entityEffectParticle = connection.registries.particleTypeRegistry[EntityEffectParticle]
    private val ambientEntityEffectParticle = connection.registries.particleTypeRegistry[AmbientEntityEffectParticle]

    private fun getLivingEntityFlag(bitMask: Int): Boolean {
        return entityMetaData.sets.getBitMask(EntityMetaDataFields.LIVING_ENTITY_FLAGS, bitMask)
    }

    @get:EntityMetaDataFunction(name = "Is hand active")
    val isHandActive: Boolean
        get() = getLivingEntityFlag(0x01)

    @get:EntityMetaDataFunction(name = "Main hand")
    open val mainHand: Hands?
        get() = if (getLivingEntityFlag(0x04)) Hands.OFF_HAND else Hands.MAIN_HAND

    @get:EntityMetaDataFunction(name = "Is auto spin attack")
    val isSpinAttacking: Boolean
        get() = getLivingEntityFlag(0x04)

    @get:EntityMetaDataFunction(name = "Health")
    open val health: Double
        get() {
            val meta = entityMetaData.sets.getFloat(EntityMetaDataFields.LIVING_ENTITY_HEALTH)
            return if (meta == Float.MIN_VALUE) {
                entityType.attributes[DefaultStatusEffectAttributeNames.GENERIC_MAX_HEALTH] ?: 1.0
            } else {
                meta.toDouble()
            }
        }

    @get:EntityMetaDataFunction(name = "Effect color")
    val effectColor: RGBColor
        get() = entityMetaData.sets.getInt(EntityMetaDataFields.LIVING_ENTITY_EFFECT_COLOR).asRGBColor()

    @get:EntityMetaDataFunction(name = "Is effect ambient")
    val effectAmbient: Boolean
        get() = entityMetaData.sets.getBoolean(EntityMetaDataFields.LIVING_ENTITY_EFFECT_AMBIENCE)

    @get:EntityMetaDataFunction(name = "Arrows in entity")
    val arrowCount: Int
        get() = entityMetaData.sets.getInt(EntityMetaDataFields.LIVING_ENTITY_ARROW_COUNT)

    @get:EntityMetaDataFunction(name = "Absorption hearts")
    val absorptionHearts: Int
        get() = entityMetaData.sets.getInt(EntityMetaDataFields.LIVING_ENTITY_ABSORPTION_HEARTS)

    @get:EntityMetaDataFunction(name = "Bed location")
    val bedPosition: Vec3i?
        get() = entityMetaData.sets.getBlockPosition(EntityMetaDataFields.LIVING_ENTITY_BED_POSITION)

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

    override fun realTick() {
        super.realTick()
        tickStatusEffects()

        if (isSleeping) {
            rotation = rotation.copy(pitch = 0.0)
        }
    }
}
