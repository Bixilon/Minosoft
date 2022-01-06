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

import de.bixilon.kutil.concurrent.queue.Queue
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.math.MMath.round10
import de.bixilon.kutil.primitive.BooleanUtil.decide
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.config.profile.delegate.watcher.SimpleProfileDelegateWatcher.Companion.profileWatch
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.camera.Camera
import de.bixilon.minosoft.gui.rendering.font.Font
import de.bixilon.minosoft.gui.rendering.font.FontLoader
import de.bixilon.minosoft.gui.rendering.framebuffer.FramebufferManager
import de.bixilon.minosoft.gui.rendering.gui.hud.atlas.TextureLike
import de.bixilon.minosoft.gui.rendering.gui.hud.atlas.TextureLikeTexture
import de.bixilon.minosoft.gui.rendering.input.key.DefaultKeyCombinations
import de.bixilon.minosoft.gui.rendering.input.key.RenderWindowInputHandler
import de.bixilon.minosoft.gui.rendering.modding.events.*
import de.bixilon.minosoft.gui.rendering.renderer.RendererManager
import de.bixilon.minosoft.gui.rendering.renderer.RendererManager.Companion.registerDefault
import de.bixilon.minosoft.gui.rendering.stats.AbstractRenderStats
import de.bixilon.minosoft.gui.rendering.stats.ExperimentalRenderStats
import de.bixilon.minosoft.gui.rendering.stats.RenderStats
import de.bixilon.minosoft.gui.rendering.system.base.IntegratedBufferTypes
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGLRenderSystem
import de.bixilon.minosoft.gui.rendering.system.window.BaseWindow
import de.bixilon.minosoft.gui.rendering.system.window.GLFWWindow
import de.bixilon.minosoft.gui.rendering.tint.TintManager
import de.bixilon.minosoft.gui.rendering.util.ScreenshotTaker
import de.bixilon.minosoft.modding.event.events.connection.play.PlayConnectionStateChangeEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.Stopwatch
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import glm_.vec2.Vec2
import glm_.vec2.Vec2i

