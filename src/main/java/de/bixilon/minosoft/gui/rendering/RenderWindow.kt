package de.bixilon.minosoft.gui.rendering

import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.vec3.Vec3
import org.lwjgl.*
import org.lwjgl.glfw.Callbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWWindowSizeCallback
import org.lwjgl.opengl.*
import org.lwjgl.opengl.GL11.glClear
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil

class RenderWindow {
    private var screenWidth = 800
    private var screenHeight = 600
    private var polygonEnabled = false
    private lateinit var shader: Shader
    private lateinit var texture0: TextureArray
    private var windowId: Long = 0
    private var deltaTime = 0.0 // time between current frame and last frame

    private var lastFrame = 0.0
    private lateinit var camera: Camera

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
        val chunk = DummyData.getDummyChunk()

        texture0 = TextureArray(arrayOf("/textures/emerald_block.png", "/textures/brown_wool.png"))
        texture0.load()

        shader = Shader("vertex.glsl", "fragment.glsl")
        shader.load()
        shader.use()
        shader.setInt("texture0", 0)

        camera.calculateProjectionMatrix(screenWidth, screenHeight, shader)
        camera.calculateViewMatrix(shader)

        val preparedChunks = mutableListOf<Mesh>()



        for ((sectionHeight, section) in chunk.sections) {
            for ((location, block) in section.blocks) {
                val textureIndex = when (block.fullIdentifier) {
                    "minecraft:dirt" -> 1
                    else -> 0
                }
                preparedChunks.add(Mesh(textureIndex, Vec3(location.x, location.y + ProtocolDefinition.SECTION_HEIGHT_Y * sectionHeight, location.z)))
                // break
            }
            // break
        }


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

            for (mesh in preparedChunks) {
                mesh.draw()
            }

            //   for ((key, value) in chunk.sections) {
            //       for ((key1) in value.blocks) {
            //           val model = Mat4().translate(Vec3(key1.x, key1.y + ProtocolDefinition.SECTION_HEIGHT_Y * key, key1.z))
            //           shader.setMat4("model", model)
            //           glDrawArrays(GL11.GL_TRIANGLES, 0, 36)
            //       }
            //   }

            glfwSwapBuffers(windowId) // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents()
            handleInput()
            camera.handleInput(deltaTime)
            if (glfwGetTime() - lastCalcTime >= 1.0) {
                glfwSetWindowTitle(windowId, "FPS: $framesLastSecond")
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
