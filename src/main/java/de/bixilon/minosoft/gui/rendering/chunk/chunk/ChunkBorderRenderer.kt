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

package de.bixilon.minosoft.gui.rendering.chunk.chunk

import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kmath.vec.vec3.i.Vec3i
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.renderer.MeshSwapper
import de.bixilon.minosoft.gui.rendering.renderer.renderer.AsyncRenderer
import de.bixilon.minosoft.gui.rendering.renderer.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.renderer.renderer.world.LayerSettings
import de.bixilon.minosoft.gui.rendering.renderer.renderer.world.WorldRenderer
import de.bixilon.minosoft.gui.rendering.system.base.layer.RenderLayer
import de.bixilon.minosoft.gui.rendering.system.base.settings.RenderSettings
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.mesh.integrated.LineMeshBuilder
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.blockPosition
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.util.KUtil.format

class ChunkBorderRenderer(
    val session: PlaySession,
    override val context: RenderContext,
) : WorldRenderer, AsyncRenderer, MeshSwapper<Mesh> {
    override val layers = LayerSettings()
    private val profile = session.profiles.rendering
    private var offset = BlockPosition()
    private var chunkPosition: ChunkPosition? = null
    private var sectionHeight: Int = Int.MIN_VALUE

    override var mesh: Mesh? = null

    override var nextMesh: Mesh? = null
    override var unload = false

    override fun registerLayers() {
        layers.register(ChunkBorderLayer, context.shaders.genericColorShader, this::draw) { mesh == null || !profile.chunkBorder.enabled }
    }

    override fun init(latch: AbstractLatch) {
        context.input.bindings.register(
            CHUNK_BORDER_TOGGLE_KEY_COMBINATION,
            KeyBinding(
                KeyActions.MODIFIER to setOf(KeyCodes.KEY_F3),
                KeyActions.STICKY to setOf(KeyCodes.KEY_G),
            ), pressed = profile.chunkBorder.enabled
        ) {
            profile.chunkBorder.enabled = it
            session.util.sendDebugMessage("Chunk borders: ${it.format()}")
        }
    }

    override fun prepareDrawAsync() {
        if (!profile.chunkBorder.enabled) {
            this.unload = true
            return
        }
        val eyePosition = session.camera.entity.renderInfo.eyePosition.blockPosition
        val chunkPosition = eyePosition.chunkPosition
        val sectionHeight = eyePosition.sectionHeight
        val offset = context.camera.offset.offset
        if (chunkPosition == this.chunkPosition && sectionHeight == this.sectionHeight && this.offset == offset && mesh != null) {
            return
        }
        unload = true
        val mesh = LineMeshBuilder(context)

        val dimension = context.session.world.dimension
        val basePosition = (chunkPosition * ChunkSize.SECTION_WIDTH_X) - ChunkPosition(offset.x, offset.z)

        mesh.drawInnerChunkLines(Vec3i(basePosition.x, -offset.y, basePosition.z), dimension)

        if (sectionHeight in dimension.minSection until dimension.maxSection) {
            mesh.drawSectionLines(Vec3i(basePosition.x, sectionHeight * ChunkSize.SECTION_HEIGHT_Y, basePosition.z))
        }

        mesh.drawOuterChunkLines(chunkPosition, offset, dimension)

        this.chunkPosition = chunkPosition
        this.sectionHeight = sectionHeight
        this.offset = offset
        this.nextMesh = mesh.bake()
    }

    private fun LineMeshBuilder.drawOuterChunkLines(chunkPosition: ChunkPosition, offset: BlockPosition, dimension: DimensionProperties) {
        for (x in -OUTER_CHUNK_SIZE..OUTER_CHUNK_SIZE + 1) {
            for (z in -OUTER_CHUNK_SIZE..OUTER_CHUNK_SIZE + 1) {
                if ((x == 0 || x == 1) && (z == 0 || z == 1)) {
                    continue
                }
                val chunkBase = (chunkPosition + ChunkPosition(x, z)) * ChunkSize.SECTION_WIDTH_X - ChunkPosition(offset.x, offset.z)
                drawLine(Vec3f(chunkBase.x + 0, dimension.minY - offset.y, chunkBase.z), Vec3f(chunkBase.x + 0, dimension.maxY - offset.y + 1, chunkBase.z), OUTER_CHUNK_LINE_WIDTH, OUTER_CHUNK_COLOR)
            }
        }
    }

    private fun LineMeshBuilder.drawInnerChunkLines(basePosition: Vec3i, dimension: DimensionProperties) {
        drawLine(Vec3f(basePosition.x + 0, basePosition.y + dimension.minY, basePosition.z), Vec3f(basePosition.x + 0, basePosition.y + dimension.maxY + 1, basePosition.z), INNER_CHUNK_LINE_WIDTH, INNER_CHUNK_COLOR)
        drawLine(Vec3f(basePosition.x, basePosition.y + dimension.minY, basePosition.z + ChunkSize.SECTION_WIDTH_Z), Vec3f(basePosition.x, basePosition.y + dimension.maxY + 1, basePosition.z + ChunkSize.SECTION_WIDTH_Z), INNER_CHUNK_LINE_WIDTH, INNER_CHUNK_COLOR)
        drawLine(Vec3f(basePosition.x + ChunkSize.SECTION_WIDTH_X, basePosition.y + dimension.minY, basePosition.z + 0), Vec3f(basePosition.x + ChunkSize.SECTION_WIDTH_X, basePosition.y + dimension.maxY + 1, basePosition.z + 0), INNER_CHUNK_LINE_WIDTH, INNER_CHUNK_COLOR)
        drawLine(Vec3f(basePosition.x + ChunkSize.SECTION_WIDTH_X, basePosition.y + dimension.minY, basePosition.z + ChunkSize.SECTION_WIDTH_Z), Vec3f(basePosition.x + ChunkSize.SECTION_WIDTH_X, basePosition.y + dimension.maxY + 1, basePosition.z + ChunkSize.SECTION_WIDTH_Z), INNER_CHUNK_LINE_WIDTH, INNER_CHUNK_COLOR)

        for (sectionHeight in dimension.minSection..dimension.maxSection) {
            val y = basePosition.y + sectionHeight * ChunkSize.SECTION_HEIGHT_Y
            drawLine(Vec3f(basePosition.x, y, basePosition.z), Vec3f(basePosition.x + ChunkSize.SECTION_WIDTH_X, y, basePosition.z), INNER_CHUNK_LINE_WIDTH, INNER_CHUNK_COLOR)
            drawLine(Vec3f(basePosition.x, y, basePosition.z), Vec3f(basePosition.x, y, basePosition.z + ChunkSize.SECTION_WIDTH_Z), INNER_CHUNK_LINE_WIDTH, INNER_CHUNK_COLOR)
            drawLine(Vec3f(basePosition.x + ChunkSize.SECTION_WIDTH_X, y, basePosition.z + ChunkSize.SECTION_WIDTH_Z), Vec3f(basePosition.x + ChunkSize.SECTION_WIDTH_X, y, basePosition.z), INNER_CHUNK_LINE_WIDTH, INNER_CHUNK_COLOR)
            drawLine(Vec3f(basePosition.x + ChunkSize.SECTION_WIDTH_X, y, basePosition.z + ChunkSize.SECTION_WIDTH_Z), Vec3f(basePosition.x, y, basePosition.z + ChunkSize.SECTION_WIDTH_Z), INNER_CHUNK_LINE_WIDTH, INNER_CHUNK_COLOR)
        }
    }

    private fun LineMeshBuilder.drawSectionLines(basePosition: Vec3i) {
        // vertical lines
        for (x in 1..ChunkSize.SECTION_MAX_X) {
            val color = when {
                x % 2 == 0 -> SECTION_COLOR_1
                else -> SECTION_COLOR_2
            }

            drawLine(Vec3f(basePosition.x + x, basePosition.y, basePosition.z), Vec3f(basePosition.x + x, basePosition.y + ChunkSize.SECTION_HEIGHT_Y, basePosition.z), SECTION_LINE_WIDTH, color)
            drawLine(Vec3f(basePosition.x + x, basePosition.y, basePosition.z + ChunkSize.SECTION_WIDTH_Z), Vec3f(basePosition.x + x, basePosition.y + ChunkSize.SECTION_HEIGHT_Y, basePosition.z + ChunkSize.SECTION_WIDTH_Z), SECTION_LINE_WIDTH, color)
        }

        for (z in 1..ChunkSize.SECTION_MAX_Z) {
            val color = when {
                z % 2 == 0 -> SECTION_COLOR_1
                else -> SECTION_COLOR_2
            }

            drawLine(Vec3f(basePosition.x, basePosition.y, basePosition.z + z), Vec3f(basePosition.x, basePosition.y + ChunkSize.SECTION_HEIGHT_Y, basePosition.z + z), SECTION_LINE_WIDTH, color)
            drawLine(Vec3f(basePosition.x + ChunkSize.SECTION_WIDTH_X, basePosition.y, basePosition.z + z), Vec3f(basePosition.x + ChunkSize.SECTION_WIDTH_X, basePosition.y + ChunkSize.SECTION_HEIGHT_Y, basePosition.z + z), SECTION_LINE_WIDTH, color)
        }

        // horizontal lines
        for (y in basePosition.y..basePosition.y + ChunkSize.SECTION_HEIGHT_Y) {
            val borderColor = when {
                y % 2 == 0 -> SECTION_COLOR_1
                else -> SECTION_COLOR_2
            }

            // x/z border
            if (y != basePosition.y && y != basePosition.y + ChunkSize.SECTION_HEIGHT_Y) {
                drawLine(Vec3f(basePosition.x, y, basePosition.z), Vec3f(basePosition.x + ChunkSize.SECTION_WIDTH_X, y, basePosition.z), SECTION_LINE_WIDTH, borderColor)
                drawLine(Vec3f(basePosition.x, y, basePosition.z), Vec3f(basePosition.x, y, basePosition.z + ChunkSize.SECTION_WIDTH_Z), SECTION_LINE_WIDTH, borderColor)
                drawLine(Vec3f(basePosition.x + ChunkSize.SECTION_WIDTH_X, y, basePosition.z + ChunkSize.SECTION_WIDTH_Z), Vec3f(basePosition.x + ChunkSize.SECTION_WIDTH_X, y, basePosition.z), SECTION_LINE_WIDTH, borderColor)
                drawLine(Vec3f(basePosition.x + ChunkSize.SECTION_WIDTH_X, y, basePosition.z + ChunkSize.SECTION_WIDTH_Z), Vec3f(basePosition.x, y, basePosition.z + ChunkSize.SECTION_WIDTH_Z), SECTION_LINE_WIDTH, borderColor)
            }

            if (y % ChunkSize.SECTION_HEIGHT_Y != 0) {
                continue
            }


            for (x in 1..ChunkSize.SECTION_MAX_X) {
                val color = when {
                    x % 2 == 0 -> SECTION_COLOR_1
                    else -> SECTION_COLOR_2
                }
                drawLine(Vec3f(basePosition.x + x, y, basePosition.z), Vec3f(basePosition.x + x, y, basePosition.z + ChunkSize.SECTION_WIDTH_Z), SECTION_LINE_WIDTH, color)
            }
            for (z in 1..ChunkSize.SECTION_MAX_Z) {
                val color = when {
                    z % 2 == 0 -> SECTION_COLOR_1
                    else -> SECTION_COLOR_2
                }
                drawLine(Vec3f(basePosition.x, y, basePosition.z + z), Vec3f(basePosition.x + ChunkSize.SECTION_WIDTH_X, y, basePosition.z + z), SECTION_LINE_WIDTH, color)
            }
        }
    }

    private fun draw() {
        mesh?.draw()
    }

    private object ChunkBorderLayer : RenderLayer {
        override val settings = RenderSettings(
            polygonOffset = true,
            polygonOffsetFactor = -1.0f,
            polygonOffsetUnit = -2.0f,
        )
        override val priority get() = 1500
    }


    companion object : RendererBuilder<ChunkBorderRenderer> {
        private val CHUNK_BORDER_TOGGLE_KEY_COMBINATION = minosoft("toggle_chunk_borders")
        private const val SECTION_LINE_WIDTH = RenderConstants.DEFAULT_LINE_WIDTH * 3
        private const val INNER_CHUNK_LINE_WIDTH = SECTION_LINE_WIDTH * 3
        private const val OUTER_CHUNK_LINE_WIDTH = INNER_CHUNK_LINE_WIDTH * 3

        private val SECTION_COLOR_1 = ChatColors.GREEN
        private val SECTION_COLOR_2 = ChatColors.YELLOW
        private val INNER_CHUNK_COLOR = ChatColors.BLUE
        private val OUTER_CHUNK_COLOR = ChatColors.RED

        private const val OUTER_CHUNK_SIZE = 3


        override fun build(session: PlaySession, context: RenderContext): ChunkBorderRenderer {
            return ChunkBorderRenderer(session, context)
        }
    }
}
