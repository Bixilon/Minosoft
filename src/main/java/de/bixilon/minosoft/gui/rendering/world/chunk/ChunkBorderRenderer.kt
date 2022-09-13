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

package de.bixilon.minosoft.gui.rendering.world.chunk

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.renderer.MeshSwapper
import de.bixilon.minosoft.gui.rendering.renderer.renderer.AsyncRenderer
import de.bixilon.minosoft.gui.rendering.renderer.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.phases.OpaqueDrawable
import de.bixilon.minosoft.gui.rendering.util.mesh.LineMesh
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.blockPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.chunkPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.sectionHeight
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.format
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class ChunkBorderRenderer(
    val connection: PlayConnection,
    override val renderWindow: RenderWindow,
) : AsyncRenderer, OpaqueDrawable, MeshSwapper {
    private val profile = connection.profiles.rendering
    override val renderSystem: RenderSystem = renderWindow.renderSystem
    private var chunkPosition: Vec2i? = null
    private var sectionHeight: Int = Int.MIN_VALUE

    override var mesh: LineMesh? = null

    override var nextMesh: LineMesh? = null
    override var unload = false

    override val skipOpaque: Boolean
        get() = mesh == null || !profile.chunkBorder.enabled

    override fun init(latch: CountUpAndDownLatch) {
        renderWindow.inputHandler.registerKeyCallback(
            CHUNK_BORDER_TOGGLE_KEY_COMBINATION,
            KeyBinding(
                KeyActions.MODIFIER to setOf(KeyCodes.KEY_F3),
                KeyActions.STICKY to setOf(KeyCodes.KEY_G),
            ), defaultPressed = profile.chunkBorder.enabled
        ) {
            profile.chunkBorder.enabled = it
            connection.util.sendDebugMessage("Chunk borders: ${it.format()}")
        }
    }

    override fun prepareDrawAsync() {
        if (!profile.chunkBorder.enabled) {
            this.unload = true
            return
        }
        val eyePosition = renderWindow.camera.matrixHandler.eyePosition.blockPosition
        val chunkPosition = eyePosition.chunkPosition
        val sectionHeight = eyePosition.sectionHeight
        if (chunkPosition == this.chunkPosition && sectionHeight == this.sectionHeight && mesh != null) {
            return
        }
        unload = true
        val mesh = LineMesh(renderWindow)

        val dimension = renderWindow.connection.world.dimension ?: return
        val basePosition = chunkPosition * Vec2i(ProtocolDefinition.SECTION_WIDTH_X, ProtocolDefinition.SECTION_WIDTH_Z)

        mesh.drawInnerChunkLines(basePosition, dimension)

        if (sectionHeight in dimension.minSection..dimension.maxSection) {
            mesh.drawSectionLines(Vec3i(basePosition.x, sectionHeight * ProtocolDefinition.SECTION_HEIGHT_Y, basePosition.y))
        }

        mesh.drawOuterChunkLines(chunkPosition, dimension)

        this.nextMesh = mesh
        this.chunkPosition = chunkPosition
        this.sectionHeight = sectionHeight
    }

    private fun LineMesh.drawOuterChunkLines(chunkPosition: Vec2i, dimension: DimensionProperties) {
        for (x in -OUTER_CHUNK_SIZE..OUTER_CHUNK_SIZE + 1) {
            for (z in -OUTER_CHUNK_SIZE..OUTER_CHUNK_SIZE + 1) {
                if ((x == 0 || x == 1) && (z == 0 || z == 1)) {
                    continue
                }
                val chunkBase = (chunkPosition + Vec2i(x, z)) * Vec2i(ProtocolDefinition.SECTION_WIDTH_X, ProtocolDefinition.SECTION_WIDTH_Z)
                drawLine(Vec3(chunkBase.x + 0, dimension.minY, chunkBase.y), Vec3(chunkBase.x + 0, dimension.maxY + 1, chunkBase.y), OUTER_CHUNK_LINE_WIDTH, OUTER_CHUNK_COLOR)
            }
        }
    }

    private fun LineMesh.drawInnerChunkLines(basePosition: Vec2i, dimension: DimensionProperties) {
        drawLine(Vec3(basePosition.x + 0, dimension.minY, basePosition.y), Vec3(basePosition.x + 0, dimension.maxY + 1, basePosition.y), INNER_CHUNK_LINE_WIDTH, INNER_CHUNK_COLOR)
        drawLine(Vec3(basePosition.x, dimension.minY, basePosition.y + ProtocolDefinition.SECTION_WIDTH_Z), Vec3(basePosition.x, dimension.maxY + 1, basePosition.y + ProtocolDefinition.SECTION_WIDTH_Z), INNER_CHUNK_LINE_WIDTH, INNER_CHUNK_COLOR)
        drawLine(Vec3(basePosition.x + ProtocolDefinition.SECTION_WIDTH_X, dimension.minY, basePosition.y + 0), Vec3(basePosition.x + ProtocolDefinition.SECTION_WIDTH_X, dimension.maxY + 1, basePosition.y + 0), INNER_CHUNK_LINE_WIDTH, INNER_CHUNK_COLOR)
        drawLine(Vec3(basePosition.x + ProtocolDefinition.SECTION_WIDTH_X, dimension.minY, basePosition.y + ProtocolDefinition.SECTION_WIDTH_Z), Vec3(basePosition.x + ProtocolDefinition.SECTION_WIDTH_X, dimension.maxY + 1, basePosition.y + ProtocolDefinition.SECTION_WIDTH_Z), INNER_CHUNK_LINE_WIDTH, INNER_CHUNK_COLOR)

        for (sectionHeight in dimension.minSection..dimension.maxSection) {
            val y = sectionHeight * ProtocolDefinition.SECTION_HEIGHT_Y
            drawLine(Vec3(basePosition.x, y, basePosition.y), Vec3(basePosition.x + ProtocolDefinition.SECTION_WIDTH_X, y, basePosition.y), INNER_CHUNK_LINE_WIDTH, INNER_CHUNK_COLOR)
            drawLine(Vec3(basePosition.x, y, basePosition.y), Vec3(basePosition.x, y, basePosition.y + ProtocolDefinition.SECTION_WIDTH_Z), INNER_CHUNK_LINE_WIDTH, INNER_CHUNK_COLOR)
            drawLine(Vec3(basePosition.x + ProtocolDefinition.SECTION_WIDTH_X, y, basePosition.y + ProtocolDefinition.SECTION_WIDTH_Z), Vec3(basePosition.x + ProtocolDefinition.SECTION_WIDTH_X, y, basePosition.y), INNER_CHUNK_LINE_WIDTH, INNER_CHUNK_COLOR)
            drawLine(Vec3(basePosition.x + ProtocolDefinition.SECTION_WIDTH_X, y, basePosition.y + ProtocolDefinition.SECTION_WIDTH_Z), Vec3(basePosition.x, y, basePosition.y + ProtocolDefinition.SECTION_WIDTH_Z), INNER_CHUNK_LINE_WIDTH, INNER_CHUNK_COLOR)
        }
    }

    private fun LineMesh.drawSectionLines(basePosition: Vec3i) {
        // vertical lines
        for (x in 1..ProtocolDefinition.SECTION_MAX_X) {
            val color = when {
                x % 2 == 0 -> SECTION_COLOR_1
                else -> SECTION_COLOR_2
            }

            drawLine(Vec3(basePosition.x + x, basePosition.y, basePosition.z), Vec3(basePosition.x + x, basePosition.y + ProtocolDefinition.SECTION_HEIGHT_Y, basePosition.z), SECTION_LINE_WIDTH, color)
            drawLine(Vec3(basePosition.x + x, basePosition.y, basePosition.z + ProtocolDefinition.SECTION_WIDTH_Z), Vec3(basePosition.x + x, basePosition.y + ProtocolDefinition.SECTION_HEIGHT_Y, basePosition.z + ProtocolDefinition.SECTION_WIDTH_Z), SECTION_LINE_WIDTH, color)
        }

        for (z in 1..ProtocolDefinition.SECTION_MAX_Z) {
            val color = when {
                z % 2 == 0 -> SECTION_COLOR_1
                else -> SECTION_COLOR_2
            }

            drawLine(Vec3(basePosition.x, basePosition.y, basePosition.z + z), Vec3(basePosition.x, basePosition.y + ProtocolDefinition.SECTION_HEIGHT_Y, basePosition.z + z), SECTION_LINE_WIDTH, color)
            drawLine(Vec3(basePosition.x + ProtocolDefinition.SECTION_WIDTH_X, basePosition.y, basePosition.z + z), Vec3(basePosition.x + ProtocolDefinition.SECTION_WIDTH_X, basePosition.y + ProtocolDefinition.SECTION_HEIGHT_Y, basePosition.z + z), SECTION_LINE_WIDTH, color)
        }

        // horizontal lines
        for (y in basePosition.y..basePosition.y + ProtocolDefinition.SECTION_HEIGHT_Y) {
            val borderColor = when {
                y % 2 == 0 -> SECTION_COLOR_1
                else -> SECTION_COLOR_2
            }

            // x/z border
            if (y != basePosition.y && y != basePosition.y + ProtocolDefinition.SECTION_HEIGHT_Y) {
                drawLine(Vec3(basePosition.x, y, basePosition.z), Vec3(basePosition.x + ProtocolDefinition.SECTION_WIDTH_X, y, basePosition.z), SECTION_LINE_WIDTH, borderColor)
                drawLine(Vec3(basePosition.x, y, basePosition.z), Vec3(basePosition.x, y, basePosition.z + ProtocolDefinition.SECTION_WIDTH_Z), SECTION_LINE_WIDTH, borderColor)
                drawLine(Vec3(basePosition.x + ProtocolDefinition.SECTION_WIDTH_X, y, basePosition.z + ProtocolDefinition.SECTION_WIDTH_Z), Vec3(basePosition.x + ProtocolDefinition.SECTION_WIDTH_X, y, basePosition.z), SECTION_LINE_WIDTH, borderColor)
                drawLine(Vec3(basePosition.x + ProtocolDefinition.SECTION_WIDTH_X, y, basePosition.z + ProtocolDefinition.SECTION_WIDTH_Z), Vec3(basePosition.x, y, basePosition.z + ProtocolDefinition.SECTION_WIDTH_Z), SECTION_LINE_WIDTH, borderColor)
            }

            if (y % ProtocolDefinition.SECTION_HEIGHT_Y != 0) {
                continue
            }


            for (x in 1..ProtocolDefinition.SECTION_MAX_X) {
                val color = when {
                    x % 2 == 0 -> SECTION_COLOR_1
                    else -> SECTION_COLOR_2
                }
                drawLine(Vec3(basePosition.x + x, y, basePosition.z), Vec3(basePosition.x + x, y, basePosition.z + ProtocolDefinition.SECTION_WIDTH_Z), SECTION_LINE_WIDTH, color)
            }
            for (z in 1..ProtocolDefinition.SECTION_MAX_Z) {
                val color = when {
                    z % 2 == 0 -> SECTION_COLOR_1
                    else -> SECTION_COLOR_2
                }
                drawLine(Vec3(basePosition.x, y, basePosition.z + z), Vec3(basePosition.x + ProtocolDefinition.SECTION_WIDTH_X, y, basePosition.z + z), SECTION_LINE_WIDTH, color)
            }
        }
    }

    override fun setupOpaque() {
        renderWindow.renderSystem.reset()
        renderWindow.shaderManager.genericColorShader.use()
    }

    override fun drawOpaque() {
        mesh?.draw()
    }


    companion object : RendererBuilder<ChunkBorderRenderer> {
        override val RESOURCE_LOCATION = ResourceLocation("minosoft:chunk_borders")
        private val CHUNK_BORDER_TOGGLE_KEY_COMBINATION = "minosoft:toggle_chunk_borders".toResourceLocation()
        private const val SECTION_LINE_WIDTH = RenderConstants.DEFAULT_LINE_WIDTH * 3
        private const val INNER_CHUNK_LINE_WIDTH = SECTION_LINE_WIDTH * 3
        private const val OUTER_CHUNK_LINE_WIDTH = INNER_CHUNK_LINE_WIDTH * 3

        private val SECTION_COLOR_1 = ChatColors.GREEN
        private val SECTION_COLOR_2 = ChatColors.YELLOW
        private val INNER_CHUNK_COLOR = ChatColors.BLUE
        private val OUTER_CHUNK_COLOR = ChatColors.RED

        private const val OUTER_CHUNK_SIZE = 3


        override fun build(connection: PlayConnection, renderWindow: RenderWindow): ChunkBorderRenderer {
            return ChunkBorderRenderer(connection, renderWindow)
        }
    }
}
