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

import de.bixilon.minosoft.data.entities.EntityDataFields
import de.bixilon.minosoft.data.player.Hands
import de.bixilon.minosoft.data.registries.effects.attributes.DefaultStatusEffectAttributeNames
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.data.text.RGBColor.Companion.asRGBColor
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.spell.AmbientEntityEffectParticle
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.spell.EntityEffectParticle
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import glm_.vec3.Vec3i

abstract class LivingEntity(connection: PlayConnection, entityType: EntityType) : Entity(connection, entityType) {
    private val entityEffectParticle = connection.registries.particleTypeRegistry[EntityEffectParticle]
    private val ambientEntityEffectParticle = connection.registries.particleTypeRegistry[AmbientEntityEffectParticle]

    private fun getLivingEntityFlag(bitMask: Int): Boolean {
        return data.sets.getBitMask(EntityDataFields.LIVING_ENTITY_FLAGS, bitMask)
    }

    @get:EntityMetaDataFunction(name = "Is hand active")
    val isHandActive: Boolean
        get() = getLivingEntityFlag(0x01)

    @get:EntityMetaDataFunction(name = "Main hand")
    open var activeHand: Hands?
        get() = if (getLivingEntityFlag(0x02)) Hands.OFF else Hands.MAIN
        set(value) {}

    @get:EntityMetaDataFunction(name = "Is auto spin attack") // aka using riptide
    val isSpinAttacking: Boolean
        get() = getLivingEntityFlag(0x04)

    @get:EntityMetaDataFunction(name = "Health")
    open val health: Double
        get() = data.sets.getOptionalFloat(EntityDataFields.LIVING_ENTITY_HEALTH)?.toDouble() ?: type.attributes[DefaultStatusEffectAttributeNames.GENERIC_MAX_HEALTH] ?: 1.0

    @get:EntityMetaDataFunction(name = "Effect color")
    val effectColor: RGBColor
        get() = data.sets.getInt(EntityDataFields.LIVING_ENTITY_EFFECT_COLOR).asRGBColor()

    @get:EntityMetaDataFunction(name = "Is effect ambient")
    val effectAmbient: Boolean
        get() = data.sets.getBoolean(EntityDataFields.LIVING_ENTITY_EFFECT_AMBIENCE)

    @get:EntityMetaDataFunction(name = "Arrows in entity")
    val arrowCount: Int
        get() = data.sets.getInt(EntityDataFields.LIVING_ENTITY_ARROW_COUNT)

    @get:EntityMetaDataFunction(name = "Absorption hearts")
    val absorptionHearts: Int
        get() = data.sets.getInt(EntityDataFields.LIVING_ENTITY_ABSORPTION_HEARTS)

    @get:EntityMetaDataFunction(name = "Bed location")
    val bedPosition: Vec3i?
        get() = data.sets.getBlockPosition(EntityDataFields.LIVING_ENTITY_BED_POSITION)

    open val isSleeping: Boolean
        get() = bedPosition != null
}
