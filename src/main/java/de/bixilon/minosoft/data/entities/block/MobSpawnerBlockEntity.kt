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

package de.bixilon.minosoft.data.entities.block

import glm_.vec3.Vec3d
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.world.entities.WorldEntities
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.BlockPositionUtil.center
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.fire.SmokeParticle
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.slowing.FlameParticle
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.invoke
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

class MobSpawnerBlockEntity(session: PlaySession) : BlockEntity(session), BlockActionEntity {
    private val smokeParticleType = session.registries.particleType[SmokeParticle]
    private val flameParticleType = session.registries.particleType[FlameParticle]
    private var requiredPlayerRange = 16


    private fun isPlayerInRange(blockPosition: BlockPosition): Boolean {
        val lock = session.world.entities.lock
        if (!lock.acquire(10.milliseconds)) {  // Deadlock workaround
            return false
        }

        val inRadius = session.world.entities.getInRadius(blockPosition.center, requiredPlayerRange.toDouble(), WorldEntities.CHECK_CLOSEST_PLAYER).isNotEmpty()
        lock.release()

        return inRadius
    }

    private fun spawnParticles(blockPosition: BlockPosition, random: Random) {
        val particle = session.world.particle ?: return
        if (!isPlayerInRange(blockPosition)) {
            return
        }
        val particlePosition = Vec3d(blockPosition) + { random.nextDouble() }
        smokeParticleType?.let { particle += SmokeParticle(session, Vec3d(particlePosition), Vec3d.EMPTY, it.default()) }
        flameParticleType?.let { particle += FlameParticle(session, Vec3d(particlePosition), Vec3d.EMPTY, it.default()) }
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

    override fun tick(session: PlaySession, state: BlockState, position: BlockPosition, random: Random) {
        spawnParticles(position, random)
    }

    companion object : BlockEntityFactory<MobSpawnerBlockEntity> {
        override val identifier: ResourceLocation = minecraft("mob_spawner")

        override fun build(session: PlaySession): MobSpawnerBlockEntity {
            return MobSpawnerBlockEntity(session)
        }
    }
}
