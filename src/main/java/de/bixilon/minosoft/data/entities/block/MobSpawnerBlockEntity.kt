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

package de.bixilon.minosoft.data.entities.block

import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.data.world.WorldEntities
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.fire.SmokeParticle
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.slowing.FlameParticle
import de.bixilon.minosoft.gui.rendering.util.VecUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.VecUtil.center
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3d
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.util.KUtil.nullCast
import glm_.vec3.Vec3d
import glm_.vec3.Vec3i
import kotlin.random.Random

class MobSpawnerBlockEntity(connection: PlayConnection) : BlockEntity(connection), BlockActionEntity {
    private val smokeParticleType = connection.registries.particleTypeRegistry[SmokeParticle]
    private val flameParticleType = connection.registries.particleTypeRegistry[FlameParticle]
    private var requiredPlayerRange = 16


    private fun isPlayerInRange(blockPosition: Vec3i): Boolean {
        return connection.world.entities.getInRadius(blockPosition.center, requiredPlayerRange.toDouble(), WorldEntities.CHECK_CLOSEST_PLAYER).isNotEmpty()
    }

    private fun spawnParticles(blockPosition: Vec3i) {
        if (!isPlayerInRange(blockPosition)) {
            return
        }
        val particlePosition = blockPosition.toVec3d + { Random.nextDouble() }
        smokeParticleType?.let { connection.world += SmokeParticle(connection, Vec3d(particlePosition), Vec3d.EMPTY, it.default()) }
        flameParticleType?.let { connection.world += FlameParticle(connection, Vec3d(particlePosition), Vec3d.EMPTY, it.default()) }
    }

    override fun setBlockActionData(data1: Byte, data2: Byte) {
        // ToDo
    }

    override fun updateNBT(nbt: Map<String, Any>) {
        nbt["MaxNearbyEntities"]?.let {
            requiredPlayerRange = nbt["MaxNearbyEntities"]?.nullCast<Number>()?.toInt() ?: 16
        }
        // ToDo: {MaxNearbyEntities: 6s, RequiredPlayerRange: 16s, SpawnCount: 4s, x: -80, y: 4, SpawnData: {id: "minecraft:zombie"}, z: 212, id: "minecraft:mob_spawner", MaxSpawnDelay: 800s, SpawnRange: 4s, Delay: 0s, MinSpawnDelay: 200s}
    }

    override fun realTick(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i) {
        spawnParticles(blockPosition)
    }

    companion object : BlockEntityFactory<MobSpawnerBlockEntity> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("minecraft:mob_spawner")

        override fun build(connection: PlayConnection): MobSpawnerBlockEntity {
            return MobSpawnerBlockEntity(connection)
        }
    }
}
