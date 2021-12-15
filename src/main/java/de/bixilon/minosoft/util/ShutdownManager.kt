/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util

import de.bixilon.minosoft.ShutdownReasons
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toSynchronizedSet
import de.bixilon.minosoft.util.filewatcher.FileWatcherService
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.task.pool.DefaultThreadPool
import kotlin.system.exitProcess

object ShutdownManager {
    private var initialized = false
    private var exiting = false


    fun shutdown(message: String? = null, reason: ShutdownReasons = ShutdownReasons.UNKNOWN) {
        Log.log(LogMessageType.GENERAL, LogLevels.INFO) { "Shutting down..." }
        for (connection in PlayConnection.ACTIVE_CONNECTIONS.toSynchronizedSet()) {
            connection.disconnect()
        }
        FileWatcherService.stop()
        DefaultThreadPool.shutdownNow()
        exitProcess(reason.exitCode)
    }

    fun init() {
        Runtime.getRuntime().addShutdownHook(Thread({
            if (exiting) {
                return@Thread
            }
            exiting = true

            shutdown()
        }, "Shutdown Hook"))
        initialized = true
    }
}
