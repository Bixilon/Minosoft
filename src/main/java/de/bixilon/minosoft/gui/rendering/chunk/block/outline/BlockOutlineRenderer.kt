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

package de.bixilon.minosoft.gui.rendering.chunk.block.outline

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.Gamemodes
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.RendererBuilder
import de.bixilon.minosoft.gui.rendering.chunk.VoxelShape
import de.bixilon.minosoft.gui.rendering.chunk.models.renderable.ElementRenderer
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.util.VecUtil
import de.bixilon.minosoft.gui.rendering.util.VecUtil.getWorldOffset
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.BitByte.isBit
import de.bixilon.minosoft.util.MMath.positiveNegative
import glm_.vec3.Vec3
import glm_.vec3.Vec3i
import org.lwjgl.opengl.GL11.*

class BlockOutlineRenderer(
    val connection: PlayConnection,
    val renderWindow: RenderWindow,
) : Renderer {
    private var currentOutlinePosition: Vec3i? = null
    private var currentOutlineBlockState: BlockState? = null

    private var outlineMesh: BlockOutlineMesh? = null
    private var collisionMesh: BlockOutlineMesh? = null
    private val outlineShader = Shader(
        resourceLocation = ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "chunk/block/outline"),
    )

    private val outlineColor = ChatColors.RED
    private val collisionColor = ChatColors.BLUE

    override fun init() {
        outlineShader.load(connection.assetsManager)
    }

    private fun drawLine(start: Vec3, end: Vec3, mesh: BlockOutlineMesh) {
        val direction = (end-start).normalize()
        val normal1 = Vec3(direction.z, direction.z, direction.x-direction.y)
        if (normal1 == VecUtil.EMPTY_VEC3) {
            normal1.x = normal1.z
            normal1.z = direction.z
        }
        normal1.normalizeAssign()
        val normal2 = (direction cross normal1).normalize()
        for (i in 0..4) {
            drawLineQuad(mesh, start, end, direction, normal1, normal2, i.isBit(0), i.isBit(1))
        }
    }

    private fun drawLineQuad(mesh: BlockOutlineMesh, start: Vec3, end: Vec3, direction: Vec3, normal1: Vec3, normal2: Vec3, invertNormal1: Boolean, invertNormal2: Boolean) {
        val normal1Multiplier = invertNormal1.positiveNegative
        val normal2Multiplier = invertNormal2.positiveNegative
        val positions = listOf(
            start + normal2 * normal2Multiplier * HALF_LINE_WIDTH - direction * HALF_LINE_WIDTH,
            start + normal1 * normal1Multiplier * HALF_LINE_WIDTH - direction * HALF_LINE_WIDTH,
            end  +  normal1 * normal1Multiplier * HALF_LINE_WIDTH + direction * HALF_LINE_WIDTH,
            end  +  normal2 * normal2Multiplier * HALF_LINE_WIDTH + direction * HALF_LINE_WIDTH,
        )
        for ((_, positionIndex) in ElementRenderer.DRAW_ODER) {
            mesh.addVertex(positions[positionIndex])
        }
    }

    private fun drawVoxelShape(shape: VoxelShape, blockPosition: Vec3, mesh: BlockOutlineMesh, margin: Float = 0.0f) {
        for (aabb in shape) {
            val min = blockPosition + aabb.min - margin
            val max = blockPosition + aabb.max + margin

            fun drawSideQuad(x: Float) {
                drawLine(Vec3(x, min.y, min.z), Vec3(x, max.y, min.z), mesh)
                drawLine(Vec3(x, min.y, min.z), Vec3(x, min.y, max.z), mesh)
                drawLine(Vec3(x, max.y, min.z), Vec3(x, max.y, max.z), mesh)
                drawLine(Vec3(x, min.y, max.z), Vec3(x, max.y, max.z), mesh)
            }

            // left quad
            drawSideQuad(min.x)

            // right quad
            drawSideQuad(max.x)

            // connections between 2 quads
            drawLine(Vec3(min.x, min.y, min.z), Vec3(max.x, min.y, min.z), mesh)
            drawLine(Vec3(min.x, max.y, min.z), Vec3(max.x, max.y, min.z), mesh)
            drawLine(Vec3(min.x, max.y, max.z), Vec3(max.x, max.y, max.z), mesh)
            drawLine(Vec3(min.x, min.y, max.z), Vec3(max.x, min.y, max.z), mesh)
        }
    }

    private fun draw(outlineMesh: BlockOutlineMesh, collisionMesh: BlockOutlineMesh?) {
        glDisable(GL_CULL_FACE)
        outlineShader.use()
        outlineShader.setRGBColor("tintColor", outlineColor)
        outlineMesh.draw()
        collisionMesh?.let {
            outlineShader.setRGBColor("tintColor", collisionColor)
            it.draw()
        }
        glEnable(GL_CULL_FACE)
    }

    private fun unload() {
        outlineMesh ?: return
        outlineMesh?.unload()
        collisionMesh?.unload()
        this.outlineMesh = null
        this.collisionMesh = null
        this.currentOutlinePosition = null
        this.currentOutlineBlockState = null
    }

    override fun draw() {
        val raycastHit = renderWindow.inputHandler.camera.getTargetBlock()

        var outlineMesh = outlineMesh
        var collisionMesh = collisionMesh

        if (raycastHit == null) {
            unload()
            return
        }

        if (raycastHit.distance >= RenderConstants.MAX_BLOCK_OUTLINE_RAYCAST_DISTANCE) {
            unload()
            return
        }

        if (connection.player.entity.gamemode == Gamemodes.ADVENTURE || connection.player.entity.gamemode == Gamemodes.SPECTATOR) {
            if (connection.mapping.blockEntityRegistry.get(raycastHit.blockState.block.resourceLocation) == null) {
                unload()
                return
            }
        }

        if (raycastHit.blockPosition == currentOutlinePosition && raycastHit.blockState == currentOutlineBlockState) {
            draw(outlineMesh!!, collisionMesh)
            return
        }

        outlineMesh?.unload()
        collisionMesh?.unload()
        outlineMesh = BlockOutlineMesh()

        val blockOffset = raycastHit.blockPosition.getWorldOffset(raycastHit.blockState.block).plus(raycastHit.blockPosition)

        drawVoxelShape(raycastHit.blockState.outlineShape, blockOffset, outlineMesh)
        outlineMesh.load()


        if (Minosoft.config.config.game.other.renderBlockOutlineCollisionBox) {
            collisionMesh = BlockOutlineMesh()

            drawVoxelShape(raycastHit.blockState.collisionShape, blockOffset, collisionMesh, 0.005f)
            collisionMesh.load()
            this.collisionMesh = collisionMesh
        }


        this.currentOutlinePosition = raycastHit.blockPosition
        this.currentOutlineBlockState = raycastHit.blockState
        this.outlineMesh = outlineMesh
        draw(outlineMesh, collisionMesh)
    }


    companion object : RendererBuilder<BlockOutlineRenderer> {
        override val RESOURCE_LOCATION = ResourceLocation("minosoft:block_outline")
        private const val LINE_WIDTH = 1.0f / 128.0f
        private const val HALF_LINE_WIDTH = LINE_WIDTH / 2.0f

        override fun build(connection: PlayConnection, renderWindow: RenderWindow): BlockOutlineRenderer {
            return BlockOutlineRenderer(connection, renderWindow)
        }
    }
}
