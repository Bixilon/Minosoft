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

import de.bixilon.minosoft.data.Gamemodes
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.RendererBuilder
import de.bixilon.minosoft.gui.rendering.chunk.VoxelShape
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.util.VecUtil.getWorldOffset
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
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
    private val outlineShader = Shader(
        resourceLocation = ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "chunk/block/outline"),
    )

    override fun init() {
        outlineShader.load(connection.assetsManager)
        outlineShader.use().setRGBColor("tintColor", ChatColors.RED)
    }

    private fun drawLine(start: Vec3, end: Vec3, mesh: BlockOutlineMesh) {
        // ToDo: Maybe use a cuboid, also we need to rotate `rotatedLineWidth`
        val rotatedLineWidth = Vec3(HALF_LINE_WIDTH, HALF_LINE_WIDTH, HALF_LINE_WIDTH)
        mesh.addVertex(Vec3(start.x, start.y, start.z) - rotatedLineWidth)
        mesh.addVertex(Vec3(start.x, start.y, start.z) + rotatedLineWidth)
        mesh.addVertex(Vec3(end.x, end.y, end.z) - rotatedLineWidth)

        mesh.addVertex(Vec3(end.x, end.y, end.z) - rotatedLineWidth)
        mesh.addVertex(Vec3(start.x, start.y, start.z) + rotatedLineWidth)
        mesh.addVertex(Vec3(end.x, end.y, end.z) + rotatedLineWidth)
    }

    private fun drawVoxelShape(shape: VoxelShape, blockPosition: Vec3, mesh: BlockOutlineMesh) {
        for (aabb in shape) {
            val min = blockPosition + aabb.min
            val max = blockPosition + aabb.max

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

    private fun draw(mesh: BlockOutlineMesh) {
        glDisable(GL_CULL_FACE)
        outlineShader.use()
        mesh.draw()
        glEnable(GL_CULL_FACE)
    }

    private fun unload() {
        outlineMesh ?: return
        outlineMesh?.unload()
        this.outlineMesh = null
        this.currentOutlinePosition = null
        this.currentOutlineBlockState = null
    }

    override fun draw() {
        val raycastHit = renderWindow.inputHandler.camera.getTargetBlock()

        var outlineMesh = outlineMesh



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
            draw(outlineMesh!!)
            return
        }

        outlineMesh?.unload()
        outlineMesh = BlockOutlineMesh()

        drawVoxelShape(raycastHit.blockState.outlineShape, raycastHit.blockPosition.getWorldOffset(raycastHit.blockState.block).plus(raycastHit.blockPosition), outlineMesh)
        outlineMesh.load()

        this.currentOutlinePosition = raycastHit.blockPosition
        this.currentOutlineBlockState = raycastHit.blockState
        this.outlineMesh = outlineMesh
        draw(outlineMesh)
    }


    companion object : RendererBuilder<BlockOutlineRenderer> {
        override val RESOURCE_LOCATION = ResourceLocation("minosoft:block_outline")
        private const val LINE_WIDTH = 1.0f / 64.0f
        private const val HALF_LINE_WIDTH = LINE_WIDTH / 2.0f

        override fun build(connection: PlayConnection, renderWindow: RenderWindow): BlockOutlineRenderer {
            return BlockOutlineRenderer(connection, renderWindow)
        }
    }
}
