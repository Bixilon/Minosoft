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
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.hud.atlas.HUDAtlasElement
import de.bixilon.minosoft.gui.rendering.hud.elements.HUDElement
import de.bixilon.minosoft.gui.rendering.hud.elements.other.CrosshairHUDElement
import de.bixilon.minosoft.gui.rendering.hud.elements.other.HotbarHUDElement
import de.bixilon.minosoft.gui.rendering.hud.elements.text.HUDTextElement
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.textures.TextureArray
import de.bixilon.minosoft.protocol.network.Connection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.json.ResourceLocationJsonMap
import glm_.glm
import glm_.mat4x4.Mat4

class HUDRenderer(val connection: Connection, val renderWindow: RenderWindow) : Renderer {
    val hudScale: HUDScale
        get() {
            return Minosoft.getConfig().config.game.hud.scale
        }
    val hudElements: MutableMap<ResourceLocation, HUDElement> = mutableMapOf(
        ResourceLocation("minosoft:hud_text_renderer") to HUDTextElement(connection, this, renderWindow),
        ResourceLocation("minosoft:hotbar") to HotbarHUDElement(this),
        ResourceLocation("minosoft:crosshair") to CrosshairHUDElement(this),
    )
    val hudShader = Shader(ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "rendering/shader/hud_vertex.glsl"), ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "rendering/shader/hud_fragment.glsl"))
    lateinit var hudAtlasTextures: TextureArray
    lateinit var hudAtlasElements: Map<ResourceLocation, HUDAtlasElement>
    var lastTickTime = 0L
    var orthographicMatrix: Mat4 = Mat4()
        private set
    var currentMesh: HUDMesh = HUDMesh()


    override fun init() {
        hudShader.load(Minosoft.MINOSOFT_ASSETS_MANAGER)

        val hudImages = HUDAtlasElement.deserialize(ResourceLocationJsonMap.create(Minosoft.MINOSOFT_ASSETS_MANAGER.readJsonAsset(ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "mapping/atlas.json"))), connection.version.versionId)
        this.hudAtlasElements = hudImages.second

        hudAtlasTextures = TextureArray.createTextureArray(connection.version.assetsManager, hudImages.first.toSet())
        hudAtlasTextures.load()

        for (element in hudElements.values) {
            element.init()
        }
    }

    override fun postInit() {
        hudAtlasTextures.use(hudShader, "hudTextureArray")
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
            update()
            lastTickTime = System.currentTimeMillis()
        }

        for (element in hudElements.values) {
            if (!element.elementProperties.enabled) {
                continue
            }
            element.draw()
        }
        hudShader.use()
        currentMesh.draw()
    }

    fun prepare() {
        currentMesh.unload()
        currentMesh = HUDMesh()
        for (element in hudElements.values) {
            if (!element.elementProperties.enabled) {
                continue
            }
            element.prepare(currentMesh)
        }
        currentMesh.load()
    }

    fun update() {
        for (element in hudElements.values) {
            if (!element.elementProperties.enabled) {
                continue
            }
            element.update()
        }
    }
}
