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

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.config.config.game.controls.KeyBindingsNames
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.chunk.ChunkBorderRenderer
import de.bixilon.minosoft.gui.rendering.chunk.WorldRenderer
import de.bixilon.minosoft.gui.rendering.chunk.block.outline.BlockOutlineRenderer
import de.bixilon.minosoft.gui.rendering.entities.EntityHitBoxRenderer
import de.bixilon.minosoft.gui.rendering.font.Font
import de.bixilon.minosoft.gui.rendering.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.hud.atlas.TextureLike
import de.bixilon.minosoft.gui.rendering.hud.atlas.TextureLikeTexture
import de.bixilon.minosoft.gui.rendering.input.key.RenderWindowInputHandler
import de.bixilon.minosoft.gui.rendering.modding.events.RenderingStateChangeEvent
import de.bixilon.minosoft.gui.rendering.modding.events.ScreenResizeEvent
import de.bixilon.minosoft.gui.rendering.particle.ParticleRenderer
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import de.bixilon.minosoft.gui.rendering.textures.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureArray
import de.bixilon.minosoft.gui.rendering.util.ScreenshotTaker
import de.bixilon.minosoft.modding.event.CallbackEventInvoker
import de.bixilon.minosoft.modding.event.events.PacketReceiveEvent
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.play.PositionAndRotationS2CP
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.CountUpAndDownLatch
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import de.bixilon.minosoft.util.Queue
import de.bixilon.minosoft.util.Stopwatch
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

