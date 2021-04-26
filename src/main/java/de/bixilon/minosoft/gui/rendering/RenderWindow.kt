/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger, Lukas Eisenhauer
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering

import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.config.config.game.controls.KeyBindingsNames
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.input.camera.FrustumChangeCallback
import de.bixilon.minosoft.gui.input.key.RenderWindowInputHandler
import de.bixilon.minosoft.gui.modding.events.RenderingStateChangeEvent
import de.bixilon.minosoft.gui.rendering.chunk.WorldRenderer
import de.bixilon.minosoft.gui.rendering.font.Font
import de.bixilon.minosoft.gui.rendering.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.hud.atlas.TextureLike
import de.bixilon.minosoft.gui.rendering.hud.atlas.TextureLikeTexture
import de.bixilon.minosoft.gui.rendering.shader.ShaderHolder
import de.bixilon.minosoft.gui.rendering.textures.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureArray
import de.bixilon.minosoft.gui.rendering.util.ScreenshotTaker
import de.bixilon.minosoft.gui.rendering.util.abstractions.ScreenResizeCallback
import de.bixilon.minosoft.modding.event.CallbackEventInvoker
import de.bixilon.minosoft.modding.event.events.ConnectionStateChangeEvent
import de.bixilon.minosoft.modding.event.events.PacketReceiveEvent
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.play.PositionAndRotationS2CP
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.CountUpAndDownLatch
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogMessageType
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import org.lwjgl.glfw.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.util.concurrent.ConcurrentLinkedQueue

