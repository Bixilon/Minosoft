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

package de.bixilon.minosoft.gui.rendering.chunk.mesher

import de.bixilon.kmath.vec.vec3.f.MVec3f
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.direction.Directions.Companion.O_DOWN
import de.bixilon.minosoft.data.direction.Directions.Companion.O_EAST
import de.bixilon.minosoft.data.direction.Directions.Companion.O_NORTH
import de.bixilon.minosoft.data.direction.Directions.Companion.O_SOUTH
import de.bixilon.minosoft.data.direction.Directions.Companion.O_UP
import de.bixilon.minosoft.data.direction.Directions.Companion.O_WEST
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.BlockStateFlags
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.offset.OffsetBlock
import de.bixilon.minosoft.data.text.formatting.color.RGBArray
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.light.types.LightLevel
import de.bixilon.minosoft.data.world.chunk.light.types.LightLevel.Companion.MAX_LEVEL
import de.bixilon.minosoft.data.world.chunk.neighbours.ChunkNeighbours
import de.bixilon.minosoft.data.world.container.block.BlockSectionDataProvider
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.chunk.entities.BlockEntityRenderer
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshesBuilder
import de.bixilon.minosoft.gui.rendering.chunk.mesh.cache.ChunkMeshCache
import de.bixilon.minosoft.gui.rendering.chunk.mesh.details.ChunkMeshDetails
import de.bixilon.minosoft.gui.rendering.light.ao.AmbientOcclusion
import de.bixilon.minosoft.gui.rendering.models.block.state.render.WorldRenderProps
import java.util.*

