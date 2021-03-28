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

import de.bixilon.minosoft.gui.rendering.hud.HUDRenderer
import de.bixilon.minosoft.modding.loading.ModLoader
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.GitInfo
import de.bixilon.minosoft.util.SystemInformation
import de.bixilon.minosoft.util.UnitFormatter
import glm_.vec2.Vec2i
import org.lwjgl.opengl.GL11.*


class HUDSystemDebugElement(hudRenderer: HUDRenderer) : DebugScreen(hudRenderer) {

    init {
        text("Java: ${Runtime.version()} ${System.getProperty("sun.arch.data.model")}bit")
    }

    private val memoryText = text("TBA")
    private val allocatedMemoryText = text("TBA")

    init {
        text("System: ${SystemInformation.SYSTEM_MEMORY_TEXT}")
        text()
        text("OS: ${SystemInformation.OS_TEXT}")
        text("CPU: ${SystemInformation.PROCESSOR_TEXT}")
        text()
    }

    private val displayText = text("TBA")
    private val gpuText = text("TBA")
    private val gpuVersionText = text("TBA")


    init {
        text()
        text(
            if (GitInfo.IS_INITIALIZED) {
                "Commit: ${GitInfo.GIT_COMMIT_ID_DESCRIBE}: ${GitInfo.GIT_COMMIT_MESSAGE_SHORT}"
            } else {
                "GitInfo uninitialized :("
            })
        text()
        text("Mods: ${ModLoader.MOD_MAP.size} active, ${hudRenderer.connection.eventListenerSize} listeners")
    }

    override fun screenChangeResizeCallback(screenDimensions: Vec2i) {
        displayText.sText = "Display: ${getScreenDimensions()}"
        layout.pushChildrenToRight(1)
    }

    override fun init() {
        gpuText.sText = "GPU: " + (glGetString(GL_RENDERER) ?: "unknown")
        gpuVersionText.sText = "Version: " + (glGetString(GL_VERSION) ?: "unknown")
    }

    override fun draw() {
        if (System.currentTimeMillis() - lastPrepareTime < ProtocolDefinition.TICK_TIME * 2) {
            return
        }
        memoryText.sText = "Memory: ${getUsedMemoryPercent()}% ${getFormattedUsedMemory()}/${SystemInformation.MAX_MEMORY_TEXT}"
        allocatedMemoryText.sText = "Allocated: ${getAllocatedMemoryPercent()}% ${getFormattedAllocatedMemory()}"
        layout.pushChildrenToRight(1)

        lastPrepareTime = System.currentTimeMillis()
    }

    private fun getUsedMemory(): Long {
        return SystemInformation.RUNTIME.totalMemory() - SystemInformation.RUNTIME.freeMemory()
    }

    private fun getFormattedUsedMemory(): String {
        return UnitFormatter.formatBytes(getUsedMemory())
    }

    private fun getAllocatedMemory(): Long {
        return SystemInformation.RUNTIME.totalMemory()
    }

    private fun getFormattedAllocatedMemory(): String {
        return UnitFormatter.formatBytes(getAllocatedMemory())
    }

    private fun getUsedMemoryPercent(): Long {
        return getUsedMemory() * 100 / SystemInformation.RUNTIME.maxMemory()
    }

    private fun getAllocatedMemoryPercent(): Long {
        return getAllocatedMemory() * 100 / SystemInformation.RUNTIME.maxMemory()
    }

    private fun getScreenDimensions(): String {
        return "${hudRenderer.renderWindow.screenDimensions.x}x${hudRenderer.renderWindow.screenDimensions.y}"
    }
}
