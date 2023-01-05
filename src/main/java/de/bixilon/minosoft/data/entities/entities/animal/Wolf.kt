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
package de.bixilon.minosoft.data.entities.entities.animal

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.entities.entities.TamableAnimal
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.KUtil

class Wolf(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : TamableAnimal(connection, entityType, data, position, rotation) {

    @get:SynchronizedEntityData
    val isBegging: Boolean
        get() = data.getBoolean(IS_BEGGING_DATA, false)

    @get:SynchronizedEntityData
    val collarColor: RGBColor
        get() = ChatColors.VALUES.getOrNull(data.get(COLLAR_COLOR_DATA, 0)) ?: ChatColors.WHITE

    // ToDo
    @get:SynchronizedEntityData
    val angerTime: Int
        get() = if (connection.version.versionId <= ProtocolVersions.V_1_8_9) { // ToDo
            // ToDo if (data.sets.getBitMask(EntityDataFields.TAMABLE_ENTITY_FLAGS, 0x02)) 1 else 0
            0
        } else {
            data.get(ANGER_TIME_DATA, 0)
        }

    @get:SynchronizedEntityData
    override val health: Double
        get() = if (connection.version.versionId > ProtocolVersions.V_19W45B) {
            super.health
        } else {
            data.get<Number>(HEALTH_DATA, 0.0f).toDouble()
        }

    companion object : EntityFactory<Wolf> {
        override val identifier: ResourceLocation = minecraft("wolf")
        private val IS_BEGGING_DATA = EntityDataField("WOLF_IS_BEGGING")
        private val COLLAR_COLOR_DATA = EntityDataField("WOLF_COLLAR_COLOR")
        private val ANGER_TIME_DATA = EntityDataField("WOLF_ANGER_TIME")
        private val HEALTH_DATA = EntityDataField("WOLF_HEALTH")

        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): Wolf {
            return Wolf(connection, entityType, data, position, rotation)
        }
    }
}
