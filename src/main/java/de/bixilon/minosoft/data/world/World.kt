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
package de.bixilon.minosoft.data.world

import de.bixilon.kutil.concurrent.lock.RWLock
import de.bixilon.kutil.concurrent.worker.unconditional.UnconditionalWorker
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.kutil.random.RandomUtil.nextInt
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.properties.rendering.RandomDisplayTickable
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.world.audio.AbstractAudioPlayer
import de.bixilon.minosoft.data.world.audio.WorldAudioPlayer
import de.bixilon.minosoft.data.world.biome.WorldBiomes
import de.bixilon.minosoft.data.world.border.WorldBorder
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.light.section.ChunkLightUtil.hasSkyLight
import de.bixilon.minosoft.data.world.chunk.light.types.LightLevel
import de.bixilon.minosoft.data.world.chunk.manager.ChunkManager
import de.bixilon.minosoft.data.world.difficulty.WorldDifficulty
import de.bixilon.minosoft.data.world.entities.WorldEntities
import de.bixilon.minosoft.data.world.iterator.WorldIterator
import de.bixilon.minosoft.data.world.particle.AbstractParticleRenderer
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.time.WorldTime
import de.bixilon.minosoft.data.world.view.WorldView
import de.bixilon.minosoft.data.world.weather.WorldWeather
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import java.util.*

/**
 * Collection of chunks and more
 */
class World(
    val session: PlaySession,
) : WorldAudioPlayer {
    val lock = RWLock.rwlock()
    val random = Random()
    val biomes = WorldBiomes(this)
    val chunks = ChunkManager(this, 1000, 100)
    val entities = WorldEntities()
    var hardcore by observed(false)
    var dimension: DimensionProperties by observed(DimensionProperties())
    var difficulty: WorldDifficulty? by observed(null)
    var time by observed(WorldTime())
    var weather by observed(WorldWeather.SUNNY)
    val view = WorldView(session)
    val border = WorldBorder()

    var name: ResourceLocation? by observed(null)


    override var audio: AbstractAudioPlayer? = null
    var particle: AbstractParticleRenderer? = null

    var occlusion by observed(0)


    operator fun get(position: BlockPosition): BlockState? {
        return chunks[position.chunkPosition]?.get(position.inChunkPosition)
    }

    @Deprecated("chunks[position]", ReplaceWith("chunks[position]"))
    operator fun get(position: ChunkPosition): Chunk? {
        return chunks[position]
    }

    fun clear() {
        lock.lock()
        chunks.clear()
        time = WorldTime()
        weather = WorldWeather.SUNNY
        border.reset()
        lock.unlock()
    }

    operator fun set(x: Int, y: Int, z: Int, state: BlockState?) = set(BlockPosition(x, y, z), state)

    operator fun set(position: BlockPosition, state: BlockState?) {
        if (!isValidPosition(position)) return
        val chunk = chunks[position.chunkPosition] ?: return
        chunk[position.inChunkPosition] = state
    }

    fun isPositionChangeable(blockPosition: BlockPosition): Boolean {
        if (border.isOutside(blockPosition)) {
            return false
        }
        return isValidPosition(blockPosition)
    }

    fun isValidPosition(position: BlockPosition): Boolean {
        val dimension = session.world.dimension
        if (position.y < dimension.minY || position.y > dimension.maxY) return false
        return true
    }

    fun getBlockEntity(position: BlockPosition): BlockEntity? {
        return chunks[position.chunkPosition]?.getBlockEntity(position.inChunkPosition)
    }

    fun getOrPutBlockEntity(blockPosition: BlockPosition): BlockEntity? {
        return chunks[blockPosition.chunkPosition]?.getOrPutBlockEntity(blockPosition.inChunkPosition)
    }

    operator fun set(position: BlockPosition, entity: BlockEntity) {
        this.set(position, entity as BlockEntity?)
    }

    @JvmName("set2")
    fun set(position: BlockPosition, entity: BlockEntity?) {
        chunks[position.chunkPosition]?.set(position.inChunkPosition, entity) // TODO: fire event if needed
    }

    fun tick() {
        val simulationDistance = view.simulationDistance
        val cameraPosition = session.player.physics.positionInfo.chunkPosition
        lock.acquire()
        chunks.tick(simulationDistance, cameraPosition)
        lock.release()
        border.tick()
    }

    fun randomDisplayTick() {
        val origin = session.player.physics.positionInfo.position
        val chunk = this.chunks[origin.chunkPosition] ?: return

        for (i in 0 until 667) {
            randomDisplayTick(16, origin, chunk)
            randomDisplayTick(32, origin, chunk)
        }
    }

    private fun randomDisplayTick(radius: Int, origin: BlockPosition, chunk: Chunk) {
        val position = BlockPosition(
            x = origin.x + random.nextInt(-radius, radius),
            y = origin.y + random.nextInt(-radius, radius),
            z = origin.z + random.nextInt(-radius, radius),
        )

        val state = chunk.neighbours.traceBlock(position) ?: return
        if (state.block !is RandomDisplayTickable) return
        if (!state.block.hasRandomTicks(session, state, position)) return

        state.block.randomDisplayTick(session, state, position, random)
    }

    operator fun get(aabb: AABB): WorldIterator {
        return WorldIterator(aabb.positions(), this)
    }

    fun isSpaceEmpty(entity: Entity? = null, aabb: AABB, chunk: Chunk? = null, fluids: Boolean = false): Boolean {
        val iterator = WorldIterator(aabb, this, chunk)

        if (entity == null) {
            return !iterator.hasCollisions(fluids)
        }
        return !iterator.hasCollisions(entity, aabb, fluids)
    }

    fun getLight(position: BlockPosition): LightLevel {
        return chunks[position.chunkPosition]?.light?.get(position.inChunkPosition) ?: LightLevel.EMPTY
    }

    fun getBrightness(position: BlockPosition): Float {
        val level = getLight(position)
        var max = level.block
        if (dimension.hasSkyLight()) {
            max = maxOf(max, level.sky)
        }
        return dimension.ambientLight[max]
    }

    fun recalculateLight(heightmap: Boolean = false) {
        val clear = UnconditionalWorker(autoWork = true)
        val calculate = UnconditionalWorker(autoWork = false)
        val events = UnconditionalWorker(autoWork = false)
        lock.acquire()
        for (chunk in chunks.chunks.unsafe.values) {
            clear += { chunk.light.clear() }
            calculate += {
                if (heightmap) {
                    chunk.light.heightmap.recalculate()
                }
                chunk.light.calculate()
            }
            events += { chunk.light.fireNeighbourEvents() }
        }
        lock.release()
        clear.work()
        calculate.work()
        events.work()
    }

    companion object {
        const val MAX_RENDER_DISTANCE = 64 // TODO: This limit can be increased, but you might also need to increase stack size, otherwise things like occlusion tracing fail with a StackOverflowError
        const val MAX_CHUNKS_SIZE = MAX_RENDER_DISTANCE * 2 + 1
    }
}
