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

package de.bixilon.minosoft.gui.rendering.block.renderable.fluid

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.fluid.FlowableFluid
import de.bixilon.minosoft.data.registries.fluid.Fluid
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.block.mesh.ChunkSectionMeshCollection
import de.bixilon.minosoft.gui.rendering.block.models.BlockModelElement
import de.bixilon.minosoft.gui.rendering.block.models.BlockModelFace
import de.bixilon.minosoft.gui.rendering.block.models.FaceSize
import de.bixilon.minosoft.gui.rendering.block.renderable.BlockLikeRenderContext
import de.bixilon.minosoft.gui.rendering.block.renderable.WorldEntryRenderer
import de.bixilon.minosoft.gui.rendering.block.renderable.block.ElementRenderer
import de.bixilon.minosoft.gui.rendering.textures.Texture
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.util.KUtil.nullCast
import glm_.glm
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec3.Vec3i

class FluidRenderer(
    val block: Block,
    private val fluid: Fluid,
) : WorldEntryRenderer {
    override val faceBorderSizes: Array<Array<FaceSize>?> = arrayOfNulls(Directions.VALUES.size)
    override val transparentFaces: BooleanArray = BooleanArray(Directions.VALUES.size)
    var stillTexture: Texture? = null
        private set
    var flowingTexture: Texture? = null
        private set

    override fun render(context: BlockLikeRenderContext) {
        if (!RenderConstants.RENDER_FLUIDS) {
            return
        }
        val lightLevel = context.lightAccessor.getLightLevel(context.blockPosition)
        val heights = calculateHeights(context.neighbourBlocks, context.blockState, context.world, context.blockPosition)
        val isFlowing = isLiquidFlowing(heights)

        var texture: Texture

        var tintColor: RGBColor? = null
        var biome: Biome? = null

        val positions = calculatePositions(heights)
        for (direction in Directions.VALUES) {
            var face = BlockModelFace(positions, direction)
            if (isFlowing || Directions.SIDES.contains(direction)) {
                face = face.scale(0.5)
                texture = flowingTexture ?: return
                if (!Directions.SIDES.contains(direction)) {
                    val angle = getRotationAngle(heights)
                    face = face.rotate(angle)
                }
            } else {
                texture = stillTexture ?: return
            }
            val neighbourBlocks = context.neighbourBlocks
            if (fluid.matches(neighbourBlocks[direction.ordinal]) || neighbourBlocks[direction.ordinal]?.getBlockRenderer(context.blockPosition + direction)?.faceBorderSizes?.let { it[direction.inverted.ordinal] != null } == true && direction != Directions.UP) {
                continue
            }
            val positionTemplate = BlockModelElement.FACE_POSITION_MAP_TEMPLATE[direction.ordinal]
            val drawPositions = arrayOf(positions[positionTemplate[0]], positions[positionTemplate[1]], positions[positionTemplate[2]], positions[positionTemplate[3]])

            if (biome == null) {
                biome = context.world.getBiome(context.blockPosition)
                tintColor = context.renderWindow.tintColorCalculator.getAverageTint(biome, context.blockState, context.blockPosition)
            }

            createQuad(drawPositions, face.getTexturePositionArray(direction), texture, context.blockPosition, context.meshCollection, tintColor, lightLevel)
        }
    }

    private fun getRotationAngle(heights: FloatArray): Float {
        val maxHeight = heights.maxOrNull()
        for (direction in Directions.SIDES) {
            val positions = getPositionsForDirection(direction)
            val currentHeights = mutableListOf<Float>()
            for (position in positions) {
                currentHeights.add(heights[position])
            }
            val allCurrentHeightsAreEqual = currentHeights.toSet().size == 1
            if (allCurrentHeightsAreEqual) {
                if (maxHeight == currentHeights[0]) {
                    return getRotationAngle(direction)
                }
            }
        }
        val minHeight = heights.minOrNull()
        val position = heights.indexOfFirst { it == minHeight }
        val directions = HEIGHT_POSITIONS_REVERSED[position]!!
        var angle = 0.0f
        for (direction in directions) {
            angle += getRotationAngle(direction)
        }
        return if (position == 1) {
            angle / directions.size
        } else {
            angle / directions.size + glm.PIf
        }
    }

    private fun getRotationAngle(direction: Directions): Float {
        return when (direction) {
            Directions.SOUTH -> glm.PIf
            Directions.NORTH -> 0.0f
            Directions.WEST -> glm.PIf * 0.5f
            Directions.EAST -> glm.PIf * 1.5f
            else -> error("Unexpected value: $direction")
        }
    }

    private fun isLiquidFlowing(heights: FloatArray): Boolean {
        return heights.toSet().size != 1 // liquid is flowing, if not all of the heights are the same
    }

    private fun createQuad(drawPositions: Array<Vec3>, texturePositions: Array<Vec2?>, texture: Texture, blockPosition: Vec3i, meshCollection: ChunkSectionMeshCollection, tintColor: RGBColor?, lightLevel: Int) {
        val mesh = ElementRenderer.getMesh(meshCollection, texture.transparency)
        for (vertex in ElementRenderer.DRAW_ODER) {
            mesh.addVertex(
                position = blockPosition plus drawPositions[vertex.first] plus ElementRenderer.DRAW_OFFSET,
                textureCoordinates = texturePositions[vertex.second]!!,
                texture = texture,
                tintColor = tintColor,
                lightLevel = lightLevel,
            )
        }
    }

    private fun calculatePositions(heights: FloatArray): List<Vec3> {
        val positions = mutableListOf<Vec3>()
        positions.addAll(DEFAULT_POSITIONS)
        for ((i, defaultPosition) in DEFAULT_POSITIONS.withIndex()) {
            val position = Vec3(defaultPosition)
            position.y += heights[i]
            positions.add(position)
        }
        return positions
    }

    private fun calculateHeights(neighbourBlocks: Array<BlockState?>, blockState: BlockState, world: World, position: Vec3i): FloatArray {
        if (fluid.matches(neighbourBlocks[Directions.UP.ordinal])) {
            return floatArrayOf(1f, 1f, 1f, 1f)
        }
        val height = getLevel(blockState)
        val heights = floatArrayOf(height, height, height, height)
        for (direction in Directions.SIDES) {
            val positions = getPositionsForDirection(direction)
            handleUpperBlocks(world, position, direction, positions, heights)
            handleDirectNeighbours(neighbourBlocks, direction, world, position, positions, heights)
        }
        return heights
    }

    private fun handleDirectNeighbours(neighbourBlocks: Array<BlockState?>, direction: Directions, world: World, position: Vec3i, positions: MutableSet<Int>, heights: FloatArray) {
        if (fluid.matches(neighbourBlocks[direction.ordinal])) {
            val neighbourLevel = getLevel(neighbourBlocks[direction.ordinal]!!)
            for (heightPosition in positions) {
                heights[heightPosition] = glm.max(heights[heightPosition], neighbourLevel)
            }
        }
        for (altDirection in direction.sidesNextTo(direction)) {
            val bothDirections = setOf(direction, altDirection)
            if (fluid.matches(world[position + direction + altDirection])) {
                val neighbourLevel = getLevel(world[position + direction + altDirection]!!)
                for (heightPosition in HEIGHT_POSITIONS) {
                    if (heightPosition.key.containsAll(bothDirections)) {
                        heights[heightPosition.value] = glm.max(heights[heightPosition.value], neighbourLevel)
                    }
                }
            }
        }
    }

    private fun handleUpperBlocks(world: World, position: Vec3i, direction: Directions, positions: MutableSet<Int>, heights: FloatArray) {
        if (fluid.matches(world[position + Directions.UP + direction])) {
            for (heightPosition in positions) {
                heights[heightPosition] = 1.0f
            }
        }
        for (altDirection in direction.sidesNextTo(direction)) {
            val bothDirections = setOf(direction, altDirection)
            if (fluid.matches(world[position + Directions.UP + direction + altDirection])) {
                for (heightPosition in HEIGHT_POSITIONS) {
                    if (heightPosition.key.containsAll(bothDirections)) {
                        heights[heightPosition.value] = 1.0f
                    }
                }
            }
        }
    }

    private fun getPositionsForDirection(direction: Directions): MutableSet<Int> {
        val positions = mutableSetOf<Int>()
        for (heightPosition in HEIGHT_POSITIONS) {
            if (heightPosition.key.contains(direction)) {
                positions.add(heightPosition.value)
            }
        }
        return positions
    }

    private fun getLevel(blockState: BlockState): Float {
        blockState.properties[BlockProperties.FLUID_LEVEL]?.let {
            return 0.9f - (it as Int / 8f)
        }
        return 0.8125f
    }

    override fun resolveTextures(textures: MutableMap<ResourceLocation, Texture>) {
        stillTexture = fluid.stillTexture?.let { Texture.getResourceTextureIdentifier(it.namespace, it.path) }?.let { WorldEntryRenderer.resolveTexture(textures, it) }
        flowingTexture = fluid.nullCast<FlowableFluid>()?.flowingTexture?.let { Texture.getResourceTextureIdentifier(it.namespace, it.path) }?.let { WorldEntryRenderer.resolveTexture(textures, it) }
    }

    companion object {
        val DEFAULT_POSITIONS = arrayOf(
            ElementRenderer.POSITION_1,
            ElementRenderer.POSITION_2,
            ElementRenderer.POSITION_3,
            ElementRenderer.POSITION_4
        )

        val HEIGHT_POSITIONS = mapOf(
            setOf(Directions.NORTH, Directions.WEST) to 0,
            setOf(Directions.NORTH, Directions.EAST) to 1,
            setOf(Directions.SOUTH, Directions.WEST) to 2,
            setOf(Directions.SOUTH, Directions.EAST) to 3,
        )

        val HEIGHT_POSITIONS_REVERSED = HEIGHT_POSITIONS.entries.associate { (k, v) -> v to k }
    }
}
