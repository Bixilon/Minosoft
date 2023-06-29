/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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
import de.bixilon.kutil.exception.ExceptionUtil.ignoreAll
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.latch.ParentLatch
import de.bixilon.kutil.latch.SimpleLatch
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.primitive.BooleanUtil.decide
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.kutil.unit.UnitFormatter.formatNanos
import de.bixilon.minosoft.gui.rendering.RenderUtil.pause
import de.bixilon.minosoft.gui.rendering.events.ResizeWindowEvent
import de.bixilon.minosoft.gui.rendering.font.manager.FontManager
import de.bixilon.minosoft.gui.rendering.input.key.DebugKeyBindings
import de.bixilon.minosoft.gui.rendering.input.key.DefaultKeyBindings
import de.bixilon.minosoft.gui.rendering.renderer.renderer.DefaultRenderer
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates
import de.bixilon.minosoft.util.Stopwatch
import de.bixilon.minosoft.util.delegate.RenderingDelegate.observeRendering
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import kotlin.system.measureNanoTime

object RenderLoader {

    private fun RenderContext.setThread() {
        if (this.thread != null) { // unsafeNull
            throw IllegalStateException("Thread is already set!")
        }
        this::thread.forceSet(Thread.currentThread())
    }

    private fun RenderContext.registerRenderer() {
        for (builder in DefaultRenderer.list) {
            this.renderer.register(builder)
        }
    }

    fun RenderContext.load(latch: AbstractLatch) {
        val renderLatch = ParentLatch(1, latch)
        setThread()
        Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { "Creating window..." }
        val stopwatch = Stopwatch()
        registerRenderer()

        window.init(connection.profiles.rendering)
        ignoreAll { window.setDefaultIcon(connection.assetsManager) }

        camera.init()

        tints.init(connection.assetsManager)


        Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { "Creating context (after ${stopwatch.labTime()})..." }

        system.init()

        Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { "Enabling all open gl features (after ${stopwatch.labTime()})..." }

        system.reset()

        // Init stage
        val initLatch = ParentLatch(1, renderLatch)
        Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { "Generating font and gathering textures (after ${stopwatch.labTime()})..." }
        textures.dynamicTextures.load(initLatch)
        textures.initializeSkins(connection)
        textures.loadDefaultTextures()
        font = FontManager.create(this, initLatch)


        framebuffer.init()


        Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { "Initializing renderer (after ${stopwatch.labTime()})..." }
        light.init()
        skeletal.init()
        renderer.init(initLatch)

        // Wait for init stage to complete
        initLatch.dec()
        initLatch.await()

        // Post init stage
        Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { "Preloading textures (after ${stopwatch.labTime()})..." }
        textures.staticTextures.preLoad(renderLatch)

        Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { "Loading textures (after ${stopwatch.labTime()})..." }
        textures.staticTextures.load(renderLatch)
        font.postInit(renderLatch)

        Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { "Post loading renderer (after ${stopwatch.labTime()})..." }
        shaders.postInit()
        skeletal.postInit()
        renderer.postInit(renderLatch)
        framebuffer.postInit()


        Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { "Loading skeletal meshes (after ${stopwatch.labTime()})" }
        models.entities.loadSkeletal()

        Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { "Registering callbacks (after ${stopwatch.labTime()})..." }

        window::focused.observeRendering(this) { state = it.decide(RenderingStates.RUNNING, RenderingStates.SLOW) }

        window::iconified.observeRendering(this) { state = it.decide(RenderingStates.PAUSED, RenderingStates.RUNNING) }
        profile.animations::sprites.observe(this, true) { textures.staticTextures.animator.enabled = it }


        input.init()
        DefaultKeyBindings.register(this)
        DebugKeyBindings.register(this)

        this::state.observe(this) {
            if (it == RenderingStates.PAUSED || it == RenderingStates.SLOW || it == RenderingStates.STOPPED) {
                pause(true)
            }
        }


        connection.events.fire(ResizeWindowEvent(this, previousSize = Vec2i(0, 0), size = window.size))

        textures.dynamicTextures.activate()
        textures.staticTextures.activate()


        renderLatch.dec() // initial count from rendering
        renderLatch.await()

        Log.log(LogMessageType.RENDERING) { "Rendering is fully prepared in ${stopwatch.totalTime()}" }
    }

    fun RenderContext.awaitPlaying() {
        state = RenderingStates.AWAITING

        val latch = SimpleLatch(1)

        connection::state.observe(this) {
            if (it == PlayConnectionStates.PLAYING && latch.count > 0) {
                latch.dec()
            }
        }

        val time = measureNanoTime {
            latch.await()
            state = RenderingStates.RUNNING
            window.visible = true
        }
        Log.log(LogMessageType.RENDERING) { "Showing window after ${time.formatNanos()}" }
    }
}
