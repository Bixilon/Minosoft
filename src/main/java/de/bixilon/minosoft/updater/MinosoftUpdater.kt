/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.updater

import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.kutil.os.PlatformInfo
import de.bixilon.kutil.string.StringUtil.formatPlaceholder
import de.bixilon.kutil.url.URLUtil.toURL
import de.bixilon.minosoft.config.profile.profiles.other.OtherProfileManager
import de.bixilon.minosoft.properties.MinosoftProperties
import de.bixilon.minosoft.util.http.HTTP2.get
import de.bixilon.minosoft.util.http.HTTPResponse
import de.bixilon.minosoft.util.http.exceptions.HTTPException
import de.bixilon.minosoft.util.json.Jackson
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.net.URL

object MinosoftUpdater {
    var update: MinosoftUpdate? by observed(null)
        private set

    private fun validateURL(url: URL) {
        if (url.protocol == "https") return
        if (url.protocol == "http") {
            if (url.host == "localhost" || url.host == "127.0.0.1") return
            throw IllegalArgumentException("Using non secure hosts on http is not allowed: $url!")
        }

        throw IllegalStateException("Illegal protocol: $url")
    }

    fun check(force: Boolean = false, callback: (MinosoftUpdate?) -> Unit) {
        if (!MinosoftProperties.canUpdate()) return
        if (!force) {
            this.update?.let { callback.invoke(update); return }
        }
        DefaultThreadPool += {
            val update = check()
            callback.invoke(update)
        }
    }

    fun check(error: Boolean = false): MinosoftUpdate? {
        val profile = OtherProfileManager.selected.updater
        return check(profile.url, profile.channel, error)
    }


    fun check(url: String, channel: String, error: Boolean): MinosoftUpdate? {
        val commit = MinosoftProperties.git?.commit ?: ""
        val version = MinosoftProperties.general.name
        val stable = MinosoftProperties.general.stable
        val os = PlatformInfo.OS
        val arch = PlatformInfo.ARCHITECTURE

        val request = url.formatPlaceholder(
            "COMMIT" to commit,
            "VERSION" to version,
            "STABLE" to stable,
            "OS" to os.name.lowercase(),
            "ARCH" to arch.name.lowercase(),
            "CHANNEL" to channel.lowercase(),
        )

        validateURL(request.toURL())
        val update = request(request, error)
        this.update = null // clear first to "reprompt"
        this.update = update
        return update
    }

    private fun request(url: String, error: Boolean): MinosoftUpdate? {
        val response: HTTPResponse<String>
        try {
            response = url.get({ it })
        } catch (exception: Throwable) {
            Log.log(LogMessageType.OTHER, LogLevels.WARN) { "Could not check for updates: $exception" }
            if (error) throw exception
            return null
        }

        return when (response.statusCode) {
            204 -> null
            200 -> parse(response.body)
            else -> throw HTTPException(response.statusCode, response.body)
        }
    }

    fun parse(data: String): MinosoftUpdate {
        return Jackson.MAPPER.readValue(data, MinosoftUpdate::class.java)
    }

    fun download(update: MinosoftUpdate, progress: UpdateProgress) {
        val download = update.download
        if (download == null) {
            progress.log?.print("Update is unavailable for download. Please download it manually!")
            return
        }
        progress.log?.print("Downloading update...")

        progress.log?.print("TODO :)")
        progress.stage = UpdateProgress.UpdateStage.FAILED
        // UpdateKey.require(data, signature)
    }
}
