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
import de.bixilon.minosoft.config.key.KeyAction
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.rendering.block.WorldRenderer
import de.bixilon.minosoft.gui.rendering.block.chunk.ChunkBorderRenderer
import de.bixilon.minosoft.gui.rendering.block.outline.BlockOutlineRenderer
import de.bixilon.minosoft.gui.rendering.entity.EntityHitBoxRenderer
import de.bixilon.minosoft.gui.rendering.font.Font
import de.bixilon.minosoft.gui.rendering.font.FontLoader
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.hud.atlas.TextureLike
import de.bixilon.minosoft.gui.rendering.gui.hud.atlas.TextureLikeTexture
import de.bixilon.minosoft.gui.rendering.input.key.RenderWindowInputHandler
import de.bixilon.minosoft.gui.rendering.modding.events.*
import de.bixilon.minosoft.gui.rendering.particle.ParticleRenderer
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import de.bixilon.minosoft.gui.rendering.stats.AbstractRenderStats
import de.bixilon.minosoft.gui.rendering.system.base.IntegratedBufferTypes
import de.bixilon.minosoft.gui.rendering.system.base.PolygonModes
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.phases.RenderPhases
import de.bixilon.minosoft.gui.rendering.system.base.phases.SkipAll
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGLRenderSystem
import de.bixilon.minosoft.gui.rendering.system.window.BaseWindow
import de.bixilon.minosoft.gui.rendering.system.window.GLFWWindow
import de.bixilon.minosoft.gui.rendering.tint.TintManager
import de.bixilon.minosoft.gui.rendering.util.ScreenshotTaker
import de.bixilon.minosoft.modding.event.events.InternalMessageReceiveEvent
import de.bixilon.minosoft.modding.event.events.PacketReceiveEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.play.PositionAndRotationS2CP
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.CountUpAndDownLatch
import de.bixilon.minosoft.util.KUtil.decide
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.KUtil.unsafeCast
import de.bixilon.minosoft.util.MMath.round10
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
    val renderStats: AbstractRenderStats = AbstractRenderStats.createInstance()

    val inputHandler = RenderWindowInputHandler(this)

    private var deltaFrameTime = 0.0

    private var lastFrame = 0.0
    private val latch = CountUpAndDownLatch(1)

    private var renderingState = RenderingStates.RUNNING
        set(value) {
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


    private val screenshotTaker = ScreenshotTaker(this)
    val tintManager = TintManager(connection)
    val textureManager = renderSystem.createTextureManager()
    lateinit var font: Font

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
                latch.dec()
                initialPositionReceived = true
            }
        })

        // order dependent (from back to front)
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

        tintManager.init(connection.assetsManager)


        Log.log(LogMessageType.RENDERING_LOADING) { "Creating context (${stopwatch.labTime()})..." }

        renderSystem.init()

        Log.log(LogMessageType.RENDERING_LOADING) { "Enabling all open gl features (${stopwatch.labTime()})..." }

        renderSystem.reset()

        Log.log(LogMessageType.RENDERING_LOADING) { "Generating font and gathering textures (${stopwatch.labTime()})..." }
        textureManager.staticTextures.createTexture(RenderConstants.DEBUG_TEXTURE_RESOURCE_LOCATION)
        WHITE_TEXTURE = TextureLikeTexture(
            texture = textureManager.staticTextures.createTexture(ResourceLocation("minosoft:textures/white.png")),
            uvStart = Vec2(0.0f, 0.0f),
            uvEnd = Vec2(0.001f, 0.001f),
            size = Vec2i(16, 16)
        )
        font = FontLoader.load(this)


        shaderManager.init()


        Log.log(LogMessageType.RENDERING_LOADING) { "Initializing renderer (${stopwatch.labTime()})..." }
        for (renderer in rendererMap.values) {
            renderer.init()
        }

        Log.log(LogMessageType.RENDERING_LOADING) { "Preloading textures (${stopwatch.labTime()})..." }
        textureManager.staticTextures.preLoad()

        Log.log(LogMessageType.RENDERING_LOADING) { "Loading textures (${stopwatch.labTime()})..." }
        textureManager.staticTextures.load()
        font.postInit()

        Log.log(LogMessageType.RENDERING_LOADING) { "Post loading renderer (${stopwatch.labTime()})..." }
        for (renderer in rendererMap.values) {
            renderer.postInit()
        }


        Log.log(LogMessageType.RENDERING_LOADING) { "Registering window callbacks (${stopwatch.labTime()})..." }

        connection.registerEvent(CallbackEventInvoker.of<WindowFocusChangeEvent> {
            renderingState = it.focused.decide(RenderingStates.RUNNING, RenderingStates.SLOW)
        })

        connection.registerEvent(CallbackEventInvoker.of<WindowIconifyChangeEvent> {
            renderingState = it.iconified.decide(RenderingStates.PAUSED, RenderingStates.RUNNING)
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
        inputHandler.registerKeyCallback("minosoft:enable_debug_polygon".toResourceLocation(), KeyBinding(
            mutableMapOf(
                KeyAction.MODIFIER to mutableSetOf(KeyCodes.KEY_F4),
                KeyAction.STICKY to mutableSetOf(KeyCodes.KEY_P),
            ),
        )) {
            val nextMode = it.decide(PolygonModes.LINE, PolygonModes.FILL)
            renderSystem.polygonMode = nextMode
            sendDebugMessage("Set polygon to: $nextMode")
        }

        inputHandler.registerKeyCallback("minosoft:quit_rendering".toResourceLocation(), KeyBinding(
            mutableMapOf(
                KeyAction.RELEASE to mutableSetOf(KeyCodes.KEY_ESCAPE),
            ),
        )) { window.close() }

        inputHandler.registerKeyCallback("minosoft:take_screenshot".toResourceLocation(), KeyBinding(
            mutableMapOf(
                KeyAction.PRESS to mutableSetOf(KeyCodes.KEY_F2),
            ),
            ignoreConsumer = true,
        )) { screenshotTaker.takeScreenshot() }

        inputHandler.registerKeyCallback("minosoft:pause_incoming_packets".toResourceLocation(), KeyBinding(
            mutableMapOf(
                KeyAction.MODIFIER to mutableSetOf(KeyCodes.KEY_F4),
                KeyAction.STICKY to mutableSetOf(KeyCodes.KEY_I),
            ),
            ignoreConsumer = true,
        )) {
            sendDebugMessage("Pausing incoming packets: $it")
            connection.network.pauseReceiving(it)
        }

        inputHandler.registerKeyCallback("minosoft:pause_outgoing_packets".toResourceLocation(), KeyBinding(
            mutableMapOf(
                KeyAction.MODIFIER to mutableSetOf(KeyCodes.KEY_F4),
                KeyAction.STICKY to mutableSetOf(KeyCodes.KEY_O),
            ),
            ignoreConsumer = true,
        )) {
            sendDebugMessage("Pausing outgoing packets: $it")
            connection.network.pauseSending(it)
        }
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
            renderSystem.clear(IntegratedBufferTypes.COLOR_BUFFER, IntegratedBufferTypes.DEPTH_BUFFER)


            val currentTickTime = System.currentTimeMillis()
            if (currentTickTime - this.lastTickTimer > ProtocolDefinition.TICK_TIME) {
                tickCount++
                // inputHandler.currentKeyConsumer?.tick(tickCount)
                this.lastTickTimer = currentTickTime
            }

            val currentFrame = window.time
            deltaFrameTime = currentFrame - lastFrame
            lastFrame = currentFrame


            textureManager.staticTextures.animator.draw()

            val rendererList = rendererMap.values

            for (renderer in rendererList) {
                renderer.prepareDraw()
            }

            for (renderer in rendererList) {
                if (renderer is SkipAll && renderer.skipAll) {
                    continue
                }
                for (phase in RenderPhases.VALUES) {
                    if (!phase.type.java.isAssignableFrom(renderer::class.java)) {
                        continue
                    }
                    if (phase.invokeSkip(renderer)) {
                        continue
                    }
                    phase.invokeSetup(renderer)
                    phase.invokeDraw(renderer)
                }
            }
            renderSystem.reset() // Reset to enable depth mask, etc again

            renderStats.endDraw()


            window.swapBuffers()
            window.pollEvents()

            inputHandler.draw(deltaFrameTime)

            // handle opengl context tasks, but limit it per frame
            queue.timeWork(RenderConstants.MAXIMUM_QUEUE_TIME_PER_FRAME)

            when (renderingState) {
                RenderingStates.RUNNING, RenderingStates.PAUSED -> {
                }
                RenderingStates.SLOW -> Thread.sleep(100L)
                RenderingStates.STOPPED -> window.close()
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

    fun registerRenderer(rendererBuilder: RendererBuilder<*>) {
        val resourceLocation = rendererBuilder.RESOURCE_LOCATION
        if (resourceLocation in RunConfiguration.SKIP_RENDERERS) {
            return
        }
        val renderer = rendererBuilder.build(connection, this)
        rendererMap[resourceLocation] = renderer
    }

    fun sendDebugMessage(message: Any) {
        connection.fireEvent(InternalMessageReceiveEvent(connection, BaseComponent(RenderConstants.DEBUG_MESSAGES_PREFIX, ChatComponent.of(message).apply { applyDefaultColor(ChatColors.BLUE) })))
    }

    fun assertOnRenderThread() {
        check(Thread.currentThread() === renderThread) { "Current thread (${Thread.currentThread().name} is not the render thread!" }
    }

    operator fun <T : Renderer> get(renderer: RendererBuilder<T>): T? {
        return rendererMap[renderer.RESOURCE_LOCATION].unsafeCast()
    }
}
