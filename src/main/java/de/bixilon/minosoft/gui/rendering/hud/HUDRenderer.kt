/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.hud

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.config.config.game.controls.KeyBindingsNames
import de.bixilon.minosoft.config.config.game.elements.ElementsNames
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.hud.atlas.HUDAtlasElement
import de.bixilon.minosoft.gui.rendering.hud.elements.other.CrosshairHUDElement
import de.bixilon.minosoft.gui.rendering.hud.nodes.HUDElement
import de.bixilon.minosoft.gui.rendering.hud.nodes.chat.ChatBoxHUDElement
import de.bixilon.minosoft.gui.rendering.hud.nodes.debug.HUDSystemDebugNode
import de.bixilon.minosoft.gui.rendering.hud.nodes.debug.HUDWorldDebugNode
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.util.abstractions.ScreenResizeCallback
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.MMath
import de.bixilon.minosoft.util.json.ResourceLocationJsonMap.toResourceLocationMap
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec2.Vec2i

class HUDRenderer(val connection: PlayConnection, val renderWindow: RenderWindow) : Renderer, ScreenResizeCallback {
    private val hudElements: MutableMap<ResourceLocation, Pair<HUDElementProperties, HUDElement>> = mutableMapOf()
    private val enabledHUDElement: MutableMap<ResourceLocation, Pair<HUDElementProperties, HUDElement>> = mutableMapOf()
    private val hudShader = Shader(
        vertexPath = ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "rendering/shader/hud_vertex.glsl"),
        fragmentPath = ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "rendering/shader/hud_fragment.glsl"),
    )
    lateinit var hudAtlasElements: Map<ResourceLocation, HUDAtlasElement>
    var orthographicMatrix: Mat4 = Mat4()
        private set
    var currentHUDMesh: HUDMesh = HUDMesh()

    private var hudEnabled = true

    private var forcePrepare = true


    override fun init() {
        hudShader.load(Minosoft.MINOSOFT_ASSETS_MANAGER)

        val hudImages = HUDAtlasElement.deserialize(Minosoft.MINOSOFT_ASSETS_MANAGER.readJsonAsset(ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "mapping/atlas.json")).toResourceLocationMap())
        this.hudAtlasElements = hudImages.second

        renderWindow.textures.allTextures.addAll(hudImages.first.toList())

        registerDefaultElements()

        renderWindow.registerKeyCallback(KeyBindingsNames.TOGGLE_HUD) { _, _ ->
            hudEnabled = !hudEnabled
        }

        for ((_, element) in hudElements.values) {
            element.init()
        }
    }

    private fun registerDefaultElements() {
        addElement(ElementsNames.CROSSHAIR_RESOURCE_LOCATION, CrosshairHUDElement(this), HUDElementProperties(
            position = Vec2(0.0f, 0.0f),
            xBinding = HUDElementProperties.PositionBindings.CENTER,
            yBinding = HUDElementProperties.PositionBindings.CENTER,
        ))

        addElement(ElementsNames.WORLD_DEBUG_SCREEN_RESOURCE_LOCATION, HUDWorldDebugNode(this), HUDElementProperties(
            position = Vec2(-1.0f, 1.0f),
            toggleKeyBinding = KeyBindingsNames.TOGGLE_DEBUG_SCREEN,
            enabled = false,
        ))
        addElement(ElementsNames.SYSTEM_DEBUG_SCREEN_RESOURCE_LOCATION, HUDSystemDebugNode(this), HUDElementProperties(
            position = Vec2(1.0f, 1.0f),
            toggleKeyBinding = KeyBindingsNames.TOGGLE_DEBUG_SCREEN,
            enabled = false,
        ))
        addElement(ElementsNames.CHAT_RESOURCE_LOCATION, ChatBoxHUDElement(this), HUDElementProperties(
            position = Vec2(0.0f, -1.0f),
            xBinding = HUDElementProperties.PositionBindings.CENTER,
        ))

    }

    fun addElement(resourceLocation: ResourceLocation, hudElement: HUDElement, defaultProperties: HUDElementProperties) {
        var needToSafeConfig = false
        val properties = Minosoft.getConfig().config.game.elements.entries.getOrPut(resourceLocation) {
            needToSafeConfig = true
            defaultProperties
        }
        if (needToSafeConfig) {
            Minosoft.getConfig().saveToFile()
        }
        if (hudElement is ScreenResizeCallback) {
            renderWindow.screenResizeCallbacks.add(hudElement)
        }
        val pair = Pair(properties, hudElement)
        hudElements[resourceLocation] = pair


        properties.toggleKeyBinding?.let {
            // register key binding
            renderWindow.registerKeyCallback(it) { _, _ ->
                if (enabledHUDElement.contains(resourceLocation)) {
                    enabledHUDElement.remove(resourceLocation)
                } else {
                    enabledHUDElement[resourceLocation] = pair
                }
                forcePrepare = true
            }
        }

        if (properties.enabled) {
            enabledHUDElement[resourceLocation] = pair
            forcePrepare = true
        }
    }

    fun removeElement(resourceLocation: ResourceLocation) {
        val element = hudElements[resourceLocation] ?: return

        element.second.let {
            if (it is ScreenResizeCallback) {
                renderWindow.screenResizeCallbacks.remove(it)
            }
        }

        element.first.toggleKeyBinding?.let {
            renderWindow.unregisterKeyBinding(it)
        }
        enabledHUDElement.remove(resourceLocation)
        hudElements.remove(resourceLocation)
        forcePrepare = true
    }

    override fun postInit() {
        for ((_, element) in hudAtlasElements) {
            element.postInit()
        }

        renderWindow.textures.use(hudShader, "textureArray")

        for (element in hudElements.values) {
            element.second.postInit()
        }
    }

    override fun onScreenResize(screenDimensions: Vec2i) {
        orthographicMatrix = glm.ortho(-screenDimensions.x / 2.0f, screenDimensions.x / 2.0f, -screenDimensions.y / 2.0f, screenDimensions.y / 2.0f)
        for ((_, hudElement) in hudElements.values) {
            hudElement.layout.clearChildrenCache()
        }
    }

    override fun draw() {
        if (!RenderConstants.RENDER_HUD) {
            return
        }

        if (!hudEnabled) {
            return
        }
        var needsUpdate = false
        val tempMesh = HUDMesh()

        for ((_, hudElement) in enabledHUDElement.values) {
            hudElement.draw()

            if (hudElement.layout.needsCacheUpdate()) {
                needsUpdate = true
                // break
            }
        }

        if (forcePrepare || needsUpdate) {
            for ((elementProperties, hudElement) in enabledHUDElement.values) {
                val realScaleFactor = elementProperties.scale * Minosoft.getConfig().config.game.hud.scale
                val realSize = hudElement.layout.sizing.currentSize * realScaleFactor
                realSize.x = MMath.clamp(realSize.x, hudElement.layout.sizing.minSize.x, hudElement.layout.sizing.maxSize.x)
                realSize.y = MMath.clamp(realSize.y, hudElement.layout.sizing.minSize.y, hudElement.layout.sizing.maxSize.y)

                val elementStart = getRealPosition(realSize, elementProperties, renderWindow.screenDimensions)

                hudElement.layout.checkCache(elementStart, realScaleFactor, orthographicMatrix, 0)
                tempMesh.addCacheMesh(hudElement.layout.cache)
            }
            currentHUDMesh.unload(false)
            tempMesh.load()
            currentHUDMesh = tempMesh
        }
        hudShader.use()
        currentHUDMesh.draw()
    }


    private fun getRealPosition(elementSize: Vec2i, elementProperties: HUDElementProperties, screenDimensions: Vec2i): Vec2i {
        val halfScreenDimensions = screenDimensions / 2
        val halfElementSize = elementSize / 2
        val realPosition = elementProperties.position * halfScreenDimensions

        var x = realPosition.x
        var y = realPosition.y
        if (elementProperties.xBinding == HUDElementProperties.PositionBindings.FURTHEST_POINT_AWAY) {
            if (elementProperties.position.x >= 0) {
                x -= elementSize.x
            }
        } else {
            x -= halfElementSize.x
        }

        if (elementProperties.yBinding == HUDElementProperties.PositionBindings.FURTHEST_POINT_AWAY) {
            if (elementProperties.position.y < 0) {
                y += elementSize.y
            }
        } else {
            y += halfElementSize.y
        }
        return Vec2i(x, y)
    }
}
