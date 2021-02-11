package de.bixilon.minosoft.gui.rendering.hud

import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.rendering.RenderWindow
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

class HUDRenderer(private val connection: Connection, private val renderWindow: RenderWindow) : Renderer {
    private val font = Font()
    private val hudScale = HUDScale.MEDIUM
    var fps: Int = 0
    var frame = 0
    private lateinit var fontShader: Shader
    private lateinit var fontAtlasTexture: TextureArray
    private lateinit var hudMeshHUD: HUDFontMesh
    private var screenWidth = 0
    private var screenHeight = 0


    override fun init(connection: Connection) {
        font.load(connection.version.assetsManager)
        fontAtlasTexture = font.createAtlasTexture()
        fontAtlasTexture.load()

        fontShader = Shader("font_vertex.glsl", "font_fragment.glsl")
        fontShader.load()
        hudMeshHUD = HUDFontMesh(floatArrayOf())
    }

    fun drawChatComponent(position: Vec2, text: ChatComponent, meshData: MutableList<Float>, maxSize: Vec2) {
        hudMeshHUD.unload()
        text.addVerticies(position, Vec2(0, 0), font, hudScale, meshData, maxSize)
    }

    fun screenChangeResizeCallback(width: Int, height: Int) {
        fontShader.use()
        screenWidth = width
        screenHeight = height

        fontShader.setMat4("projectionMatrix", glm.ortho(0.0f, width.toFloat(), height.toFloat(), 0.0f))
        prepare()
    }

    override fun draw() {
        fontAtlasTexture.use(GL_TEXTURE0)

        fontShader.use()
        glDisable(GL_DEPTH_TEST)

        frame++
        if (frame % 15 == 0) {
            prepare()
        }

        hudMeshHUD.draw()
    }

    fun prepare() {
        val runtime = Runtime.getRuntime()!!
        val meshData: MutableList<Float> = mutableListOf()
        val components: List<ChatComponent> = mutableListOf(
            ChatComponent.valueOf("§fMinosoft: §eUnreleased version (pre beta 1)"),
            ChatComponent.valueOf("§fFPS: §e$fps"),
            ChatComponent.valueOf("§fRAM: §c${(runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)}M (${runtime.totalMemory() / (1024 * 1024)}M) / ${runtime.maxMemory() / (1024 * 1024)}M"),
            ChatComponent.valueOf("§fXYZ §6${renderWindow.camera.cameraPosition.x} / ${renderWindow.camera.cameraPosition.y} / ${renderWindow.camera.cameraPosition.z}"),
            ChatComponent.valueOf("§fConnected to: §a${connection.address}"),
        )
        val offset = Vec2(3, 3)
        for ((_, component) in components.withIndex()) {
            val currentOffset = Vec2()
            drawChatComponent(offset, component, meshData, currentOffset)
            offset += Vec2(0, currentOffset.y + 1)
        }
        hudMeshHUD = HUDFontMesh(meshData.toFloatArray())
    }
}
