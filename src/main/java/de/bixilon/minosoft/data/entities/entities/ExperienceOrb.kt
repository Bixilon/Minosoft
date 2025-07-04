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
package de.bixilon.minosoft.data.entities.entities

import glm_.vec3.Vec3d
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

class ExperienceOrb : Entity {

    @get:SynchronizedEntityData
    val count: Int

    constructor(session: PlaySession, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : super(session, entityType, data, position, rotation) {
        count = 0
    }

    constructor(session: PlaySession, entityType: EntityType, data: EntityData, position: Vec3d, count: Int) : super(session, entityType, data, position, EntityRotation(0.0f, 0.0f)) {
        this.count = count
    }

    override fun onAttack(attacker: Entity): Boolean = false

    companion object : EntityFactory<ExperienceOrb> {
        override val identifier: ResourceLocation = minecraft("experience_orb")

        override fun build(session: PlaySession, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): ExperienceOrb {
            return ExperienceOrb(session, entityType, data, position, rotation)
        }
    }
}
