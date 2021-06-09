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

package de.bixilon.minosoft.data.mappings.blocks.types

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.data.mappings.versions.Registries
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3i

open class BedBlock(resourceLocation: ResourceLocation, registries: Registries, data: JsonObject) : Block(resourceLocation, registries, data) {

    override fun onEntityLand(connection: PlayConnection, entity: Entity, blockPosition: Vec3i, blockState: BlockState) {
        super.onEntityLand(connection, entity, blockPosition, blockState)

        if (entity.isSneaking) {
            return
        }

        bounce(entity)
    }

    private fun bounce(entity: Entity) {
        if (entity.velocity.y < 0.0) {
            entity.velocity.y = -entity.velocity.y * 0.66f
        }
    }
}

