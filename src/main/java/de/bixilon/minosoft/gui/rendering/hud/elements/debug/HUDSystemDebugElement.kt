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

import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.hud.elements.HUDElement
import de.bixilon.minosoft.gui.rendering.hud.elements.primitive.TextElement
import de.bixilon.minosoft.modding.loading.ModLoader
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.GitInfo
import de.bixilon.minosoft.util.SystemInformation
import de.bixilon.minosoft.util.UnitFormatter
import glm_.vec2.Vec2
import org.lwjgl.opengl.GL11.*


class HUDSystemDebugElement(hudRenderer: HUDRenderer) : HUDElement(hudRenderer) {
    private lateinit var gpuText: String
    private lateinit var gpuVersionText: String


    override fun init() {
        gpuText = glGetString(GL_RENDERER) ?: "unknown"
        gpuVersionText = glGetString(GL_VERSION) ?: "unknown"
    }

    private var lastPrepareTime = 0L

    override fun draw() {
        if (System.currentTimeMillis() - lastPrepareTime < ProtocolDefinition.TICK_TIME) {
            return
        }
        layout.clear()

        for (text in listOf(
            "Java: ${Runtime.version()} ${System.getProperty("sun.arch.data.model")}bit",
            "Memory: ${getUsedMemoryPercent()}% ${getFormattedUsedMemory()}/${SystemInformation.MAX_MEMORY_TEXT}",
            "Allocated: ${getAllocatedMemoryPercent()}% ${getFormattedAllocatedMemory()}",
            "System: ${SystemInformation.SYSTEM_MEMORY_TEXT}",
            "",
            "OS: ${SystemInformation.OS_TEXT}",
            "CPU: ${SystemInformation.PROCESSOR_TEXT}",
            "",
            "Display: ${getScreenDimensions()}",
            "GPU: $gpuText",
            "Version: $gpuVersionText",
            "",
            if (GitInfo.IS_INITIALIZED) {
                "Commit: ${GitInfo.GIT_COMMIT_ID_DESCRIBE}: ${GitInfo.GIT_COMMIT_MESSAGE_SHORT}"
            } else {
                "GitInfo uninitialized :("
            },
            "",
            "Mods: ${ModLoader.MOD_MAP.size} active, ${hudRenderer.connection.eventListenerSize} listeners",
        )) {
            val textElement = TextElement(ChatComponent.valueOf(text), hudRenderer.renderWindow.font, Vec2(2, layout.size.y + RenderConstants.TEXT_LINE_PADDING))
            layout.addChild(textElement)
        }
        layout.pushChildrenToRight(1.0f)
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
        return "${hudRenderer.renderWindow.screenWidth}x${hudRenderer.renderWindow.screenHeight}"
    }
}
