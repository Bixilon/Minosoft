/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.hud

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.config.config.game.elements.ElementsNames
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.hud.atlas.HUDAtlasElement
import de.bixilon.minosoft.gui.rendering.hud.elements.HUDElement
import de.bixilon.minosoft.gui.rendering.hud.elements.debug.HUDSystemDebugElement
import de.bixilon.minosoft.gui.rendering.hud.elements.debug.HUDWorldDebugElement
import de.bixilon.minosoft.gui.rendering.hud.elements.other.CrosshairHUDElement
import de.bixilon.minosoft.gui.rendering.hud.elements.other.HotbarHUDElement
import de.bixilon.minosoft.gui.rendering.hud.elements.primitive.ElementListElement
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.protocol.network.Connection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.json.ResourceLocationJsonMap
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2

class HUDRenderer(val connection: Connection, val renderWindow: RenderWindow) : Renderer {
    val hudScale: HUDScale
        get() {
            return Minosoft.getConfig().config.game.hud.scale
        }
    private val defaultHUDElements: MutableMap<ResourceLocation, HUDElement> = mutableMapOf(
        ElementsNames.HOTBAR_RESOURCE_LOCATION to HotbarHUDElement(this),
        ElementsNames.CROSSHAIR_RESOURCE_LOCATION to CrosshairHUDElement(this),
        ElementsNames.WORLD_DEBUG_SCREEN_RESOURCE_LOCATION to HUDWorldDebugElement(this),
        ElementsNames.SYSTEM_DEBUG_SCREEN_RESOURCE_LOCATION to HUDSystemDebugElement(this),
    )
    val hudElements: MutableMap<HUDElementProperties, HUDElement> = mutableMapOf()
    val hudShader = Shader(ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "rendering/shader/hud_vertex.glsl"), ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "rendering/shader/hud_fragment.glsl"))
    lateinit var hudAtlasElements: Map<ResourceLocation, HUDAtlasElement>
    var lastTickTime = 0L
    var orthographicMatrix: Mat4 = Mat4()
        private set
    var currentHUDMesh: HUDMesh = HUDMesh()


    override fun init() {
        for ((resourceLocation, hudElement) in defaultHUDElements) {
            hudElements[Minosoft.getConfig().config.game.elements.entries[resourceLocation]!!] = hudElement
        }

        hudShader.load(Minosoft.MINOSOFT_ASSETS_MANAGER)

        val hudImages = HUDAtlasElement.deserialize(ResourceLocationJsonMap.create(Minosoft.MINOSOFT_ASSETS_MANAGER.readJsonAsset(ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "mapping/atlas.json"))), connection.version.versionId)
        this.hudAtlasElements = hudImages.second

        renderWindow.textures.textures.addAll(hudImages.first.toList())

        for (element in hudElements.values) {
            element.init()
        }
    }

    override fun postInit() {
        renderWindow.textures.use(hudShader, "textureArray")

        for (element in hudElements.values) {
            element.postInit()
        }
    }

    override fun screenChangeResizeCallback(width: Int, height: Int) {
        orthographicMatrix = glm.ortho(-width / 2f, width / 2f, -height / 2f, height / 2f)
        for (element in hudElements.values) {
            element.screenChangeResizeCallback(width, height)
        }
    }

    override fun draw() {
        if (System.currentTimeMillis() - lastTickTime > ProtocolDefinition.TICK_TIME) {
            prepare()
            lastTickTime = System.currentTimeMillis()
        }

        for ((elementProperties, hudElement) in hudElements) {
            if (!elementProperties.enabled) {
                continue
            }
            hudElement.draw()
        }
        hudShader.use()
        currentHUDMesh.draw()
    }

    fun prepare() {
        currentHUDMesh.unload()
        currentHUDMesh = HUDMesh()
        for ((elementProperties, hudElement) in hudElements) {
            if (!elementProperties.enabled) {
                continue
            }
            val elementListElement = ElementListElement(Vec2(0, 0), 0)

            hudElement.prepare(elementListElement)


            val realScaleFactor = elementProperties.scale * hudScale.scale
            val realSize = Vec2(elementListElement.forceX ?: elementListElement.size.x, elementListElement.forceY ?: elementListElement.size.y) * realScaleFactor

            val elementStart = getRealPosition(realSize, elementProperties, renderWindow.screenDimensions)

            elementListElement.prepareVertices(elementStart, realScaleFactor, currentHUDMesh, orthographicMatrix, 0)
        }
        currentHUDMesh.load()
    }


    private fun getRealPosition(elementSize: Vec2, elementProperties: HUDElementProperties, screenDimensions: Vec2): Vec2 {
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
        return Vec2(x, y)
    }
}
