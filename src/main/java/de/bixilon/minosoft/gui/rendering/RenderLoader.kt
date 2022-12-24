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
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.primitive.BooleanUtil.decide
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.kutil.unit.UnitFormatter.formatNanos
import de.bixilon.minosoft.gui.rendering.RenderUtil.pause
import de.bixilon.minosoft.gui.rendering.events.ResizeWindowEvent
import de.bixilon.minosoft.gui.rendering.font.FontLoader
import de.bixilon.minosoft.gui.rendering.input.key.DefaultKeyCombinations
import de.bixilon.minosoft.gui.rendering.renderer.renderer.DefaultRenderer
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates
import de.bixilon.minosoft.util.KUtil
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

    fun RenderContext.load(latch: CountUpAndDownLatch) {
        setThread()
        Log.log(LogMessageType.RENDERING_LOADING) { "Creating window..." }
        val stopwatch = Stopwatch()
        registerRenderer()

        window.init(connection.profiles.rendering)
        KUtil.ignoreAll { window.setDefaultIcon(connection.assetsManager) }

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
        textureManager.initializeSkins(connection)
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


        latch.dec() // initial count from rendering
        latch.await()

        Log.log(LogMessageType.RENDERING_LOADING) { "Rendering is fully prepared in ${stopwatch.totalTime()}" }
    }

    fun RenderContext.awaitPlaying() {
        state = RenderingStates.AWAITING

        val latch = CountUpAndDownLatch(1)

        connection::state.observe(this) {
            if (it == PlayConnectionStates.PLAYING && latch.count > 0) {
                latch.dec()
            }
        }

        val time = measureNanoTime {
            latch.dec()
            latch.await()
            state = RenderingStates.RUNNING
            window.visible = true
        }
        Log.log(LogMessageType.RENDERING_GENERAL) { "Showing window after ${time.formatNanos()}" }
    }

}
