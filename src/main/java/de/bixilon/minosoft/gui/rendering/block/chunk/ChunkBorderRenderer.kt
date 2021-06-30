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

package de.bixilon.minosoft.gui.rendering.block.chunk

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.RendererBuilder
import de.bixilon.minosoft.gui.rendering.util.mesh.LineMesh
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.vec2.Vec2i
import glm_.vec3.Vec3

class ChunkBorderRenderer(
    val connection: PlayConnection,
    val renderWindow: RenderWindow,
) : Renderer {
    private var lastChunkPosition: Vec2i? = null
    private var lastMesh: LineMesh? = null


    private fun prepare() {
        val chunkPosition = renderWindow.connection.player.positionInfo.chunkPosition
        if (chunkPosition == lastChunkPosition && lastMesh != null) {
            return
        }
        lastMesh?.unload(false)
        val mesh = LineMesh()

        val dimension = renderWindow.connection.world.dimension ?: return
        val basePosition = chunkPosition * Vec2i(ProtocolDefinition.SECTION_WIDTH_X, ProtocolDefinition.SECTION_WIDTH_Z)


        // vertical lines
        for (x in 0..ProtocolDefinition.SECTION_WIDTH_X) {
            val color = when {
                x % ProtocolDefinition.SECTION_WIDTH_X == 0 -> ChatColors.BLUE
                x % 2 == 0 -> ChatColors.GREEN
                else -> ChatColors.YELLOW
            }

            mesh.drawLine(Vec3(basePosition.x + x, dimension.minY, basePosition.y), Vec3(basePosition.x + x, dimension.height, basePosition.y), RenderConstants.DEFAULT_LINE_WIDTH * 5, color)
            mesh.drawLine(Vec3(basePosition.x + x, dimension.minY, basePosition.y + ProtocolDefinition.SECTION_WIDTH_Z), Vec3(basePosition.x + x, dimension.height, basePosition.y + ProtocolDefinition.SECTION_WIDTH_Z), RenderConstants.DEFAULT_LINE_WIDTH * 5, color)
        }

        for (z in 0..ProtocolDefinition.SECTION_WIDTH_Z) {
            val color = when {
                z % ProtocolDefinition.SECTION_WIDTH_Z == 0 -> ChatColors.BLUE
                z % 2 == 0 -> ChatColors.GREEN
                else -> ChatColors.YELLOW
            }

            mesh.drawLine(Vec3(basePosition.x, dimension.minY, basePosition.y + z), Vec3(basePosition.x, dimension.height, basePosition.y + z), RenderConstants.DEFAULT_LINE_WIDTH * 5, color)
            mesh.drawLine(Vec3(basePosition.x + ProtocolDefinition.SECTION_WIDTH_X, dimension.minY, basePosition.y + z), Vec3(basePosition.x + ProtocolDefinition.SECTION_WIDTH_X, dimension.height, basePosition.y + z), RenderConstants.DEFAULT_LINE_WIDTH * 5, color)
        }

        // horizontal lines
        for (y in dimension.minY..dimension.height) {
            val borderColor = when {
                y % ProtocolDefinition.SECTION_HEIGHT_Y == 0 -> ChatColors.BLUE
                y % 2 == 0 -> ChatColors.GREEN
                else -> ChatColors.YELLOW
            }

            // x/z border
            mesh.drawLine(Vec3(basePosition.x, y, basePosition.y), Vec3(basePosition.x + ProtocolDefinition.SECTION_WIDTH_X, y, basePosition.y), RenderConstants.DEFAULT_LINE_WIDTH * 5, borderColor)
            mesh.drawLine(Vec3(basePosition.x, y, basePosition.y), Vec3(basePosition.x, y, basePosition.y + ProtocolDefinition.SECTION_WIDTH_Z), RenderConstants.DEFAULT_LINE_WIDTH * 5, borderColor)
            mesh.drawLine(Vec3(basePosition.x + ProtocolDefinition.SECTION_WIDTH_X, y, basePosition.y + ProtocolDefinition.SECTION_WIDTH_Z), Vec3(basePosition.x + ProtocolDefinition.SECTION_WIDTH_X, y, basePosition.y), RenderConstants.DEFAULT_LINE_WIDTH * 5, borderColor)
            mesh.drawLine(Vec3(basePosition.x + ProtocolDefinition.SECTION_WIDTH_X, y, basePosition.y + ProtocolDefinition.SECTION_WIDTH_Z), Vec3(basePosition.x, y, basePosition.y + ProtocolDefinition.SECTION_WIDTH_Z), RenderConstants.DEFAULT_LINE_WIDTH * 5, borderColor)


            if (y % ProtocolDefinition.SECTION_HEIGHT_Y != 0) {
                continue
            }


            for (x in 1..ProtocolDefinition.SECTION_MAX_X) {
                val color = when {
                    x % 2 == 0 -> ChatColors.GREEN
                    else -> ChatColors.YELLOW
                }
                mesh.drawLine(Vec3(basePosition.x + x, y, basePosition.y), Vec3(basePosition.x + x, y, basePosition.y + ProtocolDefinition.SECTION_WIDTH_Z), RenderConstants.DEFAULT_LINE_WIDTH * 5, color)
            }
            for (z in 1..ProtocolDefinition.SECTION_MAX_Z) {
                val color = when {
                    z % 2 == 0 -> ChatColors.GREEN
                    else -> ChatColors.YELLOW
                }
                mesh.drawLine(Vec3(basePosition.x, y, basePosition.y + z), Vec3(basePosition.x + ProtocolDefinition.SECTION_WIDTH_X, y, basePosition.y + z), RenderConstants.DEFAULT_LINE_WIDTH * 5, color)
            }
        }

        mesh.load()
        this.lastMesh = mesh
        this.lastChunkPosition = chunkPosition
    }

    override fun draw() {
        prepare()
        val mesh = lastMesh ?: return
        renderWindow.renderSystem.reset(faceCulling = false)
        renderWindow.shaderManager.genericColorShader.use()
        mesh.draw()
    }


    companion object : RendererBuilder<ChunkBorderRenderer> {
        override val RESOURCE_LOCATION = ResourceLocation("minosoft:chunk_borders")

        override fun build(connection: PlayConnection, renderWindow: RenderWindow): ChunkBorderRenderer {
            return ChunkBorderRenderer(connection, renderWindow)
        }
    }
}
