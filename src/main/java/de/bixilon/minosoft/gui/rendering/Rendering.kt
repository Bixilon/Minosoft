/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
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

import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.gui.rendering.modding.events.WindowCloseEvent
import de.bixilon.minosoft.gui.rendering.sound.AudioPlayer
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.RenderPolling
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import org.lwjgl.Version

class Rendering(private val connection: PlayConnection) {
    private var latch: CountUpAndDownLatch? = null
    val renderWindow: RenderWindow = RenderWindow(connection, this)
    val audioPlayer: AudioPlayer = AudioPlayer(connection, this)

    fun init(latch: CountUpAndDownLatch) {
        Log.log(LogMessageType.RENDERING_GENERAL, LogLevels.INFO) { "Hello LWJGL ${Version.getVersion()}!" }
        latch.inc()
        this.latch = latch
        if (RunConfiguration.OPEN_Gl_ON_FIRST_THREAD) {
            RenderPolling.rendering = this
            RenderPolling.RENDERING_LATCH.dec()
            return
        }
        start()
    }

    fun start() {
        val latch = this.latch ?: throw IllegalStateException("Rendering not initialized yet!")
        startAudioPlayerThread(latch)
        if (RunConfiguration.OPEN_Gl_ON_FIRST_THREAD) {
            startRenderWindow(latch)
        } else {
            startRenderWindowThread(latch)
        }
    }

    private fun startAudioPlayerThread(latch: CountUpAndDownLatch) {
        if (connection.profiles.audio.skipLoading) {
            return
        }
        val audioLatch = CountUpAndDownLatch(1, latch)
        Thread({
            try {
                audioPlayer.init(audioLatch)
                audioPlayer.startLoop()
                audioPlayer.exit()
            } catch (exception: Throwable) {
                exception.printStackTrace()
                try {
                    audioPlayer.exit()
                } catch (ignored: Throwable) {
                }
                latch.minus(audioLatch.count)
            }
        }, "Audio#${connection.connectionId}").start()
    }

    private fun startRenderWindowThread(latch: CountUpAndDownLatch) {
        Thread({ startRenderWindow(latch) }, "Rendering#${connection.connectionId}").start()
    }

    private fun startRenderWindow(latch: CountUpAndDownLatch) {
        try {
            CONTEXT_MAP[Thread.currentThread()] = renderWindow
            renderWindow.init(latch)
            renderWindow.startLoop()
        } catch (exception: Throwable) {
            CONTEXT_MAP.remove(Thread.currentThread())
            exception.printStackTrace()
            try {
                renderWindow.window.destroy()
                connection.fireEvent(WindowCloseEvent(window = renderWindow.window))
            } catch (ignored: Throwable) {
            }
            if (!connection.network.connected) {
                connection.disconnect()
            }
            connection.disconnect()
            connection.error = exception
        }
    }

    companion object {
        private val CONTEXT_MAP: MutableMap<Thread, RenderWindow> = mutableMapOf()

        val currentContext: RenderWindow?
            get() = CONTEXT_MAP[Thread.currentThread()]
    }
}
