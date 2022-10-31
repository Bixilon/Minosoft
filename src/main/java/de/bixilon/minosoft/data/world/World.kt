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
import de.bixilon.kutil.collections.map.LockMap
import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.kutil.concurrent.worker.unconditional.UnconditionalTask
import de.bixilon.kutil.concurrent.worker.unconditional.UnconditionalWorker
import de.bixilon.kutil.watcher.DataWatcher.Companion.watched
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.FluidBlock
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.registries.shapes.AABB
import de.bixilon.minosoft.data.world.audio.AbstractAudioPlayer
import de.bixilon.minosoft.data.world.audio.WorldAudioPlayer
import de.bixilon.minosoft.data.world.biome.accessor.BiomeAccessor
import de.bixilon.minosoft.data.world.biome.accessor.NoiseBiomeAccessor
import de.bixilon.minosoft.data.world.border.WorldBorder
import de.bixilon.minosoft.data.world.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.light.SectionLight
import de.bixilon.minosoft.data.world.chunk.neighbours.ChunkNeighbours
import de.bixilon.minosoft.data.world.difficulty.WorldDifficulty
import de.bixilon.minosoft.data.world.particle.AbstractParticleRenderer
import de.bixilon.minosoft.data.world.particle.WorldParticleRenderer
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.time.WorldTime
import de.bixilon.minosoft.data.world.view.WorldView
import de.bixilon.minosoft.data.world.weather.WorldWeather
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.blockPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.chunkPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.inChunkPosition
import de.bixilon.minosoft.modding.event.events.blocks.BlockSetEvent
import de.bixilon.minosoft.modding.event.events.blocks.chunk.ChunkDataChangeEvent
import de.bixilon.minosoft.modding.event.events.blocks.chunk.ChunkUnloadEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.chunk.ChunkUtil.canBuildBiomeCache
import de.bixilon.minosoft.util.chunk.ChunkUtil.getChunkNeighbourPositions
import de.bixilon.minosoft.util.chunk.ChunkUtil.isInViewDistance
import kotlin.random.Random

/**
 * Collection of chunks and more
 */
