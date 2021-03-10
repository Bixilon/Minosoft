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
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.hud.elements.HUDElement
import de.bixilon.minosoft.gui.rendering.hud.elements.primitive.TextElement
import glm_.vec2.Vec2


class HUDWorldDebugElement(hudRenderer: HUDRenderer) : HUDElement(hudRenderer) {
    private val camera = hudRenderer.renderWindow.camera

    override fun draw() {
        elementList.clear()

        for (text in listOf(
            "FPS: ${getFPS()}",
            "Timings: avg ${getAvgFrameTime()}ms, min ${getMinFrameTime()}ms, max ${getMaxFrameTime()}ms",
            "Chunks: q=${hudRenderer.renderWindow.worldRenderer.queuedChunks.size} v=${hudRenderer.renderWindow.worldRenderer.visibleChunks.size} p=${hudRenderer.renderWindow.worldRenderer.chunkSectionsToDraw.size} t=${hudRenderer.connection.player.world.chunks.size}",
            "Connected to ${hudRenderer.connection.address} on ${hudRenderer.connection.version} with ${hudRenderer.connection.player.account.username}",
            "",
            "XYZ ${getLocation()}",
            "Block ${getBlockPosition()}",
            "Chunk ${getChunkLocation()}",
            "Facing: ${getFacing()}",
            "Gamemode: ${hudRenderer.connection.player.gamemode?.name?.toLowerCase()}",
            "Dimension: ${hudRenderer.connection.player.world.dimension}",
            "Biome: ${camera.currentBiome}",
            "",
            "Difficulty: ${hudRenderer.connection.player.world.difficulty?.name?.toLowerCase()}, ${
                if (hudRenderer.connection.player.world.difficultyLocked) {
                    "locked"
                } else {
                    "unlocked"
                }
            }",
            "Client light: ${hudRenderer.connection.player.world.worldLightAccessor.getLightLevel(camera.blockPosition)} (sky=${hudRenderer.connection.player.world.worldLightAccessor.getSkyLight(camera.blockPosition)}, block=${hudRenderer.connection.player.world.worldLightAccessor.getBlockLight(camera.blockPosition)})"
        )) {
            val textElement = TextElement(ChatComponent.valueOf(text), hudRenderer.renderWindow.font, Vec2(2, elementList.size.y + RenderConstants.TEXT_LINE_PADDING))
            elementList.addChild(textElement)
        }
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


    private fun getLocation(): String {
        return "${formatCoordinate(camera.feetLocation.x)} / ${formatCoordinate(camera.feetLocation.y)} / ${formatCoordinate(camera.feetLocation.z)}"
    }

    private fun getBlockPosition(): String {
        return "${camera.blockPosition.x} / ${camera.blockPosition.y} / ${camera.blockPosition.z}"
    }

    private fun getChunkLocation(): String {
        return "${camera.inChunkSectionPosition.x} ${camera.inChunkSectionPosition.y} ${camera.inChunkSectionPosition.z} in ${camera.chunkPosition.x} ${camera.sectionHeight} ${camera.chunkPosition.z}"
    }

    private fun getFacing(): String {
        val yaw = hudRenderer.renderWindow.camera.yaw
        val pitch = hudRenderer.renderWindow.camera.pitch
        val direction = Directions.byDirection(camera.cameraFront)
        return "${Directions.byDirection(camera.cameraFront).name.toLowerCase()} ${direction.directionVector} (${formatRotation(yaw)} / ${formatRotation(pitch)})"
    }

    companion object {
        fun formatCoordinate(coordinate: Double): String {
            return "%.3f".format(coordinate)
        }

        fun formatRotation(rotation: Double): String {
            return "%.1f".format(rotation)
        }
    }

}
