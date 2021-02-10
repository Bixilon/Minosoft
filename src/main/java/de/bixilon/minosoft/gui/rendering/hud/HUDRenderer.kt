package de.bixilon.minosoft.gui.rendering.hud

import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.font.Font
import de.bixilon.minosoft.gui.rendering.font.Font2DMesh
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
    val font = Font()
    val hudScale = HUDScale.LARGE
    var fps: Int = 0
    private lateinit var fontShader: Shader
    private lateinit var fontAtlasTexture: TextureArray
    lateinit var hudMesh: Font2DMesh


    override fun init(connection: Connection) {
        font.load(connection.version.assetsManager)
        fontAtlasTexture = font.createAtlasTexture()
        fontAtlasTexture.load()

        fontShader = Shader("font_vertex.glsl", "font_fragment.glsl")
        fontShader.load()
    }


    fun drawLetterVertex(position: Vec2, uv: Vec2, atlasPage: Int, meshData: MutableList<Float>) {
        meshData.add(position.x)
        meshData.add(position.y)
        meshData.add(uv.x)
        meshData.add(uv.y)
        meshData.add(atlasPage.toFloat())
    }

    fun drawLetter(position: Vec2, scaledX: Float, fontChar: FontChar, meshData: MutableList<Float>) {
        val scaledHeight = fontChar.height * hudScale.scale
        drawLetterVertex(Vec2(position.x, position.y), fontChar.uvLeftDown, fontChar.atlasTextureIndex, meshData)
        drawLetterVertex(Vec2(position.x, position.y + scaledHeight), fontChar.uvLeftUp, fontChar.atlasTextureIndex, meshData)
        drawLetterVertex(Vec2(position.x + scaledX, position.y), fontChar.uvRightDown, fontChar.atlasTextureIndex, meshData)
        drawLetterVertex(Vec2(position.x + scaledX, position.y), fontChar.uvRightDown, fontChar.atlasTextureIndex, meshData)
        drawLetterVertex(Vec2(position.x, position.y + scaledHeight), fontChar.uvLeftUp, fontChar.atlasTextureIndex, meshData)
        drawLetterVertex(Vec2(position.x + scaledX, position.y + scaledHeight), fontChar.uvRightUp, fontChar.atlasTextureIndex, meshData)
    }

    fun drawString(position: Vec2, text: String) {
        if (this::hudMesh.isInitialized) {
            hudMesh.unload()
        }

        val data: MutableList<Float> = mutableListOf()
        val chars = text.toCharArray()
        var xOffset = position.x

        for (char in chars) {
            val fontChar = font.getChar(char)
            val scaledX = fontChar.endPixel * hudScale.scale
            drawLetter(Vec2(xOffset, position.y), scaledX, fontChar, data)
            xOffset += scaledX
        }

        hudMesh = Font2DMesh(data.toFloatArray())

    }

    fun screenChangeResizeCallback(width: Int, height: Int) {
        fontShader.use()

        fontShader.setMat4("projectionMatrix", glm.ortho(0.0f, width.toFloat(), 0.0f, height.toFloat()))
    }

    override fun draw() {
        fontAtlasTexture.use(GL_TEXTURE0)

        fontShader.use()
        glDisable(GL_DEPTH_TEST)

        drawString(Vec2(100, 100), "FPS: $fps")

        hudMesh.draw()
    }
}
