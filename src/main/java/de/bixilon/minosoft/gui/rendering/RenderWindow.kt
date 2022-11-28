/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.concurrent.queue.Queue
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.math.simple.DoubleMath.rounded10
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.kutil.primitive.BooleanUtil.decide
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.gui.rendering.camera.Camera
import de.bixilon.minosoft.gui.rendering.events.ResizeWindowEvent
import de.bixilon.minosoft.gui.rendering.events.WindowCloseEvent
import de.bixilon.minosoft.gui.rendering.font.Font
import de.bixilon.minosoft.gui.rendering.font.FontLoader
import de.bixilon.minosoft.gui.rendering.framebuffer.FramebufferManager
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.input.key.DefaultKeyCombinations
import de.bixilon.minosoft.gui.rendering.input.key.RenderWindowInputHandler
import de.bixilon.minosoft.gui.rendering.models.ModelLoader
import de.bixilon.minosoft.gui.rendering.renderer.renderer.RendererManager
import de.bixilon.minosoft.gui.rendering.renderer.renderer.RendererManager.Companion.registerDefault
import de.bixilon.minosoft.gui.rendering.shader.ShaderManager
import de.bixilon.minosoft.gui.rendering.skeletal.SkeletalManager
import de.bixilon.minosoft.gui.rendering.stats.AbstractRenderStats
import de.bixilon.minosoft.gui.rendering.stats.ExperimentalRenderStats
import de.bixilon.minosoft.gui.rendering.stats.RenderStats
import de.bixilon.minosoft.gui.rendering.system.base.IntegratedBufferTypes
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.window.BaseWindow
import de.bixilon.minosoft.gui.rendering.tint.TintManager
import de.bixilon.minosoft.gui.rendering.util.ScreenshotTaker
import de.bixilon.minosoft.gui.rendering.world.light.RenderLight
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.Stopwatch
import de.bixilon.minosoft.util.delegate.RenderingDelegate.observeRendering
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class RenderWindow(
    val connection: PlayConnection,
    val rendering: Rendering,
) {
    private val profile = connection.profiles.rendering
    val preferQuads = profile.advanced.preferQuads

    val window = BaseWindow.createWindow(this)
    val renderSystem = RenderSystem.createRenderSystem(this)
    val camera = Camera(this)

    val inputHandler = RenderWindowInputHandler(this)
    val screenshotTaker = ScreenshotTaker(this)
    val tintManager = TintManager(connection)
    val textureManager = renderSystem.createTextureManager()

    val queue = Queue()

    val shaderManager = ShaderManager(this)
    val framebufferManager = FramebufferManager(this)
    val renderer = RendererManager(this)
    val modelLoader = ModelLoader(this)

    val light = RenderLight(this)

    val skeletalManager = SkeletalManager(this)

    var initialized = false
        private set
    lateinit var renderStats: AbstractRenderStats
        private set

    lateinit var font: Font

    private var deltaFrameTime = 0.0

    private var lastFrame = 0.0
    private val latch = CountUpAndDownLatch(1)

    var tickCount = 0L
    var lastTickTimer = millis()

    private var slowRendering = profile.performance.slowRendering

    lateinit var thread: Thread
        private set

    var state by observed(RenderingStates.LOADING)

    init {
        connection::state.observe(this) {
            if (it == PlayConnectionStates.PLAYING && latch.count > 0) {
                latch.dec()
            }
        }
        profile.experimental::fps.observe(this, true) {
            renderStats = if (it) {
                ExperimentalRenderStats()
            } else {
                RenderStats()
            }
        }
        profile.performance::slowRendering.observe(this) { this.slowRendering = it }
        renderer.registerDefault(connection.profiles)

        var paused = false
        this::state.observe(this) {
            paused = if (paused) {
                queue.clear()
                false
            } else {
                it == RenderingStates.PAUSED
            }
        }
    }

    fun init(latch: CountUpAndDownLatch) {
        Log.log(LogMessageType.RENDERING_LOADING) { "Creating window..." }
        if (this::thread.isInitialized) {
            throw IllegalStateException("Thread is already set!")
        }
        this.thread = Thread.currentThread()
        val stopwatch = Stopwatch()

        window.init(connection.profiles.rendering)
        window.setDefaultIcon(connection.assetsManager)

        camera.init()

        tintManager.init(connection.assetsManager)


        Log.log(LogMessageType.RENDERING_LOADING, LogLevels.VERBOSE) { "Creating context (after ${stopwatch.labTime()})..." }

        renderSystem.init()

        Log.log(LogMessageType.RENDERING_LOADING, LogLevels.VERBOSE) { "Enabling all open gl features (after ${stopwatch.labTime()})..." }

        renderSystem.reset()

        // Init stage
        val initLatch = CountUpAndDownLatch(1, latch)
        Log.log(LogMessageType.RENDERING_LOADING, LogLevels.VERBOSE) { "Generating font and gathering textures (after ${stopwatch.labTime()})..." }
        textureManager.dynamicTextures.load(initLatch)
        textureManager.loadDefaultSkins(connection)
        textureManager.loadDefaultTextures()
        font = FontLoader.load(this, initLatch)


        framebufferManager.init()


        Log.log(LogMessageType.RENDERING_LOADING, LogLevels.VERBOSE) { "Initializing renderer (after ${stopwatch.labTime()})..." }
        light.init()
        skeletalManager.init()
        renderer.init(initLatch)

        // Wait for init stage to complete
        initLatch.dec()
        initLatch.await()

        // Post init stage
        Log.log(LogMessageType.RENDERING_LOADING, LogLevels.VERBOSE) { "Preloading textures (after ${stopwatch.labTime()})..." }
        textureManager.staticTextures.preLoad(latch)

        Log.log(LogMessageType.RENDERING_LOADING, LogLevels.VERBOSE) { "Loading textures (after ${stopwatch.labTime()})..." }
        textureManager.staticTextures.load(latch)
        font.postInit(latch)

        Log.log(LogMessageType.RENDERING_LOADING, LogLevels.VERBOSE) { "Post loading renderer (after ${stopwatch.labTime()})..." }
        shaderManager.postInit()
        skeletalManager.postInit()
        renderer.postInit(latch)
        framebufferManager.postInit()


        Log.log(LogMessageType.RENDERING_LOADING, LogLevels.VERBOSE) { "Loading skeletal meshes (after ${stopwatch.labTime()})" }
        modelLoader.entities.loadSkeletal()

        Log.log(LogMessageType.RENDERING_LOADING, LogLevels.VERBOSE) { "Registering callbacks (after ${stopwatch.labTime()})..." }

        window::focused.observeRendering(this) { state = it.decide(RenderingStates.RUNNING, RenderingStates.SLOW) }

        window::iconified.observeRendering(this) { state = it.decide(RenderingStates.PAUSED, RenderingStates.RUNNING) }
        profile.animations::sprites.observe(this, true) { textureManager.staticTextures.animator.enabled = it }


        inputHandler.init()
        DefaultKeyCombinations.registerAll(this)
        this::state.observe(this) {
            if (it == RenderingStates.PAUSED || it == RenderingStates.SLOW || it == RenderingStates.STOPPED) {
                pause(true)
            }
        }


        connection.events.fire(ResizeWindowEvent(this, previousSize = Vec2i(0, 0), size = window.size))

        textureManager.dynamicTextures.activate()
        textureManager.staticTextures.activate()

        Log.log(LogMessageType.RENDERING_LOADING) { "Rendering is fully prepared in ${stopwatch.totalTime()}" }
        initialized = true
        latch.dec()
        latch.await()
        this.latch.await()
        state = RenderingStates.RUNNING
        window.visible = true
        Log.log(LogMessageType.RENDERING_GENERAL) { "Showing window after ${stopwatch.totalTime()}" }
    }

    fun startLoop() {
        Log.log(LogMessageType.RENDERING_LOADING) { "Starting loop" }
        connection.events.listen<WindowCloseEvent> { state = RenderingStates.QUITTING }
        while (true) {
            if (state == RenderingStates.PAUSED) {
                window.title = "Minosoft | Paused"
            }

            while (state == RenderingStates.PAUSED) {
                Thread.sleep(20L)
                window.pollEvents()
            }

            if (connection.wasConnected || !state.active) {
                break
            }

            renderStats.startFrame()
            framebufferManager.clear()
            renderSystem.framebuffer = null
            renderSystem.clear(IntegratedBufferTypes.COLOR_BUFFER, IntegratedBufferTypes.DEPTH_BUFFER)

            light.updateAsync() // ToDo: do async
            light.update()


            val currentTickTime = millis()
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


            window.pollEvents()
            window.swapBuffers()

            inputHandler.draw(deltaFrameTime)
            camera.draw()

            // handle opengl context tasks, but limit it per frame
            queue.timeWork(RenderConstants.MAXIMUM_QUEUE_TIME_PER_FRAME)

            if (state == RenderingStates.STOPPED) {
                window.close()
                break
            }
            if (state == RenderingStates.SLOW && slowRendering) {
                Thread.sleep(100L)
            }

            for (error in renderSystem.getErrors()) {
                connection.util.sendDebugMessage(error.printMessage)
            }

            if (RenderConstants.SHOW_FPS_IN_WINDOW_TITLE) {
                window.title = "${RunConfiguration.APPLICATION_NAME} | FPS: ${renderStats.smoothAvgFPS.rounded10}"
            }
            renderStats.endFrame()
        }

        Log.log(LogMessageType.RENDERING_LOADING) { "Destroying render window..." }
        state = RenderingStates.STOPPED
        renderSystem.destroy()
        window.destroy()
        Log.log(LogMessageType.RENDERING_LOADING) { "Render window destroyed!" }
        // disconnect
        connection.network.disconnect()
    }

    fun pause(pause: Boolean? = null) {
        val guiRenderer = renderer[GUIRenderer]?.gui ?: return
        if (pause == null) {
            guiRenderer.pause()
        } else {
            guiRenderer.pause(pause)
        }
    }
}
