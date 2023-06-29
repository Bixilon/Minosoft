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

package de.bixilon.minosoft.gui

import de.bixilon.kutil.math.simple.DoubleMath.rounded10
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.RenderingStates
import de.bixilon.minosoft.gui.rendering.events.WindowCloseEvent
import de.bixilon.minosoft.gui.rendering.system.base.IntegratedBufferTypes
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogMessageType

class RenderLoop(
    private val context: RenderContext,
) {
    private var slowRendering = context.profile.performance.slowRendering

    private var deltaFrameTime = 0.0
    private var lastFrame = 0.0
    private var lastTick = TimeUtil.millis()


    init {
        var paused = false
        context::state.observe(this) {
            paused = if (paused) {
                context.queue.clear()
                false
            } else {
                it == RenderingStates.PAUSED
            }
        }
    }


    fun startLoop() {
        Log.log(LogMessageType.RENDERING_LOADING) { "Starting loop" }
        context.connection.events.listen<WindowCloseEvent> { context.state = RenderingStates.QUITTING }
        while (true) {
            if (context.state == RenderingStates.PAUSED) {
                context.window.title = "Minosoft | Paused"
            }

            while (context.state == RenderingStates.PAUSED) {
                Thread.sleep(20L)
                context.window.pollEvents()
            }

            if (context.connection.wasConnected || !context.state.active) {
                break
            }

            context.renderStats.startFrame()
            context.framebuffer.clear()
            context.system.framebuffer = null
            context.system.clear(IntegratedBufferTypes.COLOR_BUFFER, IntegratedBufferTypes.DEPTH_BUFFER)

            context.light.updateAsync() // ToDo: do async
            context.light.update()


            val currentTickTime = TimeUtil.millis()
            if (currentTickTime - this.lastTick > ProtocolDefinition.TICK_TIME) {
                // inputHandler.currentKeyConsumer?.tick(tickCount)
                this.lastTick = currentTickTime
            }

            val currentFrame = context.window.time
            deltaFrameTime = currentFrame - lastFrame
            lastFrame = currentFrame


            context.textures.staticTextures.animator.draw()

            context.renderer.render()

            context.system.reset() // Reset to enable depth mask, etc again

            context.renderStats.endDraw()


            context.window.pollEvents()
            context.window.swapBuffers()

            context.input.draw(deltaFrameTime)
            context.camera.draw()

            // handle opengl context tasks, but limit it per frame
            context.queue.timeWork(RenderConstants.MAXIMUM_QUEUE_TIME_PER_FRAME)

            if (context.state == RenderingStates.STOPPED) {
                context.window.close()
                break
            }
            if (context.state == RenderingStates.SLOW && slowRendering) {
                Thread.sleep(100L)
            }

            for (error in context.system.getErrors()) {
                context.connection.util.sendDebugMessage(error.printMessage)
            }

            if (RenderConstants.SHOW_FPS_IN_WINDOW_TITLE) {
                context.window.title = "${RunConfiguration.APPLICATION_NAME} | FPS: ${context.renderStats.smoothAvgFPS.rounded10}"
            }
            context.renderStats.endFrame()
        }

        Log.log(LogMessageType.RENDERING_LOADING) { "Destroying render window..." }
        context.state = RenderingStates.STOPPED
        context.system.destroy()
        context.window.destroy()
        Log.log(LogMessageType.RENDERING_LOADING) { "Render window destroyed!" }
        // disconnect
        context.connection.network.disconnect()
    }
}
