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
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

class Creeper(session: PlaySession, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : Monster(session, entityType, data, position, rotation) {

    @get:SynchronizedEntityData
    val fuseState: Int
        get() = data.get(FUSE_STATE_DATA, -1)

    @get:SynchronizedEntityData
    val isCharged: Boolean
        get() = data.getBoolean(IS_CHARGED_DATA, false)

    @get:SynchronizedEntityData
    val isIgnited: Boolean
        get() = data.getBoolean(IS_IGNITED_DATA, false)


    companion object : EntityFactory<Creeper> {
        override val identifier: ResourceLocation = minecraft("creeper")
        private val FUSE_STATE_DATA = EntityDataField("CREEPER_STATE")
        private val IS_CHARGED_DATA = EntityDataField("CREEPER_IS_CHARGED")
        private val IS_IGNITED_DATA = EntityDataField("CREEPER_IS_IGNITED")

        override fun build(session: PlaySession, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): Creeper {
            return Creeper(session, entityType, data, position, rotation)
        }
    }
}
