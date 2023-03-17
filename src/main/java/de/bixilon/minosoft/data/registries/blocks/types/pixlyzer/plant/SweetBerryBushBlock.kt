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

package de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.plant

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.LivingEntity
import de.bixilon.minosoft.data.registries.blocks.factory.PixLyzerBlockFactory
import de.bixilon.minosoft.data.registries.blocks.handler.entity.EntityCollisionHandler
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.physics.entities.EntityPhysics

open class SweetBerryBushBlock(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>) : PlantBlock(resourceLocation, registries, data), EntityCollisionHandler {

    override fun onEntityCollision(entity: Entity, physics: EntityPhysics<*>, position: Vec3i, state: BlockState) {
        if (entity !is LivingEntity) {
            return
        }
        physics.slowMovement(state, Vec3d(0.8f, 0.75, 0.8f))
    }

    companion object : PixLyzerBlockFactory<SweetBerryBushBlock> {

        override fun build(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>): SweetBerryBushBlock {
            return SweetBerryBushBlock(resourceLocation, registries, data)
        }
    }
}