class World(
    val connection: PlayConnection,
) : BiomeAccessor, WorldAudioPlayer, WorldParticleRenderer {
    val lock = SimpleLock()
    var cacheBiomeAccessor: NoiseBiomeAccessor? = null
    val chunks: LockMap<Vec2i, Chunk> = LockMap(mutableMapOf(), lock)
    val entities = WorldEntities()
    var hardcore by watched(false)
    var dimension: DimensionProperties? by watched(null)
    var difficulty: WorldDifficulty? by watched(null)
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

    operator fun get(blockPosition: BlockPosition): BlockState? {
        return chunks[blockPosition.chunkPosition]?.get(blockPosition.inChunkPosition)
    }

    operator fun get(chunkPosition: ChunkPosition): Chunk? {
        return chunks[chunkPosition]
    }

    @Synchronized
    private fun updateChunkExtreme(chunkPosition: ChunkPosition, mass: Boolean = false) {
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

    @Synchronized
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

        for (chunkPosition in chunks.unsafe.keys) {
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

    fun getOrCreateChunk(chunkPosition: ChunkPosition): Chunk {
        updateChunkExtreme(chunkPosition)
        return chunks.synchronizedGetOrPut(chunkPosition) { Chunk(connection, chunkPosition) }
    }

    private fun _set(chunk: Chunk, blockPosition: BlockPosition, blockState: BlockState?) {
        val inChunkPosition = blockPosition.inChunkPosition
        val previousBlock = chunk[inChunkPosition]
        if (previousBlock == blockState) {
            return
        }
        previousBlock?.block?.onBreak(connection, blockPosition, previousBlock, chunk.getBlockEntity(inChunkPosition))
        blockState?.block?.onPlace(connection, blockPosition, blockState)
        chunk[inChunkPosition] = blockState
        chunk.getOrPutBlockEntity(inChunkPosition)
        connection.fire(
            BlockSetEvent(
                connection = connection,
                blockPosition = blockPosition,
                blockState = blockState,
            )
        )
    }

    fun setBlockState(blockPosition: BlockPosition, blockState: BlockState?) {
        this[blockPosition] = blockState
    }

    operator fun set(blockPosition: BlockPosition, blockState: BlockState?) {
        val chunk = chunks[blockPosition.chunkPosition] ?: return
        _set(chunk, blockPosition, blockState)
    }

    fun forceSet(blockPosition: BlockPosition, blockState: BlockState?) {
        val chunk = getOrCreateChunk(blockPosition.chunkPosition)
        _set(chunk, blockPosition, blockState)
    }

    fun isPositionChangeable(blockPosition: BlockPosition): Boolean {
        if (border.isOutside(blockPosition)) {
            return false
        }
        return isValidPosition(blockPosition)
    }

    fun isValidPosition(blockPosition: BlockPosition): Boolean {
        val dimension = connection.world.dimension!!
        return (blockPosition.y >= dimension.minY && blockPosition.y <= dimension.maxY)
    }

    fun forceSetBlockState(blockPosition: BlockPosition, blockState: BlockState?, check: Boolean = false) {
        if (check && !isPositionChangeable(blockPosition)) {
            return
        }
        chunks[blockPosition.chunkPosition]?.set(blockPosition.inChunkPosition, blockState)
    }

    fun unloadChunk(chunkPosition: ChunkPosition) {
        val chunk = chunks.remove(chunkPosition) ?: return
        for ((index, neighbour) in chunk.neighbours.neighbours.withIndex()) {
            if (neighbour == null) {
                continue
            }
            val offset = ChunkNeighbours.OFFSETS[index]
            val neighbourPosition = chunkPosition + offset
            neighbour.neighbours.remove(-offset)
            connection.fire(ChunkDataChangeEvent(connection, neighbourPosition, neighbour))
        }
        // connection.world.view.updateServerViewDistance(chunkPosition, false)
        connection.fire(ChunkUnloadEvent(connection, chunkPosition, chunk))
        if (chunkPosition.x <= chunkMin.x || chunkPosition.y <= chunkMin.y || chunkPosition.x >= chunkMax.x || chunkPosition.y >= chunkMax.y) {
            recalculateChunkExtreme()
        }
        occlusionUpdateCallback?.onOcclusionChange()
    }

    fun getBlockEntity(blockPosition: BlockPosition): BlockEntity? {
        return get(blockPosition.chunkPosition)?.getBlockEntity(blockPosition.inChunkPosition)
    }

    fun getOrPutBlockEntity(blockPosition: BlockPosition): BlockEntity? {
        return get(blockPosition.chunkPosition)?.getOrPutBlockEntity(blockPosition.inChunkPosition)
    }

    fun setBlockEntity(blockPosition: BlockPosition, blockEntity: BlockEntity?) {
        get(blockPosition.chunkPosition)?.setBlockEntity(blockPosition.inChunkPosition, blockEntity)
    }


    fun setBlockEntities(blockEntities: Map<BlockPosition, BlockEntity>) {
        for ((blockPosition, blockEntity) in blockEntities) {
            setBlockEntity(blockPosition, blockEntity)
        }
    }

    override fun getBiome(blockPosition: BlockPosition): Biome? {
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
        val worker = UnconditionalWorker()
        for ((chunkPosition, chunk) in chunks.unsafe) {
            // ToDo: Cache (improve performance)
            if (!chunkPosition.isInViewDistance(simulationDistance, cameraPosition)) {
                continue
            }
            worker += UnconditionalTask(priority = ThreadPool.HIGH) { chunk.tick(connection, chunkPosition) }
        }
        chunks.lock.release()
        worker.work()
        border.tick()
    }

    fun randomTick() {
        val blockPosition = connection.player.position.blockPosition
        val chunk = this[blockPosition.chunkPosition] ?: return


        // ToDo: Split that up in multiple threads?
        for (i in 0 until 667) {
            randomTick(16, blockPosition, chunk)
            randomTick(32, blockPosition, chunk)
        }
    }

    private fun randomTick(radius: Int, origin: BlockPosition, chunk: Chunk) {
        val offset = Vec3i.EMPTY + { random.nextInt(-radius, radius) }
        val blockPosition = origin + offset

        val blockState = chunk.traceBlock(offset, origin, blockPosition) ?: return

        blockState.block.randomTick(connection, blockState, blockPosition, random)
    }

    operator fun get(aabb: AABB): Map<BlockPosition, BlockState> {
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

    fun getLight(blockPosition: BlockPosition): Int {
        return get(blockPosition.chunkPosition)?.light?.get(blockPosition.inChunkPosition) ?: 0x00
    }

    /**
     * @return All 8 neighbour chunks
     */
    fun getChunkNeighbours(neighbourPositions: Array<ChunkPosition>): Array<Chunk?> {
        val chunks: Array<Chunk?> = arrayOfNulls(neighbourPositions.size)
        this.chunks.lock.acquire()
        for ((index, neighbourPosition) in neighbourPositions.withIndex()) {
            chunks[index] = this.chunks.unsafe[neighbourPosition] ?: continue
        }
        this.chunks.lock.release()
        return chunks
    }

    fun getChunkNeighbours(chunkPosition: ChunkPosition): Array<Chunk?> {
        return getChunkNeighbours(getChunkNeighbourPositions(chunkPosition))
    }

    private fun calculateChunkData(chunk: Chunk) {
        val neighbours = chunk.neighbours.get() ?: return
        if (!chunk.biomesInitialized && cacheBiomeAccessor != null && chunk.biomeSource != null && neighbours.canBuildBiomeCache) {
            chunk.buildBiomeCache()
        }

        chunk.light.recalculate(false)
        chunk.light.propagateFromNeighbours()
        connection.fire(ChunkDataChangeEvent(connection, chunk.chunkPosition, chunk))
    }

    fun onChunkUpdate(chunkPosition: ChunkPosition, chunk: Chunk, checkNeighbours: Boolean = true) {
        if (chunk.isFullyLoaded) {
            // everything already calculated
            return
        }

        for ((index, neighbour) in chunk.neighbours.neighbours.withIndex()) {
            if (neighbour != null) {
                continue
            }
            val offset = ChunkNeighbours.OFFSETS[index]
            val neighbour = this[chunkPosition + offset] ?: continue
            chunk.neighbours[index] = neighbour
            neighbour.neighbours[-offset] = chunk
        }

        if (chunk.neighbours.complete) {
            calculateChunkData(chunk)
        }

        if (checkNeighbours) {
            for (index in 0 until 8) {
                val neighbour = chunk.neighbours[index] ?: continue
                calculateChunkData(neighbour)
            }
        }

        if (chunk.neighbours.complete) {
            connection.fire(ChunkDataChangeEvent(connection, chunkPosition, chunk))
        }
    }

    fun getBrightness(position: BlockPosition): Float {
        val light = getLight(position)
        val level = maxOf(light and SectionLight.BLOCK_LIGHT_MASK, light and SectionLight.SKY_LIGHT_MASK shr 4)
        return dimension?.lightLevels?.get(level) ?: 0.0f
    }

    fun recalculateLight() {
        val reset = UnconditionalWorker()
        val calculate = UnconditionalWorker()
        lock.acquire()
        for (chunk in chunks.unsafe.values) {
            reset += { chunk.light.reset() }
            calculate += { chunk.light.calculate() }
        }
        lock.release()
        reset.work()
        calculate.work()
    }

    companion object {
        const val MAX_SIZE = 29999999
        const val MAX_SIZEf = MAX_SIZE.toFloat()
        const val MAX_SIZEd = MAX_SIZE.toDouble()
        const val MAX_RENDER_DISTANCE = 64
        const val MAX_CHUNKS_SIZE = MAX_RENDER_DISTANCE * 2 + 1
    }
}
