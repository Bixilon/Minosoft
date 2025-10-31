/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.entities.entities.boss.enderdragon

import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

class EndCrystal(session: PlaySession, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : Entity(session, entityType, data, position, rotation) {

    override val canRaycast: Boolean get() = true

    @get:SynchronizedEntityData
    val beamTarget: BlockPosition?
        get() = data.get(BEAM_TARGET_DATA, null)

    @get:SynchronizedEntityData
    val showBottom: Boolean
        get() = data.getBoolean(SHOW_BOTTOM_DATA, true)


    companion object : EntityFactory<EndCrystal> {
        override val identifier = minecraft("end_crystal")
        private val BEAM_TARGET_DATA = EntityDataField("END_CRYSTAL_BEAM_TARGET")
        private val SHOW_BOTTOM_DATA = EntityDataField("END_CRYSTAL_SHOW_BOTTOM")


        override fun build(session: PlaySession, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): EndCrystal {
            return EndCrystal(session, entityType, data, position, rotation)
        }
    }
}
