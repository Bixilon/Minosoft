/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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
import de.bixilon.kutil.collections.CollectionUtil.lockMapOf
import de.bixilon.kutil.collections.map.LockMap
import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.kutil.concurrent.pool.ThreadPoolRunnable
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.watcher.DataWatcher.Companion.watched
import de.bixilon.minosoft.data.Difficulties
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.registries.AABB
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.FluidBlock
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.world.audio.AbstractAudioPlayer
import de.bixilon.minosoft.data.world.audio.WorldAudioPlayer
import de.bixilon.minosoft.data.world.biome.accessor.BiomeAccessor
import de.bixilon.minosoft.data.world.biome.accessor.NoiseBiomeAccessor
import de.bixilon.minosoft.data.world.border.WorldBorder
import de.bixilon.minosoft.data.world.particle.AbstractParticleRenderer
import de.bixilon.minosoft.data.world.particle.WorldParticleRenderer
import de.bixilon.minosoft.data.world.time.WorldTime
import de.bixilon.minosoft.data.world.view.WorldView
import de.bixilon.minosoft.data.world.weather.WorldWeather
import de.bixilon.minosoft.gui.rendering.util.VecUtil.minus
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.blockPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.chunkPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.inChunkPosition
import de.bixilon.minosoft.modding.event.EventInitiators
import de.bixilon.minosoft.modding.event.events.BlockSetEvent
import de.bixilon.minosoft.modding.event.events.ChunkDataChangeEvent
import de.bixilon.minosoft.modding.event.events.ChunkUnloadEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.chunk.ChunkUtil.canBuildBiomeCache
import de.bixilon.minosoft.util.chunk.ChunkUtil.getChunkNeighbourPositions
import de.bixilon.minosoft.util.chunk.ChunkUtil.isInViewDistance
import de.bixilon.minosoft.util.chunk.ChunkUtil.received
import kotlin.random.Random

/**
 * Collection of chunks and more
 */
