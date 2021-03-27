package de.bixilon.minosoft.gui.rendering.chunk.models.renderable

import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.data.mappings.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.data.world.BlockPosition
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.light.LightAccessor
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.chunk.ChunkMeshCollection
import de.bixilon.minosoft.gui.rendering.chunk.models.FaceSize
import de.bixilon.minosoft.gui.rendering.chunk.models.loading.BlockModelElement
import de.bixilon.minosoft.gui.rendering.chunk.models.loading.BlockModelFace
import de.bixilon.minosoft.gui.rendering.textures.Texture
import de.bixilon.minosoft.gui.rendering.util.VecUtil
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

class FluidRenderer(
    private val stillTextureName: String,
    private val flowingTextureName: String,
    private val regex: String,
) : BlockRenderInterface {
    override val faceBorderSizes: Array<Array<FaceSize>?> = arrayOfNulls(Directions.DIRECTIONS.size)
    override val transparentFaces: BooleanArray = BooleanArray(Directions.DIRECTIONS.size)
    private lateinit var stillTexture: Texture
    private lateinit var flowingTexture: Texture

    override fun render(blockState: BlockState, lightAccessor: LightAccessor, tintColor: RGBColor?, position: BlockPosition, meshCollection: ChunkMeshCollection, neighbourBlocks: Array<BlockState?>, world: World) {
        if (!RenderConstants.RENDER_FLUIDS) {
            return
        }

        val modelMatrix = Mat4().translate(position.toVec3())
        val lightLevel = lightAccessor.getLightLevel(position)
        val heights = calculateHeights(neighbourBlocks, blockState, world, position)
        val isFlowing = isLiquidFlowing(heights)

        val texture: Texture
        val angle: Float
        if (isFlowing) {
            texture = flowingTexture
            angle = getRotationAngle(heights)
        } else {
            texture = stillTexture
            angle = 0.0f
        }

        val positions = calculatePositions(heights)
        for (direction in Directions.DIRECTIONS) {
            if (isBlockSameFluid(neighbourBlocks[direction.ordinal]) || neighbourBlocks[direction.ordinal]?.getBlockRenderer(position + direction)?.faceBorderSizes?.let { it[direction.inverse.ordinal] != null } == true && direction != Directions.UP) {
                continue
            }
            val face = BlockModelFace(VecUtil.EMPTY_VECTOR, Vec3(VecUtil.BLOCK_SIZE_VECTOR.x, positions[7].y * 8, VecUtil.BLOCK_SIZE_VECTOR.z), direction)
            if (isFlowing) {
                face.scale(0.5)
            }
            face.rotate(angle)
            val positionTemplate = BlockModelElement.FACE_POSITION_MAP_TEMPLATE[direction.ordinal]
            val drawPositions = arrayOf(positions[positionTemplate[0]], positions[positionTemplate[1]], positions[positionTemplate[2]], positions[positionTemplate[3]])
            createQuad(drawPositions, face.getTexturePositionArray(direction), texture, modelMatrix, meshCollection, tintColor, lightLevel)
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

    private fun createQuad(drawPositions: Array<Vec3>, texturePositions: Array<Vec2?>, texture: Texture, modelMatrix: Mat4, meshCollection: ChunkMeshCollection, tintColor: RGBColor?, lightLevel: Int) {
        val mesh = ElementRenderer.getMesh(meshCollection, texture.transparency)
        for (vertex in ElementRenderer.DRAW_ODER) {
            val input = Vec4(drawPositions[vertex.first], 1.0f)
            val output = modelMatrix * input
            mesh.addVertex(
                position = output.toVec3(),
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

    private fun calculateHeights(neighbourBlocks: Array<BlockState?>, blockState: BlockState, world: World, position: BlockPosition): FloatArray {
        val height = getLevel(blockState)
        val heights = floatArrayOf(height, height, height, height)
        for (direction in Directions.SIDES) {
            val positions = getPositionsForDirection(direction)
            handleUpperBlocks(world, position, direction, positions, heights)
            handleDirectNeighbours(neighbourBlocks, direction, world, position, positions, heights)
        }
        return heights
    }

    private fun handleDirectNeighbours(neighbourBlocks: Array<BlockState?>, direction: Directions, world: World, position: BlockPosition, positions: MutableSet<Int>, heights: FloatArray) {
        if (isBlockSameFluid(neighbourBlocks[direction.ordinal])) {
            val neighbourLevel = getLevel(neighbourBlocks[direction.ordinal]!!)
            for (heightPosition in positions) {
                heights[heightPosition] = glm.max(heights[heightPosition], neighbourLevel)
            }
        }
        for (altDirection in direction.sidesNextTo(direction)) {
            val bothDirections = setOf(direction, altDirection)
            if (isBlockSameFluid(world.getBlockState(position + direction + altDirection))) {
                val neighbourLevel = getLevel(world.getBlockState(position + direction + altDirection)!!)
                for (heightPosition in HEIGHT_POSITIONS) {
                    if (heightPosition.key.containsAll(bothDirections)) {
                        heights[heightPosition.value] = glm.max(heights[heightPosition.value], neighbourLevel)
                    }
                }
            }
        }
    }

    private fun handleUpperBlocks(world: World, position: BlockPosition, direction: Directions, positions: MutableSet<Int>, heights: FloatArray) {
        if (isBlockSameFluid(world.getBlockState(position + Directions.UP + direction))) {
            for (heightPosition in positions) {
                heights[heightPosition] = 1.0f
            }
        }
        for (altDirection in direction.sidesNextTo(direction)) {
            val bothDirections = setOf(direction, altDirection)
            if (isBlockSameFluid(world.getBlockState(position + Directions.UP + direction + altDirection))) {
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
            return (8 - it as Int) * (1f / 8f) - 0.125f
        }
        return 0.8125f
    }

    private fun isBlockSameFluid(blockState: BlockState?): Boolean {
        if (blockState == null) {
            return false
        }
        if (blockState.owner.resourceLocation.full.contains(regex)) {
            return true
        }
        if (blockState.properties[BlockProperties.WATERLOGGED] == true) {
            return true
        }
        return false
    }

    override fun resolveTextures(indexed: MutableList<Texture>, textureMap: MutableMap<String, Texture>) {
        stillTexture = BlockRenderInterface.resolveTexture(indexed, textureMap, stillTextureName)!!
        flowingTexture = BlockRenderInterface.resolveTexture(indexed, textureMap, flowingTextureName)!!
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
