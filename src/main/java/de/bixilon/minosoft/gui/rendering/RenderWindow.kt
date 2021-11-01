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
import de.bixilon.minosoft.config.config.game.controls.KeyBindingsNames
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.block.WorldRenderer
import de.bixilon.minosoft.gui.rendering.block.chunk.ChunkBorderRenderer
import de.bixilon.minosoft.gui.rendering.block.outline.BlockOutlineRenderer
import de.bixilon.minosoft.gui.rendering.entity.EntityHitBoxRenderer
import de.bixilon.minosoft.gui.rendering.font.Font
import de.bixilon.minosoft.gui.rendering.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.hud.atlas.TextureLike
import de.bixilon.minosoft.gui.rendering.hud.atlas.TextureLikeTexture
import de.bixilon.minosoft.gui.rendering.input.key.RenderWindowInputHandler
import de.bixilon.minosoft.gui.rendering.modding.events.*
import de.bixilon.minosoft.gui.rendering.particle.ParticleRenderer
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import de.bixilon.minosoft.gui.rendering.system.base.IntegratedBufferTypes
import de.bixilon.minosoft.gui.rendering.system.base.PolygonModes
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.phases.RenderPhases
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGLRenderSystem
import de.bixilon.minosoft.gui.rendering.system.window.BaseWindow
import de.bixilon.minosoft.gui.rendering.system.window.GLFWWindow
import de.bixilon.minosoft.gui.rendering.util.ScreenshotTaker
import de.bixilon.minosoft.modding.event.events.PacketReceiveEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.play.PositionAndRotationS2CP
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.CountUpAndDownLatch
import de.bixilon.minosoft.util.KUtil.decide
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import de.bixilon.minosoft.util.Queue
import de.bixilon.minosoft.util.Stopwatch
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogMessageType
import glm_.vec2.Vec2
import glm_.vec2.Vec2i

