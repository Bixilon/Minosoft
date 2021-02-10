package de.bixilon.minosoft.gui.rendering.hud

import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.font.Font
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
    private val hudScale = HUDScale.MEDIUM
    var fps: Int = 0
    var frame = 0
    private lateinit var fontShader: Shader
    private lateinit var fontAtlasTexture: TextureArray
    private lateinit var hudMeshHUD: HUDFontMesh


    override fun init(connection: Connection) {
        font.load(connection.version.assetsManager)
        fontAtlasTexture = font.createAtlasTexture()
        fontAtlasTexture.load()

        fontShader = Shader("font_vertex.glsl", "font_fragment.glsl")
        fontShader.load()
        hudMeshHUD = HUDFontMesh(floatArrayOf())
    }

    fun drawChatComponent(position: Vec2, text: ChatComponent) {
        hudMeshHUD.unload()
        val data: MutableList<Float> = mutableListOf()
        text.addVerticies(position, Vec2(0, 0), font, hudScale, data)
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
            drawChatComponent(Vec2(0, 0), ChatComponent.valueOf("§6FPS:§e$fps"))
        }

        hudMeshHUD.draw()
    }
}
