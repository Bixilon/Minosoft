/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.hud.elements.debug

import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.gui.rendering.hud.HUDRenderer
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.UnitFormatter


class HUDWorldDebugElement(hudRenderer: HUDRenderer) : DebugScreen(hudRenderer) {
    private val camera = hudRenderer.renderWindow.camera

    private val brandText = text("§cMinosoft 0.1-pre1")
    private val fpsText = text("TBA")
    private val timingsText = text("TBA")
    private val chunksText = text("TBA")
    private val openGLText = text("TBA")

    init {
        text("Connected to ${hudRenderer.connection.address} on ${hudRenderer.connection.version} with ${hudRenderer.connection.player.account.username}")
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
        if (System.currentTimeMillis() - lastPrepareTime < ProtocolDefinition.TICK_TIME) {
            return
        }

        fpsText.sText = "FPS: ${getFPS()}"
        chunksText.sText = "Chunks: q=${hudRenderer.renderWindow.worldRenderer.queuedChunks.size} v=${hudRenderer.renderWindow.worldRenderer.visibleChunks.size} p=${hudRenderer.renderWindow.worldRenderer.allChunkSections.size} t=${hudRenderer.connection.player.world.chunks.size}"
        timingsText.sText = "Timings: avg ${getAvgFrameTime()}ms, min ${getMinFrameTime()}ms, max ${getMaxFrameTime()}ms"
        openGLText.sText = "GL: m=${UnitFormatter.formatNumber(hudRenderer.renderWindow.worldRenderer.meshes)} t=${UnitFormatter.formatNumber(hudRenderer.renderWindow.worldRenderer.triangles)}"


        // ToDo: Prepare on change
        gamemodeText.sText = "Gamemode: ${hudRenderer.connection.player.gamemode?.name?.toLowerCase()}"
        positionText.sText = "XYZ ${getPosition()}"
        blockPositionText.sText = "Block ${getVec3i()}"
        chunkPositionText.sText = "Chunk ${getChunkLocation()}"
        facingText.sText = "Facing: ${getFacing()}"

        biomeText.sText = "Biome: ${camera.currentBiome}"
        dimensionText.sText = "Dimension: ${hudRenderer.connection.player.world.dimension}"

        difficultyText.sText = "Difficulty: ${hudRenderer.connection.player.world.difficulty?.name?.toLowerCase()}, ${
            if (hudRenderer.connection.player.world.difficultyLocked) {
                "locked"
            } else {
                "unlocked"
            }
        }"

        lightText.sText = "Client light: ${hudRenderer.connection.player.world.worldLightAccessor.getLightLevel(camera.blockPosition)} (sky=${hudRenderer.connection.player.world.worldLightAccessor.getSkyLight(camera.blockPosition)}, block=${hudRenderer.connection.player.world.worldLightAccessor.getBlockLight(camera.blockPosition)})"


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
        return "${formatCoordinate(camera.cameraPosition.x)} / ${formatCoordinate(camera.cameraPosition.y)} / ${formatCoordinate(camera.cameraPosition.z)}"
    }

    private fun getVec3i(): String {
        return "${camera.blockPosition.x} / ${camera.blockPosition.y} / ${camera.blockPosition.z}"
    }

    private fun getChunkLocation(): String {
        return "${camera.inChunkSectionPosition.x} ${camera.inChunkSectionPosition.y} ${camera.inChunkSectionPosition.z} in ${camera.chunkPosition.x} ${camera.sectionHeight} ${camera.chunkPosition.y}"
    }

    private fun getFacing(): String {
        val yaw = hudRenderer.renderWindow.camera.yaw
        val pitch = hudRenderer.renderWindow.camera.pitch
        val direction = Directions.byDirection(camera.cameraFront)
        return "${Directions.byDirection(camera.cameraFront).name.toLowerCase()} ${direction.directionVector} (${formatRotation(yaw)} / ${formatRotation(pitch)})"
    }

    companion object {
        fun formatCoordinate(coordinate: Float): String {
            return "%.3f".format(coordinate)
        }

        fun formatRotation(rotation: Double): String {
            return "%.1f".format(rotation)
        }
    }

}
