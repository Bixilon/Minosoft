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

package de.bixilon.minosoft.gui.rendering.block.outline

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.RendererBuilder
import de.bixilon.minosoft.gui.rendering.input.camera.hit.BlockRaycastHit
import de.bixilon.minosoft.gui.rendering.system.base.DepthFunctions
import de.bixilon.minosoft.gui.rendering.util.VecUtil.getWorldOffset
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3d
import de.bixilon.minosoft.gui.rendering.util.mesh.LineMesh
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.util.KUtil.nullCast
import glm_.vec3.Vec3i

class BlockOutlineRenderer(
    val connection: PlayConnection,
    val renderWindow: RenderWindow,
) : Renderer {
    private var currentOutlinePosition: Vec3i? = null
    private var currentOutlineBlockState: BlockState? = null

    private var currentMesh: LineMesh? = null

    private fun drawMesh() {
        val currentMesh = currentMesh ?: return
        renderWindow.renderSystem.reset(faceCulling = false)
        if (Minosoft.config.config.game.other.blockOutline.disableZBuffer) {
            renderWindow.renderSystem.depth = DepthFunctions.ALWAYS
        }
        renderWindow.shaderManager.genericColorShader.use()
        currentMesh.draw()
    }

    private fun unload() {
        currentMesh ?: return
        currentMesh?.unload()
        this.currentMesh = null
        this.currentOutlinePosition = null
        this.currentOutlineBlockState = null
    }

    override fun draw() {
        val raycastHit = renderWindow.inputHandler.camera.target.nullCast<BlockRaycastHit>()

        var currentMesh = currentMesh

        if (raycastHit == null) {
            unload()
            return
        }

        if (raycastHit.distance >= connection.player.reachDistance) {
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
            drawMesh()
            return
        }

        currentMesh?.unload()
        currentMesh = LineMesh(renderWindow)

        val blockOffset = raycastHit.blockPosition.toVec3d + raycastHit.blockPosition.getWorldOffset(raycastHit.blockState.block)

        currentMesh.drawVoxelShape(raycastHit.blockState.outlineShape, blockOffset, RenderConstants.DEFAULT_LINE_WIDTH, Minosoft.config.config.game.other.blockOutline.outlineColor)


        if (Minosoft.config.config.game.other.blockOutline.collisionBoxes) {
            currentMesh.drawVoxelShape(raycastHit.blockState.collisionShape, blockOffset, RenderConstants.DEFAULT_LINE_WIDTH, Minosoft.config.config.game.other.blockOutline.collisionColor, 0.005f)
        }

        currentMesh.load()


        this.currentOutlinePosition = raycastHit.blockPosition
        this.currentOutlineBlockState = raycastHit.blockState
        this.currentMesh = currentMesh
        drawMesh()
    }


    companion object : RendererBuilder<BlockOutlineRenderer> {
        override val RESOURCE_LOCATION = ResourceLocation("minosoft:block_outline")

        override fun build(connection: PlayConnection, renderWindow: RenderWindow): BlockOutlineRenderer {
            return BlockOutlineRenderer(connection, renderWindow)
        }
    }
}
