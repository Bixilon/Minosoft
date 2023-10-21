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

package de.bixilon.minosoft.data.entities.block

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.world.entities.WorldEntities
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.fire.SmokeParticle
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.slowing.FlameParticle
import de.bixilon.minosoft.gui.rendering.util.VecUtil.center
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3d
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import java.util.*

class MobSpawnerBlockEntity(connection: PlayConnection) : BlockEntity(connection), BlockActionEntity {
    private val smokeParticleType = connection.registries.particleType[SmokeParticle]
    private val flameParticleType = connection.registries.particleType[FlameParticle]
    private var requiredPlayerRange = 16


    private fun isPlayerInRange(blockPosition: Vec3i): Boolean {
        return connection.world.entities.getInRadius(blockPosition.center, requiredPlayerRange.toDouble(), WorldEntities.CHECK_CLOSEST_PLAYER).isNotEmpty()
    }

    private fun spawnParticles(blockPosition: Vec3i, random: Random) {
        if (!isPlayerInRange(blockPosition)) {
            return
        }
        val particlePosition = blockPosition.toVec3d + { random.nextDouble() }
        smokeParticleType?.let { connection.world += SmokeParticle(connection, Vec3d(particlePosition), Vec3d.EMPTY, it.default()) }
        flameParticleType?.let { connection.world += FlameParticle(connection, Vec3d(particlePosition), Vec3d.EMPTY, it.default()) }
    }

    override fun setBlockActionData(type: Int, data: Int) {
        // ToDo
    }

    override fun updateNBT(nbt: Map<String, Any>) {
        nbt["MaxNearbyEntities"]?.let {
            requiredPlayerRange = nbt["MaxNearbyEntities"]?.toInt() ?: 16
        }
        // ToDo: {MaxNearbyEntities: 6s, RequiredPlayerRange: 16s, SpawnCount: 4s, x: -80, y: 4, SpawnData: {id: "minecraft:zombie"}, z: 212, id: "minecraft:mob_spawner", MaxSpawnDelay: 800s, SpawnRange: 4s, Delay: 0s, MinSpawnDelay: 200s}
    }

    override fun tick(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, random: Random) {
        spawnParticles(blockPosition, random)
    }

    companion object : BlockEntityFactory<MobSpawnerBlockEntity> {
        override val identifier: ResourceLocation = minecraft("mob_spawner")

        override fun build(connection: PlayConnection): MobSpawnerBlockEntity {
            return MobSpawnerBlockEntity(connection)
        }
    }
}
