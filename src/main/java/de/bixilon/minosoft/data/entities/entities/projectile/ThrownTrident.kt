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
package de.bixilon.minosoft.data.entities.entities.projectile

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil

class ThrownTrident(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : AbstractArrow(connection, entityType, data, position, rotation) {

    @get:SynchronizedEntityData
    val loyaltyLevel: Byte
        get() = data.get(TRIDENT_LOYALTY_LEVEL_DATA, 0.toByte())

    @get:SynchronizedEntityData
    val isEnchanted: Boolean
        get() = data.getBoolean(ENCHANTED_DATA, false)

    companion object : EntityFactory<ThrownTrident> {
        override val identifier: ResourceLocation = KUtil.minecraft("trident")
        private val TRIDENT_LOYALTY_LEVEL_DATA = EntityDataField("THROWN_TRIDENT_LOYALTY_LEVEL")
        private val ENCHANTED_DATA = EntityDataField("THROWN_TRIDENT_FOIL")


        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): ThrownTrident {
            return ThrownTrident(connection, entityType, data, position, rotation)
        }
    }
}
