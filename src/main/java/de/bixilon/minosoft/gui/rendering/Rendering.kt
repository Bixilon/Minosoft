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

import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.latch.ParentLatch
import de.bixilon.minosoft.gui.RenderLoop
import de.bixilon.minosoft.gui.rendering.RenderLoader.awaitPlaying
import de.bixilon.minosoft.gui.rendering.RenderLoader.load
import de.bixilon.minosoft.gui.rendering.events.WindowCloseEvent
import de.bixilon.minosoft.gui.rendering.sound.AudioPlayer
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import org.lwjgl.Version

class Rendering(private val connection: PlayConnection) {
    val context: RenderContext = RenderContext(connection, this)
    val audioPlayer: AudioPlayer = AudioPlayer(connection, this)

    fun start(latch: AbstractLatch, render: Boolean = true, audio: Boolean = true) {
        Log.log(LogMessageType.RENDERING_GENERAL, LogLevels.INFO) { "Hello LWJGL ${Version.getVersion()}!" }
        val loading = ParentLatch(2, latch)
        if (audio) startAudioPlayerThread(loading)
        if (render) startRenderWindowThread(loading)
        latch.dec()
    }

    private fun startAudioPlayerThread(latch: AbstractLatch) {
        if (connection.profiles.audio.skipLoading) {
            return
        }
        val audioLatch = ParentLatch(1, latch)
        Thread({
            try {
                Thread.currentThread().priority = Thread.MAX_PRIORITY
                audioPlayer.init(audioLatch)
                latch.dec() // initial count
                audioPlayer.startLoop()
                audioPlayer.exit()
            } catch (exception: Throwable) {
                exception.printStackTrace()
                try {
                    audioPlayer.exit()
                } catch (ignored: Throwable) {
                }
                connection.network.disconnect()
                connection.error = exception
                latch.minus(audioLatch.count)
            }
        }, "Audio#${connection.connectionId}").start()
    }

    private fun startRenderWindowThread(latch: AbstractLatch) {
        Thread({ startRenderWindow(latch) }, "Rendering#${connection.connectionId}").start()
    }

    private fun startRenderWindow(latch: AbstractLatch) {
        try {
            Thread.currentThread().priority = Thread.MAX_PRIORITY
            CONTEXT_MAP[Thread.currentThread()] = context
            context.load(latch)
            latch.dec()
            val loop = RenderLoop(context)
            context.awaitPlaying()
            loop.startLoop()
        } catch (exception: Throwable) {
            CONTEXT_MAP.remove(Thread.currentThread())
            exception.printStackTrace()
            try {
                context.window.destroy()
                connection.events.fire(WindowCloseEvent(context, window = context.window))
            } catch (ignored: Throwable) {
            }
            connection.network.disconnect()
            connection.error = exception
        }
    }

    companion object {
        private val CONTEXT_MAP: MutableMap<Thread, RenderContext> = mutableMapOf()

        val currentContext: RenderContext?
            get() = CONTEXT_MAP[Thread.currentThread()]
    }
}
