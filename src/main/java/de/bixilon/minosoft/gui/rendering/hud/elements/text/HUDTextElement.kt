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

package de.bixilon.minosoft.gui.rendering.hud.elements.text

import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.font.Font
import de.bixilon.minosoft.gui.rendering.font.FontBindings
import de.bixilon.minosoft.gui.rendering.hud.HUDElementProperties
import de.bixilon.minosoft.gui.rendering.hud.HUDMesh
import de.bixilon.minosoft.gui.rendering.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.hud.elements.HUDElement
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.textures.TextureArray
import de.bixilon.minosoft.protocol.network.Connection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

class HUDTextElement(val connection: Connection, hudRenderer: HUDRenderer, val renderWindow: RenderWindow) : HUDElement(hudRenderer) {
    override val elementProperties: HUDElementProperties = HUDElementProperties(Vec2(), HUDElementProperties.PositionBindings.CENTER, HUDElementProperties.PositionBindings.CENTER, 0f, true)
    private val fontBindingPerspectiveMatrices = mutableListOf(Mat4(), Mat4(), Mat4(), Mat4()) // according to FontBindings::ordinal
    private lateinit var fontShader: Shader
    private lateinit var hudMeshHUD: HUDFontMesh
    private lateinit var fontAtlasTexture: TextureArray
    private val font = Font()
    private lateinit var componentsBindingMap: Map<FontBindings, MutableList<Any>>

    var hudTextElements: MutableMap<ResourceLocation, HUDText> = mutableMapOf(
        ResourceLocation("minosoft:debug_screen") to HUDDebugScreenElement(this),
        ResourceLocation("minosoft:chat") to HUDChatElement(this),
    )

    override fun screenChangeResizeCallback(screenWidth: Int, screenHeight: Int) {
        fontShader.use()

        fontBindingPerspectiveMatrices[FontBindings.LEFT_UP.ordinal] = glm.ortho(0.0f, screenWidth.toFloat(), screenHeight.toFloat(), 0.0f)
        fontBindingPerspectiveMatrices[FontBindings.RIGHT_UP.ordinal] = glm.ortho(screenWidth.toFloat(), 0.0f, screenHeight.toFloat(), 0.0f)
        fontBindingPerspectiveMatrices[FontBindings.RIGHT_DOWN.ordinal] = glm.ortho(screenWidth.toFloat(), 0.0f, 0.0f, screenHeight.toFloat())
        fontBindingPerspectiveMatrices[FontBindings.LEFT_DOWN.ordinal] = glm.ortho(0.0f, screenWidth.toFloat(), 0.0f, screenHeight.toFloat())
    }

    private fun drawTextBackground(start: Vec2, end: Vec2, perspectiveMatrix: Mat4, mesh: HUDFontMesh) {
        fun drawLetterVertex(position: Vec2) {
            val matrixPosition = perspectiveMatrix * Vec4(position.x, position.y, 0f, 1f)
            mesh.addVertex(
                position = Vec3(matrixPosition.x, matrixPosition.y, TEXT_BACKGROUND_Z),
                textureCoordinates = TEXT_BACKGROUND_ATLAS_COORDINATES,
                atlasPage = TEXT_BACKGROUND_ATLAS_INDEX,
                color = TEXT_BACKGROUND_COLOR,
            )
        }

        drawLetterVertex(start)
        drawLetterVertex(Vec2(end.x, start.y))
        drawLetterVertex(Vec2(start.x, end.y))
        drawLetterVertex(Vec2(start.x, end.y))
        drawLetterVertex(Vec2(end.x, start.y))
        drawLetterVertex(end)

    }

    fun drawChatComponent(position: Vec2, binding: FontBindings, text: ChatComponent, mesh: HUDFontMesh, maxSize: Vec2) {
        if (text.message.isBlank()) {
            maxSize += Vec2(0, font.charHeight * hudRenderer.hudScale.scale)
            return
        }
        text.addVerticies(position, Vec2(0), fontBindingPerspectiveMatrices[binding.ordinal], binding, font, hudRenderer.hudScale, mesh, maxSize)

        drawTextBackground(position - 1, (position + maxSize) + 1, fontBindingPerspectiveMatrices[binding.ordinal], mesh)
    }

    override fun prepare(hudMesh: HUDMesh) {
        componentsBindingMap = mapOf(
            FontBindings.LEFT_UP to mutableListOf(
                "§eMinosoft (0.1-pre1)",
            ),
            FontBindings.RIGHT_UP to mutableListOf(),
            FontBindings.RIGHT_DOWN to mutableListOf(),
            FontBindings.LEFT_DOWN to mutableListOf(),
        )
        for (hudTextElement in hudTextElements.values) {
            hudTextElement.prepare(componentsBindingMap)
        }
    }

    override fun update() {
        for (hudTextElement in hudTextElements.values) {
            hudTextElement.update()
        }

        val mesh = HUDFontMesh()

        for ((binding, components) in componentsBindingMap) {
            val offset = Vec2(3, 3)

            if (binding == FontBindings.RIGHT_DOWN || binding == FontBindings.LEFT_DOWN) {
                components.reverse()
            }

            for ((_, component) in components.withIndex()) {
                val currentOffset = Vec2()
                drawChatComponent(offset, binding, ChatComponent.valueOf(component), mesh, currentOffset)
                offset += Vec2(0, currentOffset.y + 1)
            }
        }
        mesh.load()
        hudMeshHUD.unload()
        hudMeshHUD = mesh
    }


    override fun init() {
        font.load(connection.version.assetsManager)

        fontAtlasTexture = font.createAtlasTexture()
        fontAtlasTexture.load()

        hudMeshHUD = HUDFontMesh()
        hudMeshHUD.load()

        fontShader = Shader(ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "rendering/shader/font_vertex.glsl"), ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "rendering/shader/font_fragment.glsl"))
        fontShader.load()


        // fontAtlasTexture.use(fontShader, "textureArray")


        for (hudTextElement in hudTextElements.values) {
            hudTextElement.init()
        }
    }

    override fun postInit() {
        fontAtlasTexture.use(fontShader, "fontTextureArray")
    }

    override fun draw() {
        fontShader.use()

        for (hudTextElement in hudTextElements.values) {
            hudTextElement.draw()
        }
        hudMeshHUD.draw()
    }

    companion object {

        private const val TEXT_BACKGROUND_Z = -0.995f
        private val TEXT_BACKGROUND_ATLAS_COORDINATES = Vec2()
        private const val TEXT_BACKGROUND_ATLAS_INDEX = 0
        private val TEXT_BACKGROUND_COLOR = RGBColor(0, 0, 0, 80)

    }
}
