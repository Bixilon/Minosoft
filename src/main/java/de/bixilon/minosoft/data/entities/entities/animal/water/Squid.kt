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
package de.bixilon.minosoft.data.entities.entities.animal.water

import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3d

open class Squid(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation) : WaterAnimal(connection, entityType, position, rotation) {

    companion object : EntityFactory<Squid> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("squid")

        override fun build(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation): Squid {
            return Squid(connection, entityType, position, rotation)
        }
    }
}
