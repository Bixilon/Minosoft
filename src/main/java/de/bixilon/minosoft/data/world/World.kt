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
package de.bixilon.minosoft.data.world

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.kutil.concurrent.worker.unconditional.UnconditionalWorker
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.kutil.random.RandomUtil.nextInt
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.properties.rendering.RandomDisplayTickable
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.world.audio.AbstractAudioPlayer
import de.bixilon.minosoft.data.world.audio.WorldAudioPlayer
import de.bixilon.minosoft.data.world.biome.accessor.BiomeAccessor
import de.bixilon.minosoft.data.world.biome.accessor.NoiseBiomeAccessor
import de.bixilon.minosoft.data.world.border.WorldBorder
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.light.ChunkLight.Companion.canSkylight
import de.bixilon.minosoft.data.world.chunk.light.SectionLight
import de.bixilon.minosoft.data.world.chunk.manager.ChunkManager
import de.bixilon.minosoft.data.world.difficulty.WorldDifficulty
import de.bixilon.minosoft.data.world.entities.WorldEntities
import de.bixilon.minosoft.data.world.iterator.WorldIterator
import de.bixilon.minosoft.data.world.particle.AbstractParticleRenderer
import de.bixilon.minosoft.data.world.particle.WorldParticleRenderer
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.ChunkPositionUtil.chunkPosition
import de.bixilon.minosoft.data.world.positions.ChunkPositionUtil.inChunkPosition
import de.bixilon.minosoft.data.world.time.WorldTime
import de.bixilon.minosoft.data.world.view.WorldView
import de.bixilon.minosoft.data.world.weather.WorldWeather
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.EMPTY
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import java.util.*

/**
 * Collection of chunks and more
 */
class World(
    val connection: PlayConnection,
) : BiomeAccessor, WorldAudioPlayer, WorldParticleRenderer {
    val lock = SimpleLock()
    val random = Random()
    var cacheBiomeAccessor: NoiseBiomeAccessor? = null
    val chunks = ChunkManager(this)
    val entities = WorldEntities()
    var hardcore by observed(false)
    var dimension: DimensionProperties by observed(DimensionProperties())
    var difficulty: WorldDifficulty? by observed(null)
    var time by observed(WorldTime())
    var weather by observed(WorldWeather.SUNNY)
    val view = WorldView(connection)
    val border = WorldBorder()

    var name: ResourceLocation? by observed(null)


    override var audioPlayer: AbstractAudioPlayer? = null
    override var particleRenderer: AbstractParticleRenderer? = null

    var occlusion by observed(0)


    operator fun get(x: Int, y: Int, z: Int): BlockState? {
        return chunks[Vec2i(x shr 4, z shr 4)]?.get(x and 0x0F, y, z and 0x0F)
    }

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

    operator fun set(x: Int, y: Int, z: Int, state: BlockState?) {
        if (!isValidPosition(x, y, z)) return
        val chunk = chunks[x shr 4, z shr 4] ?: return
        chunk[x and 0x0F, y, z and 0x0F] = state
    }

    operator fun set(position: BlockPosition, state: BlockState?) {
        val chunk = chunks[position.chunkPosition] ?: return
        chunk[position.inChunkPosition] = state
    }

    fun isPositionChangeable(blockPosition: BlockPosition): Boolean {
        if (border.isOutside(blockPosition)) {
            return false
        }
        return isValidPosition(blockPosition)
    }

    fun isValidPosition(x: Int, y: Int, z: Int): Boolean {
        if (x > MAX_SIZE || z > MAX_SIZE) return false
        val dimension = connection.world.dimension
        if (y < dimension.minY || y > dimension.maxY) return false
        return true
    }

    fun isValidPosition(position: BlockPosition) = isValidPosition(position.x, position.y, position.z)
    fun isValidPosition(position: ChunkPosition): Boolean {
        return true // TODO
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

    override fun getBiome(blockPosition: BlockPosition): Biome? {
        return chunks[blockPosition.chunkPosition]?.getBiome(blockPosition.inChunkPosition)
    }

    override fun getBiome(x: Int, y: Int, z: Int): Biome? {
        return this.chunks[Vec2i(x shr 4, z shr 4)]?.getBiome(x and 0x0F, y, z and 0x0F)
    }

    fun tick() {
        val simulationDistance = view.simulationDistance
        val cameraPosition = connection.player.physics.positionInfo.chunkPosition
        lock.acquire()
        chunks.tick(simulationDistance, cameraPosition)
        lock.release()
        border.tick()
    }

    fun randomDisplayTick() {
        val origin = connection.player.physics.positionInfo.blockPosition
        val chunk = this.chunks[origin.chunkPosition] ?: return

        val position = Vec3i.EMPTY
        val chunkDelta = Vec2i.EMPTY

        for (i in 0 until 667) {
            randomDisplayTick(16, origin, position, chunkDelta, chunk)
            randomDisplayTick(32, origin, position, chunkDelta, chunk)
        }
    }

    private fun randomDisplayTick(radius: Int, origin: BlockPosition, position: BlockPosition, chunkDelta: Vec2i, chunk: Chunk) {
        position.x = origin.x + random.nextInt(-radius, radius)
        position.y = origin.x + random.nextInt(-radius, radius)
        position.z = origin.x + random.nextInt(-radius, radius)

        chunkDelta.x = (origin.x - position.x) shr 4
        chunkDelta.y = (origin.z - position.z) shr 4

        val state = chunk.traceBlock(position.x and 0x0F, position.y, position.z and 0x0F, chunkDelta) ?: return
        if (state.block !is RandomDisplayTickable) return
        if (!state.block.hasRandomTicks(connection, state, position)) return

        state.block.randomDisplayTick(connection, state, position, random)
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

    fun getLight(position: BlockPosition): Int {
        return chunks[position.chunkPosition]?.light?.get(position.inChunkPosition) ?: 0x00
    }

    fun getBrightness(position: BlockPosition): Float {
        val light = getLight(position)
        var level = light and SectionLight.BLOCK_LIGHT_MASK
        if (dimension.canSkylight()) {
            level = maxOf(level, light and SectionLight.SKY_LIGHT_MASK shr 4)
        }
        return dimension.ambientLight[level]
    }

    fun recalculateLight(heightmap: Boolean = false) {
        val reset = UnconditionalWorker()
        val calculate = UnconditionalWorker()
        lock.acquire()
        for (chunk in chunks.chunks.unsafe.values) {
            reset += { chunk.light.reset() }
            calculate += {
                if (heightmap) {
                    chunk.light.recalculateHeightmap()
                }
                chunk.light.calculate()
            }
        }
        lock.release()
        reset.work()
        calculate.work()
    }

    companion object {
        const val MAX_SIZE = 30_000_000
        const val MAX_SIZEf = MAX_SIZE.toFloat()
        const val MAX_SIZEd = MAX_SIZE.toDouble()
        const val MAX_RENDER_DISTANCE = 64
        const val MAX_CHUNKS_SIZE = MAX_RENDER_DISTANCE * 2 + 1
    }
}
