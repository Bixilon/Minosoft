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
package de.bixilon.minosoft.data.entities.entities.monster

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions

class Skeleton(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : AbstractSkeleton(connection, entityType, data, position, rotation) {

    val isConverting: Boolean
        get() = data.getBoolean(CONVERTING_DATA, false)


    companion object : EntityFactory<Skeleton> {
        override val identifier: ResourceLocation = ResourceLocation("skeleton")
        private val CONVERTING_DATA = EntityDataField("SKELETON_STRAY_FREEZE_CONVERTING")
        private val LEGACY_TYPE_DATA = EntityDataField("LEGACY_SKELETON_TYPE")

        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): Skeleton {
            return Skeleton(connection, entityType, data, position, rotation)
        }

        override fun tweak(connection: PlayConnection, data: EntityData?, versionId: Int): ResourceLocation {
            if (data == null || versionId <= ProtocolVersions.V_1_8_9) {
                return identifier
            }
            val specialType = data.get(LEGACY_TYPE_DATA, 0)
            if (specialType == 1) {
                return WitherSkeleton.identifier
            }
            return identifier
        }
    }
}