class RenderWindow(
    val connection: PlayConnection,
    val rendering: Rendering,
) {
    private val profile = connection.profiles.rendering
    val preferQuads = profile.advanced.preferQuads

    val window: BaseWindow = GLFWWindow(this, connection)
    val renderSystem: RenderSystem = OpenGLRenderSystem(this)
    val camera = Camera(this)

    val inputHandler = RenderWindowInputHandler(this)
    val screenshotTaker = ScreenshotTaker(this)
    val tintManager = TintManager(connection)
    val textureManager = renderSystem.createTextureManager()

    val queue = Queue()

    val shaderManager = ShaderManager(this)
    val framebufferManager = FramebufferManager(this)
    val renderer = RendererManager(this)

    var initialized = false
        private set
    lateinit var renderStats: AbstractRenderStats
        private set

    lateinit var font: Font

    lateinit var WHITE_TEXTURE: TextureLike

    private var deltaFrameTime = 0.0

    private var lastFrame = 0.0
    private val latch = CountUpAndDownLatch(1)

    var tickCount = 0L
    var lastTickTimer = TimeUtil.time


    var renderingState = RenderingStates.RUNNING
        private set(value) {
            if (field == value) {
                return
            }
            if (field == RenderingStates.PAUSED) {
                queue.clear()
            }
            val previousState = field
            field = value
            connection.fireEvent(RenderingStateChangeEvent(connection, previousState, value))
        }

    init {
        connection.registerEvent(CallbackEventInvoker.of<PlayConnectionStateChangeEvent> {
            if (it.state == PlayConnectionStates.PLAYING && latch.count > 0) {
                latch.dec()
            }
        })
        profile.experimental::fps.profileWatch(this, true, profile) {
            renderStats = if (it) {
                ExperimentalRenderStats()
            } else {
                RenderStats()
            }
        }
        renderer.registerDefault(connection.profiles)
    }

    fun init(latch: CountUpAndDownLatch) {
        Log.log(LogMessageType.RENDERING_LOADING) { "Creating window..." }
        val stopwatch = Stopwatch()

        window.init(connection.profiles.rendering)
        window.setDefaultIcon(connection.assetsManager)

        camera.init()

        tintManager.init(connection.assetsManager)


        Log.log(LogMessageType.RENDERING_LOADING, LogLevels.VERBOSE) { "Creating context (${stopwatch.labTime()})..." }

        renderSystem.init()

        Log.log(LogMessageType.RENDERING_LOADING, LogLevels.VERBOSE) { "Enabling all open gl features (${stopwatch.labTime()})..." }

        renderSystem.reset()

        // Init stage
        val initLatch = CountUpAndDownLatch(1, latch)
        Log.log(LogMessageType.RENDERING_LOADING, LogLevels.VERBOSE) { "Generating font and gathering textures (${stopwatch.labTime()})..." }
        textureManager.staticTextures.createTexture(RenderConstants.DEBUG_TEXTURE_RESOURCE_LOCATION)
        WHITE_TEXTURE = TextureLikeTexture(texture = textureManager.staticTextures.createTexture(ResourceLocation("minosoft:textures/white.png")), uvStart = Vec2(0.0f, 0.0f), uvEnd = Vec2(0.001f, 0.001f), size = Vec2i(16, 16))
        font = FontLoader.load(this, initLatch)


        framebufferManager.init()


        Log.log(LogMessageType.RENDERING_LOADING, LogLevels.VERBOSE) { "Initializing renderer (${stopwatch.labTime()})..." }
        renderer.init(initLatch)

        // Wait for init stage to complete
        initLatch.dec()
        initLatch.await()

        // Post init stage
        Log.log(LogMessageType.RENDERING_LOADING, LogLevels.VERBOSE) { "Preloading textures (${stopwatch.labTime()})..." }
        textureManager.staticTextures.preLoad(latch)

        Log.log(LogMessageType.RENDERING_LOADING, LogLevels.VERBOSE) { "Loading textures (${stopwatch.labTime()})..." }
        textureManager.staticTextures.load(latch)
        font.postInit(latch)

        Log.log(LogMessageType.RENDERING_LOADING, LogLevels.VERBOSE) { "Post loading renderer (${stopwatch.labTime()})..." }
        shaderManager.postInit()
        renderer.postInit(latch)
        framebufferManager.postInit()

        Log.log(LogMessageType.RENDERING_LOADING, LogLevels.VERBOSE) { "Registering callbacks (${stopwatch.labTime()})..." }

        connection.registerEvent(CallbackEventInvoker.of<WindowFocusChangeEvent> {
            renderingState = it.focused.decide(RenderingStates.RUNNING, RenderingStates.SLOW)
        })

        connection.registerEvent(CallbackEventInvoker.of<WindowIconifyChangeEvent> {
            renderingState = it.iconified.decide(RenderingStates.PAUSED, RenderingStates.RUNNING)
        })
        profile.animations::sprites.profileWatch(this, true, profile = profile) { textureManager.staticTextures.animator.enabled = it }


        inputHandler.init()
        DefaultKeyCombinations.registerAll(this)


        connection.fireEvent(ResizeWindowEvent(previousSize = Vec2i(0, 0), size = window.size))


        Log.log(LogMessageType.RENDERING_LOADING) { "Rendering is fully prepared in ${stopwatch.totalTime()}" }
        initialized = true
        latch.dec()
        latch.await()
        this.latch.await()
        window.visible = true
        Log.log(LogMessageType.RENDERING_GENERAL) { "Showing window after ${stopwatch.totalTime()}" }
    }

    fun startLoop() {
        Log.log(LogMessageType.RENDERING_LOADING) { "Starting loop" }
        var closed = false
        connection.registerEvent(CallbackEventInvoker.of<WindowCloseEvent> { closed = true })
        while (true) {
            if (connection.wasConnected || closed) {
                break
            }

            if (renderingState == RenderingStates.PAUSED) {
                window.title = "Minosoft | Paused"
            }

            while (renderingState == RenderingStates.PAUSED) {
                Thread.sleep(100L)
                window.pollEvents()
            }

            renderStats.startFrame()
            framebufferManager.clear()
            renderSystem.framebuffer = null
            renderSystem.clear(IntegratedBufferTypes.COLOR_BUFFER, IntegratedBufferTypes.DEPTH_BUFFER)


            val currentTickTime = TimeUtil.time
            if (currentTickTime - this.lastTickTimer > ProtocolDefinition.TICK_TIME) {
                tickCount++
                // inputHandler.currentKeyConsumer?.tick(tickCount)
                this.lastTickTimer = currentTickTime
            }

            val currentFrame = window.time
            deltaFrameTime = currentFrame - lastFrame
            lastFrame = currentFrame


            textureManager.staticTextures.animator.draw()

            renderer.render()

            renderSystem.reset() // Reset to enable depth mask, etc again

            renderStats.endDraw()


            window.swapBuffers()
            window.pollEvents()

            inputHandler.draw(deltaFrameTime)
            camera.draw()

            // handle opengl context tasks, but limit it per frame
            queue.timeWork(RenderConstants.MAXIMUM_QUEUE_TIME_PER_FRAME)

            when (renderingState) {
                RenderingStates.RUNNING, RenderingStates.PAUSED -> {
                }
                RenderingStates.SLOW -> Thread.sleep(100L)
                RenderingStates.STOPPED -> window.close()
            }

            for (error in renderSystem.getErrors()) {
                connection.util.sendDebugMessage(error.printMessage)
            }
            renderStats.endFrame()

            if (RenderConstants.SHOW_FPS_IN_WINDOW_TITLE) {
                window.title = "Minosoft | FPS: ${renderStats.smoothAvgFPS.round10}"
            }
        }

        Log.log(LogMessageType.RENDERING_LOADING) { "Destroying render window..." }
        window.destroy()
        Log.log(LogMessageType.RENDERING_LOADING) { "Render window destroyed!" }
        // disconnect
        connection.disconnect()
    }
}
