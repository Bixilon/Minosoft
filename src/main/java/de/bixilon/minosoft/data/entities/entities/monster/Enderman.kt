/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
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
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions

class Enderman(session: PlaySession, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : AbstractSkeleton(session, entityType, data, position, rotation) {

    // ToDo: No clue here
    @get:SynchronizedEntityData
    val carriedBlock: BlockState?
        get() = if (session.version.versionId <= ProtocolVersions.V_1_8_9) { // ToDo: No clue here
            session.registries.blockState.getOrNull(data.get(CARRIED_BLOCK_DATA, 0) shl 4 or data.get(LEGACY_BLOCK_DATA_DATA, 0))
        } else {
            data.get<BlockState?>(CARRIED_BLOCK_DATA, null)
        }

    @get:SynchronizedEntityData
    val isScreaming: Boolean
        get() = data.getBoolean(SCREAMING_DATA, false)

    @get:SynchronizedEntityData
    val isStarring: Boolean
        get() = data.getBoolean(STARRING_DATA, false)


    companion object : EntityFactory<Enderman> {
        override val identifier: ResourceLocation = minecraft("enderman")
        private val CARRIED_BLOCK_DATA = EntityDataField("ENDERMAN_CARRIED_BLOCK")
        private val SCREAMING_DATA = EntityDataField("ENDERMAN_IS_SCREAMING")
        private val STARRING_DATA = EntityDataField("ENDERMAN_IS_STARRING")
        private val LEGACY_BLOCK_DATA = EntityDataField("LEGACY_ENDERMAN_CARRIED_BLOCK")
        private val LEGACY_BLOCK_DATA_DATA = EntityDataField("LEGACY_ENDERMAN_CARRIED_BLOCK_DATA")

        override fun build(session: PlaySession, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): Enderman {
            return Enderman(session, entityType, data, position, rotation)
        }
    }
}
