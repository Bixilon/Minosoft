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

package de.bixilon.minosoft.gui.rendering.hud.nodes.debug

import de.bixilon.minosoft.config.config.game.controls.KeyBindingsNames
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.block.WorldRenderer
import de.bixilon.minosoft.gui.rendering.hud.HUDElementProperties
import de.bixilon.minosoft.gui.rendering.hud.HUDRenderBuilder
import de.bixilon.minosoft.gui.rendering.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.hud.nodes.properties.NodeAlignment
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.UnitFormatter
import glm_.vec2.Vec2
import java.util.*


class HUDWorldDebugNode(hudRenderer: HUDRenderer) : DebugScreenNode(hudRenderer) {
    private val connection = hudRenderer.connection
    private val player = connection.player
    private val camera = hudRenderer.renderWindow.inputHandler.camera
    private val worldRenderer = hudRenderer.renderWindow.rendererMap[WorldRenderer.RESOURCE_LOCATION] as WorldRenderer?

    init {
        layout.sizing.forceAlign = NodeAlignment.LEFT
        layout.sizing.padding.left = 2
        layout.sizing.padding.top = 2
        text("§cMinosoft 0.1-pre1")
    }

    private val fpsText = text("TBA")
    private val timingsText = text("TBA")
    private val chunksText = text("TBA")
    private val openGLText = text("TBA")

    init {
        text("Connected to ${hudRenderer.connection.address} on ${hudRenderer.connection.version} with ${hudRenderer.connection.account.username}")
        text("")
    }

    private val positionText = text("TBA")
    private val blockPositionText = text("TBA")
    private val chunkPositionText = text("TBA")
    private val facingText = text("TBA")
    private val gamemodeText = text("TBA")
    private val dimensionText = text("TBA")
    private val biomeText = text("TBA")

    init {
        text()
    }

    private val difficultyText = text("TBA")
    private val lightText = text("TBA")


    override fun draw() {
        if (System.currentTimeMillis() - lastPrepareTime < ProtocolDefinition.TICK_TIME * 2) {
            return
        }

        fpsText.sText = "FPS: ${getFPS()}"
        chunksText.sText = "Chunks: q=${worldRenderer?.queuedChunks?.size} v=${worldRenderer?.visibleChunks?.size} p=${worldRenderer?.allChunkSections?.size} t=${hudRenderer.connection.world.chunks.size}"
        timingsText.sText = "Timings: avg ${getAvgFrameTime()}ms, min ${getMinFrameTime()}ms, max ${getMaxFrameTime()}ms"
        openGLText.sText = "GL: m=${worldRenderer?.meshes?.let { UnitFormatter.formatNumber(it) }} t=${worldRenderer?.triangles?.let { UnitFormatter.formatNumber(it) }}"


        // ToDo: Prepare on change
        gamemodeText.sText = "Gamemode: ${hudRenderer.connection.player.gamemode.name.lowercase(Locale.getDefault())}"
        positionText.sText = "XYZ ${getPosition()}"
        blockPositionText.sText = "Block ${getBlockPosition()}"
        chunkPositionText.sText = "Chunk ${getChunkLocation()}"
        facingText.sText = "Facing: ${getFacing()}"

        biomeText.sText = "Biome: ${player.positionInfo.biome}"
        dimensionText.sText = "Dimension: ${hudRenderer.connection.world.dimension}"

        difficultyText.sText = "Difficulty: ${hudRenderer.connection.world.difficulty?.name?.lowercase(Locale.getDefault())}, ${
            if (hudRenderer.connection.world.difficultyLocked) {
                "locked"
            } else {
                "unlocked"
            }
        }"

        lightText.sText = "Client light: sky=${hudRenderer.connection.world.worldLightAccessor.getSkyLight(player.positionInfo.blockPosition)}, block=${hudRenderer.connection.world.worldLightAccessor.getBlockLight(player.positionInfo.blockPosition)}"


        lastPrepareTime = System.currentTimeMillis()
    }

    private fun nanoToMillis1d(nanos: Long): String {
        return "%.1f".format(nanos / 1E6f)
    }

    private fun getFPS(): String {
        val renderStats = hudRenderer.renderWindow.renderStats
        return "${renderStats.fpsLastSecond}"
    }

    private fun getAvgFrameTime(): String {
        return nanoToMillis1d(hudRenderer.renderWindow.renderStats.avgFrameTime)
    }

    private fun getMinFrameTime(): String {
        return nanoToMillis1d(hudRenderer.renderWindow.renderStats.minFrameTime)
    }

    private fun getMaxFrameTime(): String {
        return nanoToMillis1d(hudRenderer.renderWindow.renderStats.maxFrameTime)
    }


    private fun getPosition(): String {
        return "${formatCoordinate(camera.entity.position.x)} / ${formatCoordinate(camera.entity.position.y)} / ${formatCoordinate(camera.entity.position.z)}"
    }

    private fun getBlockPosition(): String {
        val blockPosition = player.positionInfo.blockPosition
        return "${blockPosition.x} / ${blockPosition.y} / ${blockPosition.z}"
    }

    private fun getChunkLocation(): String {
        val inChunkSectionPosition = player.positionInfo.inChunkSectionPosition
        return "${inChunkSectionPosition.x} ${inChunkSectionPosition.y} ${inChunkSectionPosition.z} in ${player.positionInfo.chunkPosition.x} ${player.positionInfo.sectionHeight} ${player.positionInfo.chunkPosition.y}"
    }

    private fun getFacing(): String {
        val yaw = hudRenderer.renderWindow.inputHandler.camera.entity.rotation.headYaw
        val pitch = hudRenderer.renderWindow.inputHandler.camera.entity.rotation.pitch
        val direction = Directions.byDirection(camera.cameraFront)
        return "${Directions.byDirection(camera.cameraFront).name.lowercase(Locale.getDefault())} ${direction.vector} (${formatRotation(yaw)} / ${formatRotation(pitch)})"
    }

    companion object : HUDRenderBuilder<HUDWorldDebugNode> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("minosoft:world_debug_screen")
        override val DEFAULT_PROPERTIES = HUDElementProperties(
            position = Vec2(-1.0f, 1.0f),
            toggleKeyBinding = KeyBindingsNames.TOGGLE_DEBUG_SCREEN,
            enabled = false,
        )

        override fun build(hudRenderer: HUDRenderer): HUDWorldDebugNode {
            return HUDWorldDebugNode(hudRenderer)
        }

        fun formatCoordinate(coordinate: Double): String {
            return "%.5f".format(coordinate)
        }

        fun formatRotation(rotation: Double): String {
            return "%.1f".format(rotation)
        }
    }
}
