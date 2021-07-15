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

package de.bixilon.minosoft.data.registries.blocks.types

import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockFactory
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3i
import kotlin.math.abs

open class HoneyBlock(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>) : Block(resourceLocation, registries, data) {

    override fun onEntityCollision(connection: PlayConnection, entity: Entity, blockState: BlockState, blockPosition: Vec3i) {
        super.onEntityCollision(connection, entity, blockState, blockPosition)

        if (isSliding(entity, blockPosition)) {
            if (entity.velocity.y < -0.13) {
                val horizontalMultiplier = -0.05 / entity.velocity.y
                entity.velocity.x *= horizontalMultiplier
                entity.velocity.z *= horizontalMultiplier
            }
            entity.velocity.y = -0.05
        }
    }

    private fun isSliding(entity: Entity, blockPosition: Vec3i): Boolean {
        if (entity.onGround || (entity.position.y > blockPosition.y + 0.9375 - 1.0E-7) || entity.velocity.y >= -0.08) {
            return false
        }
        val x = abs(blockPosition.x + 0.5 - entity.position.x) + 1.0E-7
        val z = abs(blockPosition.z + 0.5 - entity.position.z) + 1.0E-7
        val minSize = 0.4375 + (entity.dimensions.x / 2.0)
        return x > minSize || z > minSize
    }

    companion object : BlockFactory<HoneyBlock> {

        override fun build(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>): HoneyBlock {
            return HoneyBlock(resourceLocation, registries, data)
        }
    }
}