class SolidSectionMesher(
    val context: RenderContext,
) {
    private val profile = context.session.profiles.block.rendering
    private val tints = context.tints
    private var ambientOcclusion = false

    init {
        val profile = context.session.profiles.rendering
        profile.light::ambientOcclusion.observe(this, true) { this.ambientOcclusion = it }
    }

    private fun areAllNeighboursFullOpaque(position: InSectionPosition, blocks: BlockSectionDataProvider, neighbours: Array<ChunkSection?>): Boolean {
        return (if (position.y > 0) blocks.fullOpaque[position.minusY().index] else (neighbours[Directions.O_DOWN]?.blocks?.fullOpaque?.get(position.with(y = ChunkSize.SECTION_MAX_Y).index) == true))
            && (if (position.y < ChunkSize.SECTION_MAX_Y) blocks.fullOpaque[position.plusY().index] else (neighbours[Directions.O_UP]?.blocks?.fullOpaque?.get(position.with(y = 0).index) == true))

            && (if (position.z > 0) blocks.fullOpaque[position.minusZ().index] else (neighbours[Directions.O_NORTH]?.blocks?.fullOpaque?.get(position.with(z = ChunkSize.SECTION_MAX_Z).index) == true))
            && (if (position.z < ChunkSize.SECTION_MAX_Z) blocks.fullOpaque[position.plusZ().index] else (neighbours[Directions.O_SOUTH]?.blocks?.fullOpaque?.get(position.with(z = 0).index) == true))

            && (if (position.x > 0) blocks.fullOpaque[position.minusX().index] else (neighbours[Directions.O_WEST]?.blocks?.fullOpaque?.get(position.with(x = ChunkSize.SECTION_MAX_X).index) == true))
            && (if (position.x < ChunkSize.SECTION_MAX_X) blocks.fullOpaque[position.plusX().index] else (neighbours[Directions.O_EAST]?.blocks?.fullOpaque?.get(position.with(x = 0).index) == true))
    }

    fun mesh(section: ChunkSection, cache: ChunkMeshCache, neighbourChunks: ChunkNeighbours, neighbours: Array<ChunkSection?>, mesh: ChunkMeshesBuilder) {
        val details = mesh.details
        val random = if (profile.antiMoirePattern && ChunkMeshDetails.ANTI_MOIRE_PATTERN in details) Random(0L) else null

        val chunk = section.chunk


        val isLowestSection = section.height == chunk.world.dimension.minSection
        val isHighestSection = section.height == chunk.world.dimension.maxSection
        val blocks = section.blocks
        val entities: ArrayList<BlockEntityRenderer> = if (ChunkMeshDetails.ENTITIES in details) ArrayList(section.entities.count) else EMPTY_ARRAY_LIST

        val tint = RGBArray(1)
        val neighbourBlocks: Array<BlockState?> = arrayOfNulls(Directions.SIZE)
        val light = ByteArray(Directions.SIZE + 1) // last index (6) for the current block

        val cameraOffset = context.camera.offset.offset

        val offset = BlockPosition.of(chunk.position, section.height)

        val floatOffset = MVec3f()

        val ao = if (ambientOcclusion && ChunkMeshDetails.AMBIENT_OCCLUSION in details) AmbientOcclusion(section) else null

        val props = WorldRenderProps(floatOffset.unsafe, mesh, random, neighbourBlocks, light, details, ao) // TODO: really use unsafe?

        val min = blocks.minPosition
        val max = blocks.maxPosition

        for (y in min.y..max.y) {
            for (x in min.x..max.x) {
                for (z in min.z..max.z) {
                    val inSection = InSectionPosition(x, y, z)
                    val state = blocks[inSection] ?: continue
                    if (state.block is FluidBlock) continue // fluids are rendered in a different renderer

                    if (ChunkMeshDetails.NON_FULL_BLOCKS !in details && BlockStateFlags.FULL_OUTLINE !in state.flags) continue
                    if (ChunkMeshDetails.MINOR_VISUAL_IMPACT !in details && BlockStateFlags.MINOR_VISUAL_IMPACT in state.flags) continue
                    if (ChunkMeshDetails.CULL_FULL_OPAQUE in details && areAllNeighboursFullOpaque(inSection, blocks, neighbours)) continue

                    val model = state.block.model ?: state.model
                    val entity = if (ChunkMeshDetails.ENTITIES in details) section.entities[inSection] else null
                    val entityRenderer = entity?.let { cache.createEntity(inSection, entity) }
                    if (model == null && entityRenderer == null) continue

                    val position = offset + inSection
                    val inChunk = InChunkPosition(inSection.x, position.y, inSection.z)
                    floatOffset.x = (position.x - cameraOffset.x).toFloat()
                    floatOffset.y = (position.y - cameraOffset.y).toFloat()
                    floatOffset.z = (position.z - cameraOffset.z).toFloat()


                    if (ChunkMeshDetails.SIDE_DOWN in details) setDown(inSection, isLowestSection, neighbourBlocks, neighbours, light, section, chunk)
                    if (ChunkMeshDetails.SIDE_UP in details) setUp(isHighestSection, inSection, neighbourBlocks, neighbours, light, section, chunk)

                    // TODO: mesh details (sides)
                    setZ(neighbourBlocks, inChunk, neighbours, light, neighbourChunks, section, chunk)
                    setX(neighbourBlocks, inChunk, neighbours, light, neighbourChunks, section, chunk)


                    val maxHeight = chunk.light.heightmap[inSection.xz]
                    light[SELF_LIGHT_INDEX] = section.light[inSection].raw
                    if (position.y + 1 >= maxHeight) {
                        light[O_UP] = LightLevel(light[O_UP]).with(sky = MAX_LEVEL).raw
                    }
                    if (position.y + 0 >= maxHeight) {
                        light[SELF_LIGHT_INDEX] = LightLevel(light[SELF_LIGHT_INDEX]).with(sky = MAX_LEVEL).raw
                    }
                    if (position.y - 1 >= maxHeight) {
                        light[O_DOWN] = LightLevel(light[O_DOWN]).with(sky = MAX_LEVEL).raw
                    }

                    if (ChunkMeshDetails.RANDOM_OFFSET in details && BlockStateFlags.OFFSET in state.flags && state.block is OffsetBlock) {
                        val randomOffset = state.block.getModelOffset(position)
                        floatOffset.x += randomOffset.x
                        floatOffset.y += randomOffset.y
                        floatOffset.z += randomOffset.z
                    }

                    ao?.clear()


                    val tints = tints.getBlockTint(state, chunk, InChunkPosition(x, position.y, z), tint)
                    var rendered = false
                    model?.render(props, position, state, entity, tints)?.let { if (it) rendered = true }

                    if (entityRenderer != null) {
                        entityRenderer.update(LightLevel(light[SELF_LIGHT_INDEX]))
                        entities += entityRenderer
                        rendered = true
                    }

                    if (rendered) {
                        mesh.addBlock(x, y, z)
                    }
                    if (Thread.interrupted()) throw InterruptedException()
                }
            }
        }
        mesh.entities = entities
    }

    private inline fun setDown(position: InSectionPosition, lowest: Boolean, neighbourBlocks: Array<BlockState?>, neighbours: Array<ChunkSection?>, light: ByteArray, section: ChunkSection, chunk: Chunk) {
        if (position.y == 0) {
            neighbourBlocks[O_DOWN] = neighbours[O_DOWN]?.blocks?.let { it[position.with(y = ChunkSize.SECTION_MAX_Y)] }
            light[O_DOWN] = (if (lowest) chunk.light.bottom else neighbours[O_DOWN]?.light)?.get(position.with(y = ChunkSize.SECTION_MAX_Y))?.raw ?: 0x00
        } else {
            neighbourBlocks[O_DOWN] = section.blocks[position.minusY()]
            light[O_DOWN] = section.light[position.minusY()].raw
        }
    }

    fun setUp(highest: Boolean, position: InSectionPosition, neighbourBlocks: Array<BlockState?>, neighbours: Array<ChunkSection?>, light: ByteArray, section: ChunkSection, chunk: Chunk) {
        if (position.y == ChunkSize.SECTION_MAX_Y) {
            neighbourBlocks[O_UP] = neighbours[O_UP]?.blocks?.let { it[position.with(y = 0)] }
            light[O_UP] = (if (highest) chunk.light.top else neighbours[O_UP]?.light)?.get(position.with(y = 0))?.raw ?: 0x00
        } else {
            neighbourBlocks[O_UP] = section.blocks[position.plusY()]
            light[O_UP] = section.light[position.plusY()].raw
        }
    }

    private inline fun setZ(neighbourBlocks: Array<BlockState?>, position: InChunkPosition, neighbours: Array<ChunkSection?>, light: ByteArray, neighbourChunks: ChunkNeighbours, section: ChunkSection, chunk: Chunk) = when (position.z) {
        0 -> {
            setNeighbour(neighbourBlocks, position.with(z = ChunkSize.SECTION_MAX_Z), light, neighbours[O_NORTH], neighbourChunks[Directions.NORTH], O_NORTH)
            setNeighbour(neighbourBlocks, position.plusZ(), light, section, chunk, O_SOUTH)
        }

        ChunkSize.SECTION_MAX_Z -> {
            setNeighbour(neighbourBlocks, position.minusZ(), light, section, chunk, O_NORTH)
            setNeighbour(neighbourBlocks, position.with(z = 0), light, neighbours[O_SOUTH], neighbourChunks[Directions.SOUTH], O_SOUTH)
        }

        else -> {
            setNeighbour(neighbourBlocks, position.minusZ(), light, section, chunk, O_NORTH)
            setNeighbour(neighbourBlocks, position.plusZ(), light, section, chunk, O_SOUTH)
        }
    }


    private inline fun setX(neighbourBlocks: Array<BlockState?>, position: InChunkPosition, neighbours: Array<ChunkSection?>, light: ByteArray, neighbourChunks: ChunkNeighbours, section: ChunkSection, chunk: Chunk) = when (position.x) {
        0 -> {
            setNeighbour(neighbourBlocks, position.with(x = ChunkSize.SECTION_MAX_X), light, neighbours[O_WEST], neighbourChunks[Directions.WEST], O_WEST)
            setNeighbour(neighbourBlocks, position.plusX(), light, section, chunk, O_EAST)
        }

        ChunkSize.SECTION_MAX_X -> {
            setNeighbour(neighbourBlocks, position.with(x = 0), light, neighbours[O_EAST], neighbourChunks[Directions.EAST], O_EAST)
            setNeighbour(neighbourBlocks, position.minusX(), light, section, chunk, O_WEST)
        }

        else -> {
            setNeighbour(neighbourBlocks, position.minusX(), light, section, chunk, O_WEST)
            setNeighbour(neighbourBlocks, position.plusX(), light, section, chunk, O_EAST)
        }
    }

    private inline fun setNeighbour(blocks: Array<BlockState?>, position: InChunkPosition, light: ByteArray, section: ChunkSection?, chunk: Chunk?, direction: Int) {
        val inSection = position.inSectionPosition
        blocks[direction] = section?.blocks?.let { it[inSection] }

        var level = section?.light?.get(inSection)?.raw ?: 0x00
        if (chunk != null && position.y >= chunk.light.heightmap[position.xz]) {
            level = LightLevel(level).with(sky = MAX_LEVEL).raw
        }
        light[direction] = level
    }

    companion object {
        const val SELF_LIGHT_INDEX = Directions.SIZE // after all directions
        val EMPTY_ARRAY_LIST = ArrayList<BlockEntityRenderer>(0)
    }
}
