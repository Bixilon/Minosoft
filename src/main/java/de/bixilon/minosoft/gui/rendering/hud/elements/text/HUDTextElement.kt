package de.bixilon.minosoft.gui.rendering.hud.elements.text

import de.bixilon.minosoft.data.mappings.ModIdentifier
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.font.Font
import de.bixilon.minosoft.gui.rendering.font.FontBindings
import de.bixilon.minosoft.gui.rendering.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.hud.elements.HUDElement
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.textures.TextureArray
import de.bixilon.minosoft.protocol.network.Connection
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import org.lwjgl.opengl.GL13.GL_TEXTURE0

class HUDTextElement(val connection: Connection, val hudRenderer: HUDRenderer, val renderWindow: RenderWindow) : HUDElement {
    private val fontBindingPerspectiveMatrices = mutableListOf(Mat4(), Mat4(), Mat4(), Mat4()) // according to FontBindings::ordinal
    private lateinit var fontShader: Shader
    private lateinit var hudMeshHUD: HUDFontMesh
    private lateinit var fontAtlasTexture: TextureArray
    private val font = Font()
    private lateinit var componentsBindingMap: Map<FontBindings, MutableList<Any>>

    var hudTextElements: MutableMap<ModIdentifier, HUDText> = mutableMapOf(
        ModIdentifier("minosoft:debug_screen") to HUDDebugScreenElement(this),
        ModIdentifier("minosoft:chat") to HUDChatElement(this),
    )

    override fun screenChangeResizeCallback(width: Int, height: Int) {
        fontShader.use()

        fontBindingPerspectiveMatrices[FontBindings.LEFT_UP.ordinal] = glm.ortho(0.0f, width.toFloat(), height.toFloat(), 0.0f)
        fontBindingPerspectiveMatrices[FontBindings.RIGHT_UP.ordinal] = glm.ortho(width.toFloat(), 0.0f, height.toFloat(), 0.0f)
        fontBindingPerspectiveMatrices[FontBindings.RIGHT_DOWN.ordinal] = glm.ortho(width.toFloat(), 0.0f, 0.0f, height.toFloat())
        fontBindingPerspectiveMatrices[FontBindings.LEFT_DOWN.ordinal] = glm.ortho(0.0f, width.toFloat(), 0.0f, height.toFloat())

        prepare()
    }

    private fun drawTextBackground(start: Vec2, end: Vec2, perspectiveMatrix: Mat4, meshData: MutableList<Float>) {
        fun drawLetterVertex(position: Vec2) {
            val matrixPosition = perspectiveMatrix * Vec4(position.x, position.y, 0f, 1f)
            meshData.add(matrixPosition.x)
            meshData.add(matrixPosition.y)
            meshData.add(-0.995f)

            meshData.add(0f)
            meshData.add(0f)
            meshData.add(Float.fromBits(0))

            meshData.add(Float.fromBits(RGBColor(0, 0, 0, 76).color))
        }

        drawLetterVertex(start)
        drawLetterVertex(Vec2(end.x, start.y))
        drawLetterVertex(Vec2(start.x, end.y))
        drawLetterVertex(Vec2(start.x, end.y))
        drawLetterVertex(Vec2(end.x, start.y))
        drawLetterVertex(end)

    }

    fun drawChatComponent(position: Vec2, binding: FontBindings, text: ChatComponent, meshData: MutableList<Float>, maxSize: Vec2) {
        if (text.message.isBlank()) {
            maxSize += Vec2(0, font.charHeight * hudRenderer.hudScale.scale)
            return
        }
        text.addVerticies(position, Vec2(0), fontBindingPerspectiveMatrices[binding.ordinal], binding, font, hudRenderer.hudScale, meshData, maxSize)

        drawTextBackground(position - 1, (position + maxSize) + 1, fontBindingPerspectiveMatrices[binding.ordinal], meshData)
    }

    override fun prepare() {
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

        val meshData: MutableList<Float> = mutableListOf()

        for ((binding, components) in componentsBindingMap) {
            val offset = Vec2(3, 3)

            if (binding == FontBindings.RIGHT_DOWN || binding == FontBindings.LEFT_DOWN) {
                components.reverse()
            }

            for ((_, component) in components.withIndex()) {
                val currentOffset = Vec2()
                drawChatComponent(offset, binding, ChatComponent.valueOf(component), meshData, currentOffset)
                offset += Vec2(0, currentOffset.y + 1)
            }
        }
        hudMeshHUD.unload()
        hudMeshHUD = HUDFontMesh(meshData.toFloatArray())
    }

    override fun init() {
        font.load(connection.version.assetsManager)
        fontShader = Shader("font_vertex.glsl", "font_fragment.glsl")
        fontShader.load()
        hudMeshHUD = HUDFontMesh(floatArrayOf())


        fontAtlasTexture = font.createAtlasTexture()
        fontAtlasTexture.load()

        for (hudTextElement in hudTextElements.values) {
            hudTextElement.init()
        }
    }

    override fun draw() {
        fontAtlasTexture.use(GL_TEXTURE0)
        fontShader.use()

        for (hudTextElement in hudTextElements.values) {
            hudTextElement.draw()
        }
        hudMeshHUD.draw()
    }
}
