/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui

import de.bixilon.kutil.math.simple.DoubleMath.rounded10
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.profiler.stack.StackedProfiler
import de.bixilon.kutil.profiler.stack.StackedProfiler.Companion.invoke
import de.bixilon.kutil.time.TimeUtil.sleep
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.RenderingOptions
import de.bixilon.minosoft.gui.rendering.RenderingStates
import de.bixilon.minosoft.gui.rendering.events.WindowCloseEvent
import de.bixilon.minosoft.gui.rendering.system.base.IntegratedBufferTypes
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogMessageType
import java.io.FileOutputStream
import kotlin.time.Duration.Companion.milliseconds

class RenderLoop(
    private val context: RenderContext,
) {
    private var slowRendering = context.profile.performance.slowRendering

    private var lastFrame = 0.0


    init {
        context.profile.performance::slowRendering.observe(this) { this.slowRendering = it }
    }

    private fun RenderContext.burn() {
        while (!query.isReady) {
            var burned = false
            if (queue.size > 0) burned = true
            profiler("queue") { queue.work(5) }
            profiler("burn") { renderer.forEach { if (it.burnTime()) burned = true } }
            if (!burned) {
                profiler("sleep") { sleep(1.milliseconds) }
            }
        }
    }

    private fun loop() {
        if (context.state == RenderingStates.PAUSED) {
            context.window.title = "Minosoft | Paused"
        }

        while (context.state == RenderingStates.PAUSED) {
            sleep(20.milliseconds)
            context.window.pollEvents()
            context.queue.work()
        }

        context.profiler = if (RenderingOptions.profileFrames) StackedProfiler() else null
        context.renderStats.startFrame()

        context.profiler("window poll events") { context.window.pollEvents() }

        context.profiler("framebuffer update") {
            context.framebuffer.update()
            context.framebuffer.clear()
            context.system.framebuffer = null
            context.system.clear(IntegratedBufferTypes.COLOR_BUFFER, IntegratedBufferTypes.DEPTH_BUFFER)
        }


        val time = context.window.time
        val delta = time - lastFrame
        lastFrame = time

        context.profiler("input") { context.input.draw(delta) }
        context.profiler("camera") { context.camera.draw() }

        context.profiler("light") {
            context.light.updateAsync() // ToDo: do async
            context.light.update()
        }



        context.profiler("animations") { context.textures.static.animator.update() }

        context.query.begin()
        context.profiler("draw") { context.renderer.draw() }
        context.query.end()

        context.profiler("burn") { context.burn() }


        context.profiler("post draw") { context.renderer.forEach { it.postDraw() } }

        context.profiler("reset") { context.system.reset() } // Reset to enable depth mask, etc again

        // handle opengl context tasks, but limit it per frame
        context.profiler("queue") { context.queue.workTimeLimited(RenderConstants.MAXIMUM_QUEUE_TIME_PER_FRAME) }

        context.renderStats.endDraw()


        context.profiler("swap buffers") { context.window.swapBuffers() }

        context.profiler("clear framebuffer") {
            // glClear waits for any unfinished operation, so it might wait for the buffer swap and makes frame times really long.
            context.framebuffer.clear()
            context.system.framebuffer = null
            context.system.clear(IntegratedBufferTypes.COLOR_BUFFER, IntegratedBufferTypes.DEPTH_BUFFER)
        }


        if (context.state == RenderingStates.BACKGROUND && slowRendering) {
            sleep(100.milliseconds)
        }

        for (error in context.system.getErrors()) {
            context.session.util.sendDebugMessage(error.message)
        }

        if (RenderConstants.SHOW_FPS_IN_WINDOW_TITLE) {
            context.window.title = "${RunConfiguration.APPLICATION_NAME} | FPS: ${context.renderStats.smoothAvgFPS.rounded10}"
        }
        context.renderStats.endFrame()



        context.profiler?.let {
            context.profiler = null
            val segment = it.finish()
            if (segment.duration > 20.milliseconds) {
                FileOutputStream("minosoft.perf").use { it.write(segment.toPerf().toByteArray()) }
            }
        }
    }


    fun startLoop() {
        Log.log(LogMessageType.RENDERING) { "Starting loop" }
        context.session.events.listen<WindowCloseEvent> { context.state = RenderingStates.QUITTING }
        while (true) {
            if (context.state == RenderingStates.QUITTING || context.session.established || !context.state.active) {
                break
            }
            loop()
        }
    }
}
