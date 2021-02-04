package de.bixilon.minosoft.gui.rendering

import de.bixilon.minosoft.data.world.ChunkLocation
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.textures.TextureArray
import de.bixilon.minosoft.protocol.network.Connection
import org.lwjgl.*
import org.lwjgl.glfw.Callbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWWindowSizeCallback
import org.lwjgl.opengl.*
import org.lwjgl.opengl.GL11.glClear
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class RenderWindow(private val connection: Connection) {
    private var screenWidth = 800
    private var screenHeight = 600
    private var polygonEnabled = false
    private lateinit var shader: Shader
    private lateinit var texture0: TextureArray
    private var windowId: Long = 0
    private var deltaTime = 0.0 // time between current frame and last frame

    private var lastFrame = 0.0
    lateinit var camera: Camera

    val renderQueue = ConcurrentLinkedQueue<Runnable>()

    val chunkSectionsToDraw = ConcurrentHashMap<ChunkLocation, ConcurrentHashMap<Int, Mesh>>()


    fun init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set()

        // Initialize  Most GLFW functions will not work before doing this.
        check(glfwInit()) { "Unable to initialize GLFW" }

        // Configure GLFW
        glfwDefaultWindowHints() // optional, the current window hints are already the default
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE) // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE) // the window will be resizable

        // Create the window
        windowId = glfwCreateWindow(screenWidth, screenHeight, "Hello World!", MemoryUtil.NULL, MemoryUtil.NULL)
        if (windowId == MemoryUtil.NULL) {
            glfwTerminate()
            throw RuntimeException("Failed to create the GLFW window")
        }
        camera = Camera(45f, windowId)
        glfwSetWindowSizeCallback(windowId, object : GLFWWindowSizeCallback() {
            override fun invoke(window: Long, width: Int, height: Int) {
                GL11.glViewport(0, 0, width, height)
                screenWidth = width
                screenHeight = height
                camera.calculateProjectionMatrix(screenWidth, screenHeight, shader)
            }
        })

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(this.windowId) { windowId: Long, key: Int, scanCode: Int, action: Int, mods: Int ->
            run {
                if (action != GLFW_RELEASE) {
                    return@run
                }
                when (key) {
                    GLFW_KEY_ESCAPE -> {
                        glfwSetWindowShouldClose(this.windowId, true)
                    }
                    GLFW_KEY_P -> {
                        switchPolygonMode()
                    }
                }
            }

        }

        glfwSetInputMode(windowId, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
        glfwSetCursorPosCallback(windowId) { windowId: Long, xPos: Double, yPos: Double -> camera.mouseCallback(xPos, yPos) }
        MemoryStack.stackPush().let { stack ->
            val pWidth = stack.mallocInt(1) // int*
            val pHeight = stack.mallocInt(1) // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(windowId, pWidth, pHeight)

            // Get the resolution of the primary monitor
            val videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor())

            // Center the window
            glfwSetWindowPos(windowId, (videoMode!!.width() - pWidth[0]) / 2, (videoMode.height() - pHeight[0]) / 2)
        }

        // Make the OpenGL context current
        glfwMakeContextCurrent(windowId)
        // Enable v-sync
        glfwSwapInterval(1)


        // Make the window visible
        glfwShowWindow(windowId)
        GL.createCapabilities()
        GL11.glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
    }

    fun startLoop() {
        texture0 = TextureArray(connection.version.assetsManager, arrayOf("block/bedrock", "block/dirt", "block/stone", "block/glass", "block/red_wool"))
        texture0.load()

        shader = Shader("vertex.glsl", "fragment.glsl")
        shader.load()
        shader.use()
        shader.setInt("texture0", 0)

        camera.calculateProjectionMatrix(screenWidth, screenHeight, shader)
        camera.calculateViewMatrix(shader)


        var framesLastSecond = 0
        var lastCalcTime = glfwGetTime()
        while (!glfwWindowShouldClose(windowId)) {
            glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT) // clear the framebuffer

            val currentFrame = glfwGetTime()

            deltaTime = currentFrame - lastFrame
            lastFrame = currentFrame

            texture0.use(GL13.GL_TEXTURE0)

            shader.use()

            camera.calculateViewMatrix(shader)

            for ((_, map) in chunkSectionsToDraw) {
                for ((_, mesh) in map) {
                    mesh.draw(shader)
                }
            }

            glfwSwapBuffers(windowId) // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents()
            handleInput()

            for (renderQueueElement in renderQueue) {
                renderQueueElement.run()
                renderQueue.remove(renderQueueElement)
            }

            camera.handleInput(deltaTime)

            if (glfwGetTime() - lastCalcTime >= 0.5) {
                glfwSetWindowTitle(windowId, "FPS: ${framesLastSecond * 2}")
                lastCalcTime = glfwGetTime()
                framesLastSecond = 0
            }
            framesLastSecond++
        }
    }

    fun exit() {
        // Free the window callbacks and destroy the window
        Callbacks.glfwFreeCallbacks(windowId)
        glfwDestroyWindow(windowId)

        // Terminate GLFW and free the error callback
        glfwTerminate()
        glfwSetErrorCallback(null)!!.free()
    }

    private fun switchPolygonMode() {
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, if (polygonEnabled) {
            GL11.GL_LINE
        } else {
            GL11.GL_FILL
        })
        polygonEnabled = !polygonEnabled
    }

    private fun handleInput() {
    }
}