class RenderWindow(
    val connection: PlayConnection,
    val rendering: Rendering,
) {
    var initialized = false
        private set
    private lateinit var renderThread: Thread
    val renderStats = RenderStats()
    var screenDimensions = Vec2i(900, 500)
        private set
    var screenDimensionsF = Vec2(screenDimensions)
        private set
    val inputHandler = RenderWindowInputHandler(this)

    var windowId = 0L
    private var deltaFrameTime = 0.0

    private var lastFrame = 0.0
    private val latch = CountUpAndDownLatch(1)

    private var renderingState = RenderingStates.RUNNING


    private val screenshotTaker = ScreenshotTaker(this)
    val tintColorCalculator = TintColorCalculator(connection.world)
    val font = Font()
    val textures = TextureArray(synchronizedMapOf())

    val rendererMap: MutableMap<ResourceLocation, Renderer> = synchronizedMapOf()

    val queue = Queue()


    val shaders: MutableList<Shader> = mutableListOf()
    val shaderManager = ShaderManager(this)

    lateinit var WHITE_TEXTURE: TextureLike


    var tickCount = 0L
    var lastTickTimer = System.currentTimeMillis()

    private var initialPositionReceived = false

    init {
        connection.registerEvent(CallbackEventInvoker.of<PacketReceiveEvent> {
            val packet = it.packet
            if (packet !is PositionAndRotationS2CP) {
                return@of
            }
            if (!initialPositionReceived) {
                latch.dec()
                initialPositionReceived = true
            }
        })

        // order dependant (from back to front)
        registerRenderer(SkyRenderer)
        registerRenderer(WorldRenderer)
        registerRenderer(BlockOutlineRenderer)
        if (Minosoft.config.config.game.graphics.particles.enabled) {
            registerRenderer(ParticleRenderer)
        }
        if (Minosoft.config.config.game.entities.hitBox.enabled) {
            registerRenderer(EntityHitBoxRenderer)
        }
        if (Minosoft.config.config.game.world.chunkBorders.enabled) {
            registerRenderer(ChunkBorderRenderer)
        }
        registerRenderer(HUDRenderer)
    }

    fun init(latch: CountUpAndDownLatch) {
        renderThread = Thread.currentThread()
        Log.log(LogMessageType.RENDERING_LOADING) { "Creating window..." }
        val stopwatch = Stopwatch()
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



        if (!StaticConfiguration.DEBUG_MODE) {
            glfwSetInputMode(windowId, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
        }
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

        Log.log(LogMessageType.RENDERING_LOADING) { "Creating context (${stopwatch.labTime()})..." }
        // Make the OpenGL context current
        glfwMakeContextCurrent(windowId)
        // Enable v-sync
        glfwSwapInterval(Minosoft.config.config.game.other.swapInterval)


        // Make the window visible
        GL.createCapabilities()

        glClearColor(1.0f, 1.0f, 0.0f, 1.0f)

        Log.log(LogMessageType.RENDERING_LOADING) { "Enabling all open gl features (${stopwatch.labTime()})..." }
        glEnable(GL_DEPTH_TEST)

        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        glEnable(GL_CULL_FACE)


        Log.log(LogMessageType.RENDERING_LOADING) { "Generating font and gathering textures (${stopwatch.labTime()})..." }
        textures.allTextures.getOrPut(RenderConstants.DEBUG_TEXTURE_RESOURCE_LOCATION) { Texture(RenderConstants.DEBUG_TEXTURE_RESOURCE_LOCATION) }
        WHITE_TEXTURE = TextureLikeTexture(
            texture = Texture(ResourceLocation("minosoft:textures/white.png")),
            uvStart = Vec2(0, 0),
            uvEnd = Vec2(1.0f, 1.0f),
            size = Vec2i(16, 16)
        )
        textures.allTextures.getOrPut(WHITE_TEXTURE.texture.resourceLocation) { WHITE_TEXTURE.texture }

        font.load(connection.assetsManager, textures.allTextures)

        shaderManager.init()


        Log.log(LogMessageType.RENDERING_LOADING) { "Initializing renderer (${stopwatch.labTime()})..." }
        for (renderer in rendererMap.values) {
            renderer.init()
        }


        Log.log(LogMessageType.RENDERING_LOADING) { "Preloading textures (${stopwatch.labTime()})..." }
        textures.preLoad(connection.assetsManager)
        font.loadAtlas()

        Log.log(LogMessageType.RENDERING_LOADING) { "Loading textures (${stopwatch.labTime()})..." }
        textures.load()

        Log.log(LogMessageType.RENDERING_LOADING) { "Post loading renderer (${stopwatch.labTime()})..." }
        for (renderer in rendererMap.values) {
            renderer.postInit()
        }


        Log.log(LogMessageType.RENDERING_LOADING) { "Registering glfw callbacks (${stopwatch.labTime()})..." }
        glfwSetWindowSizeCallback(windowId, object : GLFWWindowSizeCallback() {
            override fun invoke(window: Long, width: Int, height: Int) {
                glViewport(0, 0, width, height)
                val previousSize = screenDimensions
                screenDimensions = Vec2i(width, height)
                screenDimensionsF = Vec2(screenDimensions)
                connection.fireEvent(ScreenResizeEvent(previousScreenDimensions = previousSize, screenDimensions = screenDimensions))
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
        glfwSetKeyCallback(this.windowId, inputHandler::keyInput)
        glfwSetMouseButtonCallback(this.windowId, inputHandler::mouseKeyInput)

        glfwSetCharCallback(windowId, inputHandler::charInput)
        glfwSetCursorPosCallback(windowId, inputHandler::mouseMove)


        inputHandler.init()
        registerGlobalKeyCombinations()


        connection.fireEvent(ScreenResizeEvent(previousScreenDimensions = Vec2i(0, 0), screenDimensions = screenDimensions))


        Log.log(LogMessageType.RENDERING_LOADING) { "Rendering is fully prepared in ${stopwatch.totalTime()}" }
        initialized = true
        latch.dec()
        latch.await()
        this.latch.await()
        glfwShowWindow(windowId)
        Log.log(LogMessageType.RENDERING_GENERAL) { "Showing window after ${stopwatch.totalTime()}" }
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

        inputHandler.registerKeyCallback(KeyBindingsNames.QUIT_RENDERING) { glfwSetWindowShouldClose(windowId, true) }
        inputHandler.registerKeyCallback(KeyBindingsNames.TAKE_SCREENSHOT) { screenshotTaker.takeScreenshot() }

        inputHandler.registerKeyCallback(KeyBindingsNames.DEBUG_PAUSE_INCOMING_PACKETS) {
            sendDebugMessage("Pausing incoming packets: $it")
            connection.network.pauseReceiving(it)
        }
        inputHandler.registerKeyCallback(KeyBindingsNames.DEBUG_PAUSE_OUTGOING_PACKETS) {
            sendDebugMessage("Pausing outgoing packets: $it")
            connection.network.pauseSending(it)
        }
    }

    fun startLoop() {
        Log.log(LogMessageType.RENDERING_LOADING) { "Starting loop" }
        while (!glfwWindowShouldClose(windowId)) {
            if (connection.wasConnected) {
                break
            }
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
            inputHandler.draw(deltaFrameTime)

            // handle opengl context tasks, but limit it per frame
            queue.timeWork(RenderConstants.MAXIMUM_QUEUE_TIME_PER_FRAME)

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
            queue.clear()
        }
        val previousState = this.renderingState
        this.renderingState = renderingStatus
        connection.fireEvent(RenderingStateChangeEvent(connection, previousState, renderingState))
    }

    fun registerRenderer(rendererBuilder: RendererBuilder<*>) {
        val renderer = rendererBuilder.build(connection, this)
        rendererMap[rendererBuilder.RESOURCE_LOCATION] = renderer
    }

    fun sendDebugMessage(message: String) {
        connection.sender.sendFakeChatMessage(RenderConstants.DEBUG_MESSAGES_PREFIX + message)
    }

    fun getClipboardText(): String {
        return glfwGetClipboardString(windowId) ?: ""
    }

    fun assertOnRenderThread() {
        check(Thread.currentThread() == renderThread) { "Current thread (${Thread.currentThread().name} is not the render thread!" }
    }

    operator fun <T : Renderer> get(renderer: RendererBuilder<T>): T? {
        return rendererMap[renderer.RESOURCE_LOCATION] as T?
    }
}
