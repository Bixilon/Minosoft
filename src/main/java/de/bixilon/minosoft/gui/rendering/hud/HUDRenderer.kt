package de.bixilon.minosoft.gui.rendering.hud

import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.font.Font
import de.bixilon.minosoft.gui.rendering.font.FontChar
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.textures.TextureArray
import de.bixilon.minosoft.protocol.network.Connection
import glm_.glm
import glm_.vec2.Vec2
import org.lwjgl.opengl.GL11.GL_DEPTH_TEST
import org.lwjgl.opengl.GL11.glDisable
import org.lwjgl.opengl.GL13.GL_TEXTURE0

class HUDRenderer : Renderer {
    private val font = Font()
    private val hudScale = HUDScale.LARGE
    var fps: Int = 0
    var frame = 0
    private lateinit var fontShader: Shader
    private lateinit var fontAtlasTexture: TextureArray
    lateinit var hudMeshHUD: HUDFontMesh


    override fun init(connection: Connection) {
        font.load(connection.version.assetsManager)
        fontAtlasTexture = font.createAtlasTexture()
        fontAtlasTexture.load()

        fontShader = Shader("font_vertex.glsl", "font_fragment.glsl")
        fontShader.load()
        drawString(Vec2(100, 100), "FPS: $fps")
    }


    fun drawLetterVertex(position: Vec2, uv: Vec2, atlasPage: Int, color: RGBColor, meshData: MutableList<Float>) {
        meshData.add(position.x)
        meshData.add(position.y)
        meshData.add(uv.x)
        meshData.add(uv.y)
        meshData.add(atlasPage.toFloat())
        meshData.add(color.red / 256f)
        meshData.add(color.green / 256f)
        meshData.add(color.blue / 256f)
    }

    fun drawLetter(position: Vec2, scaledX: Float, fontChar: FontChar, color: RGBColor, meshData: MutableList<Float>) {
        val scaledHeight = fontChar.height * hudScale.scale
        drawLetterVertex(Vec2(position.x, position.y), fontChar.uvLeftDown, fontChar.atlasTextureIndex, color, meshData)
        drawLetterVertex(Vec2(position.x, position.y + scaledHeight), fontChar.uvLeftUp, fontChar.atlasTextureIndex, color, meshData)
        drawLetterVertex(Vec2(position.x + scaledX, position.y), fontChar.uvRightDown, fontChar.atlasTextureIndex, color, meshData)
        drawLetterVertex(Vec2(position.x + scaledX, position.y), fontChar.uvRightDown, fontChar.atlasTextureIndex, color, meshData)
        drawLetterVertex(Vec2(position.x, position.y + scaledHeight), fontChar.uvLeftUp, fontChar.atlasTextureIndex, color, meshData)
        drawLetterVertex(Vec2(position.x + scaledX, position.y + scaledHeight), fontChar.uvRightUp, fontChar.atlasTextureIndex, color, meshData)
    }

    fun drawString(position: Vec2, text: String) {
        if (this::hudMeshHUD.isInitialized) {
            hudMeshHUD.unload()
        }

        val data: MutableList<Float> = mutableListOf()
        val chars = text.toCharArray()
        var xOffset = position.x

        for (char in chars) {
            val fontChar = font.getChar(char)
            val scaledX = fontChar.endPixel * hudScale.scale
            drawLetter(Vec2(xOffset, position.y), scaledX, fontChar, ChatColors.getRandomColor(), data)
            xOffset += scaledX
        }

        hudMeshHUD = HUDFontMesh(data.toFloatArray())

    }

    fun screenChangeResizeCallback(width: Int, height: Int) {
        fontShader.use()

        fontShader.setMat4("projectionMatrix", glm.ortho(0.0f, width.toFloat(), 0.0f, height.toFloat()))
    }

    override fun draw() {
        fontAtlasTexture.use(GL_TEXTURE0)

        fontShader.use()
        glDisable(GL_DEPTH_TEST)

        frame++
        if (frame % 30 == 0) {
            drawString(Vec2(100, 100), "FPS: $fps")
        }

        hudMeshHUD.draw()
    }
}
