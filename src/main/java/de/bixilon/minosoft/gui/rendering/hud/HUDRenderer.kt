package de.bixilon.minosoft.gui.rendering.hud

import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.font.Font
import de.bixilon.minosoft.gui.rendering.font.FontBindings
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.textures.TextureArray
import de.bixilon.minosoft.protocol.network.Connection
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import org.lwjgl.opengl.GL11.GL_DEPTH_TEST
import org.lwjgl.opengl.GL11.glDisable
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import java.util.concurrent.ConcurrentLinkedQueue

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
    var chatMessages: ConcurrentLinkedQueue<Pair<ChatComponent, Long>> = ConcurrentLinkedQueue()
    private var showChat = true
    private var showDebugScreen = true
    private val fontBindingPerspectiveMatrices = mutableListOf(Mat4(), Mat4(), Mat4(), Mat4()) // according to FontBindings::ordinal


    override fun init(connection: Connection) {
        font.load(connection.version.assetsManager)
        fontAtlasTexture = font.createAtlasTexture()
        fontAtlasTexture.load()

        fontShader = Shader("font_vertex.glsl", "font_fragment.glsl")
        fontShader.load()
        hudMeshHUD = HUDFontMesh(floatArrayOf())
    }

    fun drawChatComponent(position: Vec2, binding: FontBindings, text: ChatComponent, meshData: MutableList<Float>, maxSize: Vec2) {
        hudMeshHUD.unload()
        text.addVerticies(position, Vec2(0, 0), fontBindingPerspectiveMatrices[binding.ordinal], binding, font, hudScale, meshData, maxSize)
    }

    fun screenChangeResizeCallback(width: Int, height: Int) {
        fontShader.use()
        screenWidth = width
        screenHeight = height

        fontBindingPerspectiveMatrices[FontBindings.LEFT_UP.ordinal] = glm.ortho(0.0f, width.toFloat(), height.toFloat(), 0.0f)
        fontBindingPerspectiveMatrices[FontBindings.RIGHT_UP.ordinal] = glm.ortho(width.toFloat(), 0.0f, height.toFloat(), 0.0f)
        fontBindingPerspectiveMatrices[FontBindings.RIGHT_DOWN.ordinal] = glm.ortho(width.toFloat(), 0.0f, 0.0f, height.toFloat())
        fontBindingPerspectiveMatrices[FontBindings.LEFT_DOWN.ordinal] = glm.ortho(0.0f, width.toFloat(), 0.0f, height.toFloat())

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
        val componentsBindingMap: Map<FontBindings, MutableList<Any>> = mapOf(
            FontBindings.LEFT_UP to mutableListOf(
                "§aMinosoft (0.1-pre1)",
            ),
            FontBindings.RIGHT_UP to mutableListOf(),
            FontBindings.RIGHT_DOWN to mutableListOf(),
            FontBindings.LEFT_DOWN to mutableListOf(),
        )

        if (showDebugScreen) {
            componentsBindingMap[FontBindings.LEFT_UP]!!.addAll(listOf(
                "§fFPS: §8$fps",
                "§fXYZ §8${"%.4f".format(renderWindow.camera.cameraPosition.x)} / ${"%.4f".format(renderWindow.camera.cameraPosition.y)} / ${"%.4f".format(renderWindow.camera.cameraPosition.z)}",
                "§fConnected to: §8${connection.address}",
            ))
            componentsBindingMap[FontBindings.RIGHT_UP]!!.addAll(listOf(
                "§fJava: §8${Runtime.version()} ${System.getProperty("sun.arch.data.model")}bit",
                "§fMemory: §8${runtime.freeMemory() * 100 / runtime.maxMemory()}% ${(runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)}/${runtime.maxMemory() / (1024 * 1024)}MB",
                "§fAllocated: §8${runtime.totalMemory() * 100 / runtime.maxMemory()}% ${runtime.totalMemory() / (1024 * 1024)}MB",
                " ",
                "CPU: §8${runtime.availableProcessors()}x TODO",
                "OS: §8${System.getProperty("os.name")}",
                " ",
                "Display: §8${screenWidth}x${screenHeight}",
            ))
        }
        if (showChat) {
            for (entry in chatMessages) {
                if (System.currentTimeMillis() - entry.second > 10000) {
                    chatMessages.remove(entry)
                    continue
                }
                componentsBindingMap[FontBindings.LEFT_DOWN]!!.add(entry.first)
            }
        }
        for ((binding, components) in componentsBindingMap) {
            val offset = Vec2(3, 3)

            if (binding == FontBindings.RIGHT_DOWN || binding == FontBindings.LEFT_DOWN) {
                components.reverse()
            }

            for ((_, component) in components.withIndex()) {
                val currentOffset = Vec2()
                val chatComponent = if (component is ChatComponent) {
                    component
                } else {
                    ChatComponent.valueOf(component)
                }
                drawChatComponent(offset, binding, chatComponent, meshData, currentOffset)
                offset += Vec2(0, currentOffset.y + 1)
            }
            hudMeshHUD = HUDFontMesh(meshData.toFloatArray())
        }
    }
}
