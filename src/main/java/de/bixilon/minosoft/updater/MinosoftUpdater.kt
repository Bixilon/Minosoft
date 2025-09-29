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

package de.bixilon.minosoft.updater

import com.google.common.io.Files
import de.bixilon.kutil.array.ByteArrayUtil.toHex
import de.bixilon.kutil.base64.Base64Util.fromBase64
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.file.PathUtil.div
import de.bixilon.kutil.file.PathUtil.toPath
import de.bixilon.kutil.hash.HashUtil
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.kutil.os.PlatformInfo
import de.bixilon.kutil.shutdown.ShutdownManager
import de.bixilon.kutil.stream.InputStreamUtil.copy
import de.bixilon.kutil.string.StringUtil.formatPlaceholder
import de.bixilon.kutil.url.URLUtil.toURL
import de.bixilon.minosoft.assets.util.FileUtil
import de.bixilon.minosoft.config.profile.profiles.other.OtherProfileManager
import de.bixilon.minosoft.properties.MinosoftProperties
import de.bixilon.minosoft.terminal.CommandLineArguments
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.http.HTTP2.get
import de.bixilon.minosoft.util.http.HTTPResponse
import de.bixilon.minosoft.util.http.exceptions.HTTPException
import de.bixilon.minosoft.util.json.Jackson
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.security.MessageDigest
import java.security.SignatureException
import kotlin.io.path.absolutePathString

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
        if (!RunConfiguration.UPDATE_CHECKING) return
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
            progress.error = IllegalAccessError("Unavailable...")
            return
        }
        progress.log?.print("Downloading update...")

        try {
            val stream = download.url.openStream()
            val digest = MessageDigest.getInstance(HashUtil.SHA_512)
            val temp = FileUtil.createTempFile()
            val signature = UpdateKey.createInstance()
            stream.copy(FileOutputStream(temp), digest = digest, signature = signature)
            if (digest.digest().toHex() != download.sha512) throw SignatureException("Hash mismatch of downloaded file: Expected ${download.sha512}, got ${digest.digest().toHex()}")
            if (!signature.verify(download.signature.fromBase64())) throw SignatureException("Signature of downloaded file mismatches!")

            progress.log?.print("Moving temporary file to final destination")

            // move to current directory
            val output = File(("./Minosoft-${update.id}.jar"))
            Files.move(temp, output) // TODO: might be possible to tamper jar? in the meantime
            progress.log?.print("Success, file saved to $output")

            start(output)
            progress.log?.print("Started new process, exiting")
            ShutdownManager.shutdown()
        } catch (error: Throwable) {
            if (progress.log == null) {
                error.printStackTrace()
            } else {
                progress.log?.print(error)
            }
            progress.error = error
            throw error
        }
    }

    fun start(jar: File) {
        val arguments: MutableList<String> = mutableListOf()
        arguments += (System.getProperty("java.home").toPath() / "bin" / "java").absolutePathString()
        arguments += "-jar"
        arguments += jar.absolutePath.toString()
        arguments += CommandLineArguments.ARGUMENTS
        ProcessBuilder(arguments).start()
    }
}
