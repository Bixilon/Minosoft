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

package de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.entities.block.BedBlockEntity
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.factory.PixLyzerBlockFactory
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

open class BedBlock(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>) : BlockWithEntity<BedBlockEntity>(resourceLocation, registries, data) {

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

    companion object : PixLyzerBlockFactory<BedBlock> {

        override fun build(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>): BedBlock {
            return BedBlock(resourceLocation, registries, data)
        }
    }
}

