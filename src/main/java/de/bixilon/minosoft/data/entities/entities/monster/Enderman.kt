/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
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

import de.bixilon.minosoft.data.entities.EntityMetaDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.data.mappings.entities.EntityFactory
import de.bixilon.minosoft.data.mappings.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import glm_.vec3.Vec3d

class Enderman(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation) : AbstractSkeleton(connection, entityType, position, rotation) {

    // ToDo: No clue here
    @get:EntityMetaDataFunction(name = "Carried block")
    val carriedBlock: BlockState?
        get() = if (versionId <= ProtocolVersions.V_1_8_9) { // ToDo: No clue here
            connection.registries.getBlockState(entityMetaData.sets.getInt(EntityMetaDataFields.LEGACY_ENDERMAN_CARRIED_BLOCK) shl 4 or entityMetaData.sets.getInt(EntityMetaDataFields.LEGACY_ENDERMAN_CARRIED_BLOCK_DATA))
        } else {
            entityMetaData.sets.getBlock(EntityMetaDataFields.ENDERMAN_CARRIED_BLOCK)
        }

    @get:EntityMetaDataFunction(name = "Is screaming")
    val isScreaming: Boolean
        get() = entityMetaData.sets.getBoolean(EntityMetaDataFields.ENDERMAN_IS_SCREAMING)

    @get:EntityMetaDataFunction(name = "Is starring")
    val isStarring: Boolean
        get() = entityMetaData.sets.getBoolean(EntityMetaDataFields.ENDERMAN_IS_STARRING)


    companion object : EntityFactory<Enderman> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("enderman")

        override fun build(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation): Enderman {
            return Enderman(connection, entityType, position, rotation)
        }
    }
}