class RenderWindow(
    val connection: PlayConnection,
    val rendering: Rendering,
) {
    val window: BaseWindow = GLFWWindow(connection)
    val renderSystem: RenderSystem = OpenGLRenderSystem(this)
    var initialized = false
        private set
    private lateinit var renderThread: Thread
    val renderStats = RenderStats()

    val inputHandler = RenderWindowInputHandler(this)

    private var deltaFrameTime = 0.0

    private var lastFrame = 0.0
    private val latch = CountUpAndDownLatch(1)

    private var renderingState = RenderingStates.RUNNING


    private val screenshotTaker = ScreenshotTaker(this)
    val tintColorCalculator = TintColorCalculator(connection.world)
    val font = Font()
    val textureManager = renderSystem.createTextureManager()

    val rendererMap: MutableMap<ResourceLocation, Renderer> = synchronizedMapOf()

    val queue = Queue()

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
                // ToDo: Set previous position
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

        window.init()

        inputHandler.camera.init(this)

        tintColorCalculator.init(connection.assetsManager)


        Log.log(LogMessageType.RENDERING_LOADING) { "Creating context (${stopwatch.labTime()})..." }

        renderSystem.init()

        Log.log(LogMessageType.RENDERING_LOADING) { "Enabling all open gl features (${stopwatch.labTime()})..." }

        renderSystem.reset()

        Log.log(LogMessageType.RENDERING_LOADING) { "Generating font and gathering textures (${stopwatch.labTime()})..." }
        textureManager.staticTextures.createTexture(RenderConstants.DEBUG_TEXTURE_RESOURCE_LOCATION)
        WHITE_TEXTURE = TextureLikeTexture(
            texture = textureManager.staticTextures.createTexture(ResourceLocation("minosoft:textures/white.png")),
            uvStart = Vec2(0, 0),
            uvEnd = Vec2(1.0f, 1.0f),
            size = Vec2i(16, 16)
        )

        font.load(connection.assetsManager, textureManager)

        shaderManager.init()


        Log.log(LogMessageType.RENDERING_LOADING) { "Initializing renderer (${stopwatch.labTime()})..." }
        for (renderer in rendererMap.values) {
            renderer.init()
        }

        Log.log(LogMessageType.RENDERING_LOADING) { "Preloading textures (${stopwatch.labTime()})..." }
        textureManager.staticTextures.preLoad()
        font.loadAtlas()

        Log.log(LogMessageType.RENDERING_LOADING) { "Loading textures (${stopwatch.labTime()})..." }
        textureManager.staticTextures.load()

        Log.log(LogMessageType.RENDERING_LOADING) { "Post loading renderer (${stopwatch.labTime()})..." }
        for (renderer in rendererMap.values) {
            renderer.postInit()
        }


        Log.log(LogMessageType.RENDERING_LOADING) { "Registering window callbacks (${stopwatch.labTime()})..." }

        connection.registerEvent(CallbackEventInvoker.of<WindowFocusChangeEvent> {
            setRenderStatus(it.focused.decide(RenderingStates.RUNNING, RenderingStates.SLOW))
        })

        connection.registerEvent(CallbackEventInvoker.of<WindowIconifyChangeEvent> {
            setRenderStatus(it.iconified.decide(RenderingStates.PAUSED, RenderingStates.RUNNING))
        })


        inputHandler.init()
        registerGlobalKeyCombinations()


        connection.fireEvent(ResizeWindowEvent(previousSize = Vec2i(0, 0), size = window.size))


        Log.log(LogMessageType.RENDERING_LOADING) { "Rendering is fully prepared in ${stopwatch.totalTime()}" }
        initialized = true
        latch.dec()
        latch.await()
        this.latch.await()
        window.visible = true
        Log.log(LogMessageType.RENDERING_GENERAL) { "Showing window after ${stopwatch.totalTime()}" }
    }

    private fun registerGlobalKeyCombinations() {
        inputHandler.registerKeyCallback(KeyBindingsNames.DEBUG_POLYGON) {
            val nextMode = it.decide(PolygonModes.LINE, PolygonModes.FILL)
            renderSystem.polygonMode = nextMode
            sendDebugMessage("Set polygon to: $nextMode")
        }

        inputHandler.registerKeyCallback(KeyBindingsNames.QUIT_RENDERING) { window.close() }
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
        var closed = false
        connection.registerEvent(CallbackEventInvoker.of<WindowCloseEvent> {
            closed = true
        })

        while (true) {
            if (connection.wasConnected || closed) {
                break
            }
            if (renderingState == RenderingStates.PAUSED) {
                Thread.sleep(100L)
                window.pollEvents()
                continue
            }
            renderStats.startFrame()
            renderSystem.clear(IntegratedBufferTypes.COLOR_BUFFER, IntegratedBufferTypes.DEPTH_BUFFER)


            val currentTickTime = System.currentTimeMillis()
            if (currentTickTime - this.lastTickTimer > ProtocolDefinition.TICK_TIME) {
                tickCount++
                inputHandler.currentKeyConsumer?.tick(tickCount)
                this.lastTickTimer = currentTickTime
            }

            val currentFrame = window.time
            deltaFrameTime = currentFrame - lastFrame
            lastFrame = currentFrame


            textureManager.staticTextures.animator.draw()


            for (renderer in rendererMap.values) {
                renderer.prepareDraw()
            }

            for (renderer in rendererMap.values) {
                for (phase in RenderPhases.VALUES) {
                    if (!phase.type.java.isAssignableFrom(renderer::class.java)) {
                        continue
                    }
                    phase.invokeSetup(renderer)
                    phase.invokeDraw(renderer)
                }
            }

            renderStats.endDraw()


            window.swapBuffers()
            window.pollEvents()

            inputHandler.draw(deltaFrameTime)

            // handle opengl context tasks, but limit it per frame
            queue.timeWork(RenderConstants.MAXIMUM_QUEUE_TIME_PER_FRAME)

            when (renderingState) {
                RenderingStates.SLOW -> Thread.sleep(100L)
                RenderingStates.RUNNING, RenderingStates.PAUSED -> {
                }
                RenderingStates.STOPPED -> window.close()
            }
            renderStats.endFrame()

            if (RenderConstants.SHOW_FPS_IN_WINDOW_TITLE) {
                window.title = "Minosoft | FPS: ${renderStats.fpsLastSecond}"
            }
        }

        Log.log(LogMessageType.RENDERING_LOADING) { "Destroying render window..." }
        window.destroy()
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

    fun assertOnRenderThread() {
        check(Thread.currentThread() === renderThread) { "Current thread (${Thread.currentThread().name} is not the render thread!" }
    }

    operator fun <T : Renderer> get(renderer: RendererBuilder<T>): T? {
        return rendererMap[renderer.RESOURCE_LOCATION] as T?
    }
}
