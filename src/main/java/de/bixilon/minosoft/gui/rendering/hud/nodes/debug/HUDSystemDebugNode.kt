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
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.rendering.hud.HUDElementProperties
import de.bixilon.minosoft.gui.rendering.hud.HUDRenderBuilder
import de.bixilon.minosoft.gui.rendering.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.hud.nodes.primitive.LabelNode
import de.bixilon.minosoft.gui.rendering.hud.nodes.properties.NodeAlignment
import de.bixilon.minosoft.gui.rendering.input.camera.hit.BlockRaycastHit
import de.bixilon.minosoft.gui.rendering.input.camera.hit.EntityRaycastHit
import de.bixilon.minosoft.gui.rendering.input.camera.hit.FluidRaycastHit
import de.bixilon.minosoft.gui.rendering.input.camera.hit.RaycastHit
import de.bixilon.minosoft.gui.rendering.modding.events.ResizeWindowEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.modding.loading.ModLoader
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.GitInfo
import de.bixilon.minosoft.util.SystemInformation
import de.bixilon.minosoft.util.UnitFormatter.formatBytes
import glm_.vec2.Vec2


class HUDSystemDebugNode(hudRenderer: HUDRenderer) : DebugScreenNode(hudRenderer) {

    init {
        layout.sizing.forceAlign = NodeAlignment.RIGHT
        layout.sizing.padding.top = 2
        layout.sizing.padding.right = 2
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

    private val gpuMemoryText = text("TBA")


    init {
        text()
        text(
            if (GitInfo.IS_INITIALIZED) {
                "Commit: ${GitInfo.GIT_COMMIT_ID_DESCRIBE}: ${GitInfo.GIT_COMMIT_MESSAGE_SHORT}"
            } else {
                "GitInfo uninitialized :("
            })
        text()
        text("Mods: ${ModLoader.MOD_MAP.size} active, ${hudRenderer.connection.size} listeners")
    }

    init {
        text()
    }

    private val target = text("TBA")

    init {
        text()
    }

    private val targetBlock = text()

    init {
        text()
    }

    private val targetFluid = text()

    init {
        text()
    }

    private val targetEntity = text()

    override fun init() {
        gpuText.sText = "GPU: " + hudRenderer.renderWindow.renderSystem.gpuType
        gpuVersionText.sText = "Version: " + hudRenderer.renderWindow.renderSystem.version

        hudRenderer.connection.registerEvent(CallbackEventInvoker.of<ResizeWindowEvent> {
            displayText.sText = "Display: ${getScreenDimensions()}"
        })
    }

    override fun draw() {
        if (System.currentTimeMillis() - lastPrepareTime < ProtocolDefinition.TICK_TIME * 2) {
            return
        }
        memoryText.sText = "Memory: ${getUsedMemoryPercent()}% ${getFormattedUsedMemory()}/${SystemInformation.MAX_MEMORY_TEXT}"
        allocatedMemoryText.sText = "Allocated: ${getAllocatedMemoryPercent()}% ${getFormattedAllocatedMemory()}"

        gpuMemoryText.sText = "VRAM: ${hudRenderer.renderWindow.renderSystem.usedVRAM.formatBytes()} / ${hudRenderer.renderWindow.renderSystem.availableVRAM.formatBytes()} | ${hudRenderer.renderWindow.renderSystem.maximumVRAM.formatBytes()}"


        val target = hudRenderer.renderWindow.inputHandler.camera.target

        val targetClass = target?.let { it::class.java }
        this.target.text = target?.let {
            val targetType = when (targetClass) {
                FluidRaycastHit::class.java -> "Fluid"
                BlockRaycastHit::class.java -> "Block"
                EntityRaycastHit::class.java -> "Entity"
                else -> "Unknown"
            }
            BaseComponent("Target: ", targetType, ": ", it)
        } ?: ChatComponent.of("No target!")

        if (target != null) {
            fun addTarget(labelNode: LabelNode, raycastHit: RaycastHit?, name: String) {
                labelNode.text = if (raycastHit != null && targetClass != raycastHit::class.java) {
                    BaseComponent("$name target: ", raycastHit)
                } else {
                    ChatComponent.of()
                }
            }

            val camera = hudRenderer.renderWindow.inputHandler.camera
            addTarget(this.targetBlock, camera.blockTarget, "Block")
            addTarget(this.targetFluid, camera.fluidTarget, "Fluid")
            addTarget(this.targetEntity, camera.entityTarget, "Entity")
        }
        lastPrepareTime = System.currentTimeMillis()
    }

    private fun getUsedMemory(): Long {
        return SystemInformation.RUNTIME.totalMemory() - SystemInformation.RUNTIME.freeMemory()
    }

    private fun getFormattedUsedMemory(): String {
        return getUsedMemory().formatBytes()
    }

    private fun getAllocatedMemory(): Long {
        return SystemInformation.RUNTIME.totalMemory()
    }

    private fun getFormattedAllocatedMemory(): String {
        return getAllocatedMemory().formatBytes()
    }

    private fun getUsedMemoryPercent(): Long {
        return getUsedMemory() * 100 / SystemInformation.RUNTIME.maxMemory()
    }

    private fun getAllocatedMemoryPercent(): Long {
        return getAllocatedMemory() * 100 / SystemInformation.RUNTIME.maxMemory()
    }

    private fun getScreenDimensions(): String {
        return "${hudRenderer.renderWindow.window.size.x}x${hudRenderer.renderWindow.window.size.y}"
    }

    companion object : HUDRenderBuilder<HUDSystemDebugNode> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("minosoft:system_debug_screen")
        override val DEFAULT_PROPERTIES = HUDElementProperties(
            position = Vec2(1.0f, 1.0f),
            toggleKeyBinding = KeyBindingsNames.TOGGLE_DEBUG_SCREEN,
            enabled = false,
        )

        override fun build(hudRenderer: HUDRenderer): HUDSystemDebugNode {
            return HUDSystemDebugNode(hudRenderer)
        }
    }
}