class RenderWindow(
    val connection: PlayConnection,
    val rendering: Rendering,
) {
    val renderStats = RenderStats()
    var screenDimensions = Vec2i(900, 500)
        private set
    var screenDimensionsF = Vec2(screenDimensions)
        private set
    val inputHandler = RenderWindowInputHandler(this)

    var windowId = 0L
    private var deltaFrameTime = 0.0 // time between current frame and last frame

    private var lastFrame = 0.0
    private val latch = CountUpAndDownLatch(1)

    private var renderingState = RenderingStates.RUNNING


    private val screenshotTaker = ScreenshotTaker(this)
    val tintColorCalculator = TintColorCalculator(connection.world)
    val font = Font()
    val textures = TextureArray(mutableListOf())

    val rendererMap: MutableMap<ResourceLocation, Renderer> = mutableMapOf()

    val renderQueue = ConcurrentLinkedQueue<Runnable>()

    lateinit var WHITE_TEXTURE: TextureLike


    val screenResizeCallbacks: MutableSet<ScreenResizeCallback> = mutableSetOf(inputHandler.camera)

    var tickCount = 0L
    var lastTickTimer = System.currentTimeMillis()

    init {
        connection.registerEvent(CallbackEventInvoker.of<ConnectionStateChangeEvent> {
            if (it.connection.isDisconnected) {
                renderQueue.add {
                    glfwSetWindowShouldClose(windowId, true)
                }
            }
        })
        connection.registerEvent(CallbackEventInvoker.of<PacketReceiveEvent> {
            val packet = it.packet
            if (packet !is PositionAndRotationS2CP) {
                return@of
            }
            if (latch.count > 0) {
                latch.countDown()
            }
            renderQueue.add {
                inputHandler.camera.setPosition(packet.position)
                inputHandler.camera.setRotation(packet.rotation.yaw, packet.rotation.pitch)
            }
        })

        registerRenderer(WorldRenderer)
        registerRenderer(HUDRenderer)
    }

    fun init(latch: CountUpAndDownLatch) {
        Log.log(LogMessageType.RENDERING_LOADING) { "Creating window..." }
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
        windowId = glfwCreateWindow(screenDimensions.x, screenDimensions.y, "Minosoft", MemoryUtil.NULL, MemoryUtil.NULL)
        if (windowId == MemoryUtil.NULL) {
            glfwTerminate()
            throw RuntimeException("Failed to create the GLFW window")
        }
        inputHandler.camera.init(this)

        tintColorCalculator.init(connection.assetsManager)


        glfwSetKeyCallback(this.windowId, inputHandler::invoke)

        glfwSetCharCallback(windowId, inputHandler::invoke)

        if (!StaticConfiguration.DEBUG_MODE) {
            glfwSetInputMode(windowId, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
        }
        glfwSetCursorPosCallback(windowId, inputHandler::invoke)
        glfwSetWindowSizeLimits(windowId, 100, 100, GLFW_DONT_CARE, GLFW_DONT_CARE)


        MemoryStack.stackPush().let { stack ->
            val pWidth = stack.mallocInt(1)
            val pHeight = stack.mallocInt(1)

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(windowId, pWidth, pHeight)

            // Get the resolution of the primary monitor
            val videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor())!!

            // Center the window
            glfwSetWindowPos(windowId, (videoMode.width() - pWidth[0]) / 2, (videoMode.height() - pHeight[0]) / 2)
        }

        Log.log(LogMessageType.RENDERING_LOADING) { "Creating context..." }
        // Make the OpenGL context current
        glfwMakeContextCurrent(windowId)
        // Enable v-sync
        glfwSwapInterval(1)


        // Make the window visible
        GL.createCapabilities()

        setSkyColor(RGBColor("#fffe7a"))

        Log.log(LogMessageType.RENDERING_LOADING) { "Enabling all open gl features..." }
        glEnable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        glEnable(GL_CULL_FACE)


        Log.log(LogMessageType.RENDERING_LOADING) { "Generating font and textures..." }
        textures.allTextures.add(Texture(RenderConstants.DEBUG_TEXTURE_RESOURCE_LOCATION))
        WHITE_TEXTURE = TextureLikeTexture(
            texture = Texture(ResourceLocation("minosoft:textures/white.png")),
            uvStart = Vec2(0, 0),
            uvEnd = Vec2(1.0f, 1.0f),
            size = Vec2i(16, 16)
        )
        textures.allTextures.add(WHITE_TEXTURE.texture)

        font.load(connection.assetsManager)

        font.preLoadAtlas(textures)

        Log.log(LogMessageType.RENDERING_LOADING) { "Initializing renderer..." }
        for (renderer in rendererMap.values) {
            renderer.init()
        }


        Log.log(LogMessageType.RENDERING_LOADING) { "Preloading textures..." }
        textures.preLoad(connection.assetsManager)

        font.loadAtlas()
        Log.log(LogMessageType.RENDERING_LOADING) { "Loading textures..." }
        textures.load()

        Log.log(LogMessageType.RENDERING_LOADING) { "Post loading renderer..." }
        for (renderer in rendererMap.values) {
            renderer.postInit()
            if (renderer is ShaderHolder) {
                inputHandler.camera.addShaders(renderer.shader)
            }
        }


        Log.log(LogMessageType.RENDERING_LOADING) { "Registering glfw callbacks..." }
        glfwSetWindowSizeCallback(windowId, object : GLFWWindowSizeCallback() {
            override fun invoke(window: Long, width: Int, height: Int) {
                glViewport(0, 0, width, height)
                screenDimensions = Vec2i(width, height)
                screenDimensionsF = Vec2(screenDimensions)
                for (callback in screenResizeCallbacks) {
                    callback.onScreenResize(screenDimensions)
                }
            }
        })

        glfwSetWindowFocusCallback(windowId, object : GLFWWindowFocusCallback() {
            override fun invoke(window: Long, focused: Boolean) {
                setRenderStatus(if (focused) {
                    RenderingStates.RUNNING
                } else {
                    RenderingStates.SLOW
                })
            }
        })

        glfwSetWindowIconifyCallback(windowId, object : GLFWWindowIconifyCallback() {
            override fun invoke(window: Long, iconified: Boolean) {
                setRenderStatus(if (iconified) {
                    RenderingStates.PAUSED
                } else {
                    RenderingStates.RUNNING
                })
            }
        })


        registerGlobalKeyCombinations()

        for (callback in screenResizeCallbacks) {
            callback.onScreenResize(screenDimensions)
        }


        glEnable(GL_DEPTH_TEST)

        Log.log(LogMessageType.RENDERING_LOADING) { "Rendering is fully prepared" }
        latch.countDown()
        latch.waitUntilZero()
        this.latch.waitUntilZero()
        glfwShowWindow(windowId)
        Log.log(LogMessageType.RENDERING_GENERAL) { "Showing window" }
    }

    private fun registerGlobalKeyCombinations() {
        inputHandler.registerKeyCallback(KeyBindingsNames.DEBUG_POLYGON) {
            glPolygonMode(GL_FRONT_AND_BACK, if (it) {
                GL_LINE
            } else {
                GL_FILL
            })
            sendDebugMessage("Toggled polygon mode!")
        }

        inputHandler.registerKeyCallback(KeyBindingsNames.QUIT_RENDERING) {
            glfwSetWindowShouldClose(windowId, true)
        }
        inputHandler.registerKeyCallback(KeyBindingsNames.TAKE_SCREENSHOT) {
            screenshotTaker.takeScreenshot()
        }
    }

    fun startRenderLoop() {
        Log.log(LogMessageType.RENDERING_LOADING) { "Starting loop" }
        while (!glfwWindowShouldClose(windowId)) {
            if (renderingState == RenderingStates.PAUSED) {
                Thread.sleep(100L)
                glfwPollEvents()
                continue
            }
            renderStats.startFrame()
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer


            val currentTickTime = System.currentTimeMillis()
            if (currentTickTime - this.lastTickTimer > ProtocolDefinition.TICK_TIME) {
                tickCount++
                inputHandler.currentKeyConsumer?.tick(tickCount)
                this.lastTickTimer = currentTickTime
            }

            val currentFrame = glfwGetTime()
            deltaFrameTime = currentFrame - lastFrame
            lastFrame = currentFrame


            textures.animator.draw()


            for (renderer in rendererMap.values) {
                renderer.draw()
            }

            renderStats.endDraw()


            glfwSwapBuffers(windowId)
            glfwPollEvents()
            inputHandler.camera.draw()
            inputHandler.camera.handleInput(deltaFrameTime)

            // handle opengl context tasks, but limit it per frame
            var actionsDone = 0
            for (renderQueueElement in renderQueue) {
                if (actionsDone == RenderConstants.MAXIMUM_CALLS_PER_FRAME) {
                    break
                }
                renderQueueElement.run()
                renderQueue.remove(renderQueueElement)
                actionsDone++
            }

            when (renderingState) {
                RenderingStates.SLOW -> Thread.sleep(100L)
                RenderingStates.RUNNING, RenderingStates.PAUSED -> {
                }
                RenderingStates.STOPPED -> glfwSetWindowShouldClose(windowId, true)
            }
            renderStats.endFrame()

            if (RenderConstants.SHOW_FPS_IN_WINDOW_TITLE) {
                glfwSetWindowTitle(windowId, "Minosoft | FPS: ${renderStats.fpsLastSecond}")
            }
        }
    }

    fun exit() {
        Log.log(LogMessageType.RENDERING_LOADING) { "Destroying render window..." }
        // Free the window callbacks and destroy the window
        Callbacks.glfwFreeCallbacks(windowId)
        glfwDestroyWindow(windowId)

        // Terminate GLFW and free the error callback
        glfwTerminate()
        glfwSetErrorCallback(null)!!.free()

        Log.log(LogMessageType.RENDERING_LOADING) { "Render window destroyed!" }
        // disconnect
        connection.disconnect()
    }

    private fun setRenderStatus(renderingStatus: RenderingStates) {
        if (renderingStatus == this.renderingState) {
            return
        }
        if (this.renderingState == RenderingStates.PAUSED) {
            renderQueue.clear()
        }
        val previousState = this.renderingState
        this.renderingState = renderingStatus
        connection.fireEvent(RenderingStateChangeEvent(connection, previousState, renderingState))
    }

    fun registerRenderer(renderBuilder: RenderBuilder) {
        val renderer = renderBuilder.build(connection, this)
        rendererMap[renderBuilder.RESOURCE_LOCATION] = renderer
        if (renderer is ScreenResizeCallback) {
            screenResizeCallbacks.add(renderer)
        }
        if (renderer is FrustumChangeCallback) {
            inputHandler.camera.addFrustumChangeCallback(renderer)
        }
    }

    fun setSkyColor(color: RGBColor) {
        glClearColor(color.floatRed, color.floatGreen, color.floatBlue, 1.0f)
    }

    fun sendDebugMessage(message: String) {
        connection.sender.sendFakeChatMessage(RenderConstants.DEBUG_MESSAGES_PREFIX + message)
    }

    fun getClipboardText(): String {
        return glfwGetClipboardString(windowId) ?: ""
    }
}
