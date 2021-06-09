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
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.RendererBuilder
import de.bixilon.minosoft.gui.rendering.util.VecUtil.getWorldOffset
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3d
import de.bixilon.minosoft.gui.rendering.util.mesh.LineMesh
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3i
import org.lwjgl.opengl.GL11.*

class BlockOutlineRenderer(
    val connection: PlayConnection,
    val renderWindow: RenderWindow,
) : Renderer {
    private var currentOutlinePosition: Vec3i? = null
    private var currentOutlineBlockState: BlockState? = null

    private var outlineMesh: LineMesh? = null
    private var collisionMesh: LineMesh? = null

    private fun draw(outlineMesh: LineMesh, collisionMesh: LineMesh?) {
        glDisable(GL_CULL_FACE)
        if (Minosoft.config.config.game.other.blockOutline.disableZBuffer) {
            glDepthFunc(GL_ALWAYS)
        }
        renderWindow.shaderManager.genericColorShader.use()
        outlineMesh.draw()
        collisionMesh?.draw()
        glEnable(GL_CULL_FACE)
        if (Minosoft.config.config.game.other.blockOutline.disableZBuffer) {
            glDepthFunc(GL_LESS)
        }
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

        if (connection.player.gamemode == Gamemodes.ADVENTURE || connection.player.gamemode == Gamemodes.SPECTATOR) {
            if (raycastHit.blockState.block.blockEntityType == null) {
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
        outlineMesh = LineMesh(Minosoft.config.config.game.other.blockOutline.outlineColor, LINE_WIDTH)

        val blockOffset = raycastHit.blockPosition.toVec3d + raycastHit.blockPosition.getWorldOffset(raycastHit.blockState.block)

        outlineMesh.drawVoxelShape(raycastHit.blockState.outlineShape, blockOffset, outlineMesh)
        outlineMesh.load()


        if (Minosoft.config.config.game.other.blockOutline.collisionBoxes) {
            collisionMesh = LineMesh(Minosoft.config.config.game.other.blockOutline.collisionColor, LINE_WIDTH)

            collisionMesh.drawVoxelShape(raycastHit.blockState.collisionShape, blockOffset, collisionMesh, 0.005f)
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

        override fun build(connection: PlayConnection, renderWindow: RenderWindow): BlockOutlineRenderer {
            return BlockOutlineRenderer(connection, renderWindow)
        }
    }
}
