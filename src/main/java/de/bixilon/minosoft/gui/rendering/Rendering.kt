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

import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ConnectionStates
import de.bixilon.minosoft.util.CountUpAndDownLatch
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import org.lwjgl.Version

class Rendering(private val connection: PlayConnection) {
    val renderWindow: RenderWindow = RenderWindow(connection, this)

    fun start(latch: CountUpAndDownLatch) {
        latch.countUp()
        Thread({
            try {
                Log.log(LogMessageType.RENDERING_GENERAL, LogLevels.INFO) { "Hello LWJGL ${Version.getVersion()}!" }
                CONTEXT_MAP[Thread.currentThread()] = renderWindow
                renderWindow.init(latch)
                renderWindow.startRenderLoop()
                renderWindow.exit()
            } catch (exception: Throwable) {
                CONTEXT_MAP.remove(Thread.currentThread())
                exception.printStackTrace()
                try {
                    renderWindow.exit()
                } catch (ignored: Throwable) {
                }
                if (connection.isConnected) {
                    connection.disconnect()
                }
                connection.connectionState = ConnectionStates.FAILED_NO_RETRY
            }
        }, "Rendering").start()
    }

    companion object {
        private val CONTEXT_MAP: MutableMap<Thread, RenderWindow> = mutableMapOf()

        val currentContext: RenderWindow?
            get() = CONTEXT_MAP[Thread.currentThread()]
    }
}
