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
package de.bixilon.minosoft.data.entities.entities.item

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.physics.entities.item.FallingBlockPhysics
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class FallingBlockEntity(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : Entity(connection, entityType, data, position, rotation) {

    @get:SynchronizedEntityData
    var blockState: BlockState? = null
        private set

    @get:SynchronizedEntityData
    val spawnPosition: Vec3i?
        get() = data.get(SPAWN_POSITION_DATA, null)


    override fun onAttack(attacker: Entity): Boolean = false
    override fun createPhysics() = FallingBlockPhysics(this)

    override fun setObjectData(data: Int) {
        blockState = connection.registries.blockState.getOrNull(data)
    }

    override fun tick() {
        if (blockState == null) return // TODO: discard
        super.tick()
    }

    companion object : EntityFactory<FallingBlockEntity> {
        override val identifier: ResourceLocation = minecraft("falling_block")
        private val SPAWN_POSITION_DATA = EntityDataField("FALLING_BLOCK_SPAWN_POSITION")


        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): FallingBlockEntity {
            return FallingBlockEntity(connection, entityType, data, position, rotation)
        }
    }
}