class World(
    val connection: PlayConnection,
) : BiomeAccessor, WorldAudioPlayer, WorldParticleRenderer {
    val lock = SimpleLock()
    var cacheBiomeAccessor: NoiseBiomeAccessor? = null
    val chunks: LockMap<Vec2i, Chunk> = lockMapOf()
    val entities = WorldEntities()
    var hardcore by watched(false)
    var dimension: DimensionProperties? by watched(null)
    var difficulty: Difficulties? by watched(null)
    var difficultyLocked by watched(false)
    var hashedSeed = 0L
    val time = WorldTime(this)
    val weather = WorldWeather()
    val view = WorldView(connection)
    val border = WorldBorder()
    private val random = Random

    override var audioPlayer: AbstractAudioPlayer? = null
    override var particleRenderer: AbstractParticleRenderer? = null
    var occlusionUpdateCallback: OcclusionUpdateCallback? = null

    var chunkMin = Vec2i(Int.MAX_VALUE)
    var chunkMax = Vec2i(Int.MIN_VALUE)
    var chunkSize = Vec2i.EMPTY

    operator fun get(blockPosition: Vec3i): BlockState? {
        return chunks[blockPosition.chunkPosition]?.get(blockPosition.inChunkPosition)
    }

    operator fun get(chunkPosition: Vec2i): Chunk? {
        return chunks[chunkPosition]
    }

    private fun updateChunkExtreme(chunkPosition: Vec2i, mass: Boolean = false) {
        var changes = 0
        if (chunkPosition.x < chunkMin.x) {
            chunkMin.x = chunkPosition.x
            changes++
        }
        if (chunkPosition.y < chunkMin.y) {
            chunkMin.y = chunkPosition.y
            changes++
        }
        if (chunkPosition.x > chunkMax.x) {
            chunkMax.x = chunkPosition.x
            changes++
        }
        if (chunkPosition.y > chunkMax.y) {
            chunkMax.y = chunkPosition.y
            changes++
        }
        if (!mass && changes > 0) {
            updateWorldSize()
            view.updateServerDistance()
        }
    }

    private fun updateWorldSize() {
        val nextSize = (chunkMax - chunkMin)
        if (chunks.unsafe.isNotEmpty()) {
            nextSize += 1 // own chunk
        }
        if (nextSize.x > MAX_CHUNKS_SIZE) {
            nextSize.x = MAX_CHUNKS_SIZE
        }
        if (nextSize.x < 0) {
            nextSize.x = 0
        }
        if (nextSize.y > MAX_CHUNKS_SIZE) {
            nextSize.y = MAX_CHUNKS_SIZE
        }
        if (nextSize.y < 0) {
            nextSize.y = 0
        }
        this.chunkSize = nextSize
    }

    private fun recalculateChunkExtreme() {
        chunks.lock.acquire()

        chunkMin = Vec2i(Int.MAX_VALUE)
        chunkMax = Vec2i(Int.MIN_VALUE)

        for (chunkPosition in chunks.keys) {
            updateChunkExtreme(chunkPosition, true)
        }
        updateWorldSize()
        view.updateServerDistance()

        chunks.lock.release()
    }

    fun clear() {
        chunks.lock.lock()
        chunks.unsafe.clear()
        chunkMin = Vec2i(Int.MAX_VALUE)
        chunkMax = Vec2i(Int.MIN_VALUE)
        updateWorldSize()
        view.updateServerDistance()
        chunks.lock.unlock()
    }

    fun getOrCreateChunk(chunkPosition: Vec2i): Chunk {
        updateChunkExtreme(chunkPosition)
        return chunks.synchronizedGetOrPut(chunkPosition) { Chunk(connection, chunkPosition) }
    }

    private fun _set(chunk: Chunk, blockPosition: Vec3i, blockState: BlockState?) {
        val inChunkPosition = blockPosition.inChunkPosition
        val previousBlock = chunk[inChunkPosition]
        if (previousBlock == blockState) {
            return
        }
        previousBlock?.block?.onBreak(connection, blockPosition, previousBlock, chunk.getBlockEntity(inChunkPosition))
        blockState?.block?.onPlace(connection, blockPosition, blockState)
        chunk[inChunkPosition] = blockState
        chunk.getOrPutBlockEntity(inChunkPosition)
        connection.fireEvent(BlockSetEvent(
            connection = connection,
            blockPosition = blockPosition,
            blockState = blockState,
        ))
    }

    fun setBlockState(blockPosition: Vec3i, blockState: BlockState?) {
        this[blockPosition] = blockState
    }

    operator fun set(blockPosition: Vec3i, blockState: BlockState?) {
        val chunk = chunks[blockPosition.chunkPosition] ?: return
        _set(chunk, blockPosition, blockState)
    }

    fun forceSet(blockPosition: Vec3i, blockState: BlockState?) {
        val chunk = getOrCreateChunk(blockPosition.chunkPosition)
        _set(chunk, blockPosition, blockState)
    }

    fun isPositionChangeable(blockPosition: Vec3i): Boolean {
        if (border.isOutside(blockPosition)) {
            return false
        }
        val dimension = connection.world.dimension!!
        return (blockPosition.y >= dimension.minY || blockPosition.y < dimension.maxY)
    }

    fun forceSetBlockState(blockPosition: Vec3i, blockState: BlockState?, check: Boolean = false) {
        if (check && !isPositionChangeable(blockPosition)) {
            return
        }
        chunks[blockPosition.chunkPosition]?.set(blockPosition.inChunkPosition, blockState)
    }

    fun unloadChunk(chunkPosition: Vec2i) {
        val chunk = chunks.remove(chunkPosition) ?: return
        val neighbourPositions = getChunkNeighbourPositions(chunkPosition)
        for (neighbourPosition in neighbourPositions) {
            val neighbour = this[neighbourPosition] ?: continue
            neighbour.neighboursLoaded = false
            connection.fireEvent(ChunkDataChangeEvent(connection, EventInitiators.UNKNOWN, neighbourPosition, neighbour))
        }
        // connection.world.view.updateServerViewDistance(chunkPosition, false)
        connection.fireEvent(ChunkUnloadEvent(connection, EventInitiators.UNKNOWN, chunkPosition, chunk))
        if (chunkPosition.x <= chunkMin.x || chunkPosition.y <= chunkMin.y || chunkPosition.x >= chunkMax.x || chunkPosition.y >= chunkMax.y) {
            recalculateChunkExtreme()
        }
        occlusionUpdateCallback?.onOcclusionChange()
    }

    fun getBlockEntity(blockPosition: Vec3i): BlockEntity? {
        return get(blockPosition.chunkPosition)?.getBlockEntity(blockPosition.inChunkPosition)
    }

    fun getOrPutBlockEntity(blockPosition: Vec3i): BlockEntity? {
        return get(blockPosition.chunkPosition)?.getOrPutBlockEntity(blockPosition.inChunkPosition)
    }

    fun setBlockEntity(blockPosition: Vec3i, blockEntity: BlockEntity?) {
        get(blockPosition.chunkPosition)?.setBlockEntity(blockPosition.inChunkPosition, blockEntity)
    }


    fun setBlockEntities(blockEntities: Map<Vec3i, BlockEntity>) {
        for ((blockPosition, blockEntity) in blockEntities) {
            setBlockEntity(blockPosition, blockEntity)
        }
    }

    override fun getBiome(blockPosition: Vec3i): Biome? {
        val inChunkPosition = blockPosition.inChunkPosition
        val minY = dimension?.minY ?: 0
        if (inChunkPosition.y < minY) {
            inChunkPosition.y = minY
        }
        val maxY = dimension?.maxY ?: DimensionProperties.DEFAULT_MAX_Y
        if (inChunkPosition.y > maxY) {
            inChunkPosition.y = maxY
        }
        return this[blockPosition.chunkPosition]?.getBiome(inChunkPosition)
    }

    override fun getBiome(x: Int, y: Int, z: Int): Biome? {
        return this[Vec2i(x shr 4, z shr 4)]?.getBiome(x and 0x0F, y, z and 0x0F)
    }

    fun tick() {
        val simulationDistance = view.simulationDistance
        val cameraPosition = connection.player.positionInfo.chunkPosition
        chunks.lock.acquire()
        val latch = CountUpAndDownLatch(chunks.size)
        for ((chunkPosition, chunk) in chunks) {
            // ToDo: Cache (improve performance)
            if (!chunkPosition.isInViewDistance(simulationDistance, cameraPosition)) {
                latch.dec()
                continue
            }
            DefaultThreadPool += ThreadPoolRunnable(priority = ThreadPool.HIGH) { chunk.tick(connection, chunkPosition); latch.dec() }
        }
        chunks.lock.release()
        border.tick()
        latch.await()
    }

    fun randomTick() {
        for (i in 0 until 667) {
            randomTick(16)
            randomTick(32)
        }
    }

    private fun randomTick(radius: Int) {
        val blockPosition = connection.player.position.blockPosition + { random.nextInt(radius) } - { random.nextInt(radius) }

        val blockState = this[blockPosition] ?: return

        blockState.block.randomTick(connection, blockState, blockPosition, random)
    }

    operator fun get(aabb: AABB): Map<Vec3i, BlockState> {
        val ret: MutableMap<Vec3i, BlockState> = mutableMapOf()
        var run = 0
        var chunk: Chunk? = null
        var lastChunkPosition = Vec2i.EMPTY
        for (position in aabb.blockPositions) {
            val chunkPosition = position.chunkPosition
            if (chunkPosition != lastChunkPosition || run++ == 0) {
                chunk = this[chunkPosition]
                lastChunkPosition = chunkPosition
            }
            val state = chunk?.get(position.inChunkPosition) ?: continue
            ret[position] = state
        }
        return ret
    }

    fun isSpaceEmpty(aabb: AABB, checkFluids: Boolean = false): Boolean {
        for (position in aabb.blockPositions) {
            val blockState = this[position] ?: continue
            if ((blockState.collisionShape + position).intersect(aabb)) {
                return false
            }
            if (!checkFluids || blockState.block !is FluidBlock) {
                continue
            }
            val height = blockState.block.fluid.getHeight(blockState)
            if (position.y + height > aabb.min.y) {
                return false
            }
        }
        return true
    }

    fun getLight(blockPosition: Vec3i): Int {
        return get(blockPosition.chunkPosition)?.getLight(blockPosition.inChunkPosition) ?: 0x00
    }

    /**
     * @return All 8 neighbour chunks
     */
    fun getChunkNeighbours(neighbourPositions: Array<Vec2i>): Array<Chunk?> {
        val chunks: Array<Chunk?> = arrayOfNulls(neighbourPositions.size)
        for ((index, neighbourPosition) in neighbourPositions.withIndex()) {
            chunks[index] = this[neighbourPosition] ?: continue
        }
        return chunks
    }

    fun getChunkNeighbours(chunkPosition: Vec2i): Array<Chunk?> {
        return getChunkNeighbours(getChunkNeighbourPositions(chunkPosition))
    }

    fun onChunkUpdate(chunkPosition: Vec2i, chunk: Chunk, checkNeighbours: Boolean = true) {
        if (chunk.isFullyLoaded) {
            return
        }


        val neighboursPositions = getChunkNeighbourPositions(chunkPosition)
        val neighbours = getChunkNeighbours(neighboursPositions)

        if (neighbours.received) {
            chunk.neighboursLoaded = true

            if (!chunk.biomesInitialized && cacheBiomeAccessor != null && chunk.biomeSource != null && neighbours.canBuildBiomeCache) {
                chunk.buildBiomeCache()
            }
            connection.fireEvent(ChunkDataChangeEvent(connection, EventInitiators.UNKNOWN, chunkPosition, chunk))
        }


        if (!checkNeighbours) {
            return
        }

        for (index in 0 until 8) {
            val neighbour = neighbours[index] ?: continue
            onChunkUpdate(neighboursPositions[index], neighbour, false)
        }
    }

    fun getBrightness(position: Vec3i): Float {
        val light = getLight(position) and 0x0F
        return dimension?.lightLevels?.get(light) ?: 0.0f
    }

    companion object {
        const val MAX_SIZE = 29999999
        const val MAX_SIZEf = MAX_SIZE.toFloat()
        const val MAX_SIZEd = MAX_SIZE.toDouble()
        const val MAX_RENDER_DISTANCE = 64
        const val MAX_CHUNKS_SIZE = MAX_RENDER_DISTANCE * 2 + 1
    }
}
