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

package de.bixilon.minosoft.data.assets

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.Util
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.io.*
import java.nio.file.Files
import java.security.MessageDigest
import java.util.zip.GZIPOutputStream

interface FileAssetsManager : AssetsManager {

    fun getAssetSize(hash: String): Long

    fun getFileAssetSize(hash: String): Long {
        return getFileAssetSize(hash, true)
    }

    fun getFileAssetSize(hash: String, compress: Boolean): Long

    fun saveAsset(data: ByteArray, compress: Boolean = true): String {
        val hash = Util.sha1(data)
        val destination = getAssetDiskPath(hash, compress)
        val outFile = File(destination)
        if (outFile.exists() && outFile.length() > 0) {
            return hash
        }
        Util.createParentFolderIfNotExist(destination)
        var outputStream: OutputStream = FileOutputStream(destination)
        if (compress) {
            outputStream = GZIPOutputStream(outputStream)
        }
        outputStream.write(data)
        outputStream.close()
        return hash
    }

    fun verifyAssetHash(hash: String, expectedSize: Long? = null, compressed: Boolean): Boolean {
        val size = getFileAssetSize(hash, compressed)
        if (size < 0L) {
            // file does not exist
            return false
        }
        if (!Minosoft.getConfig().config.debug.verifyAssets) {
            // file exists AND we should not check the hash of our file
            return true
        }
        expectedSize?.let {
            if (it != size) {
                return false
            }
        }
        try {
            return if (compressed) {
                hash == Util.sha1Gzip(File(getAssetDiskPath(hash, compressed)))
            } else {
                hash == Util.sha1(File(getAssetDiskPath(hash, compressed)))
            }
        } catch (exception: IOException) {
            Log.log(LogMessageType.ASSETS, level = LogLevels.VERBOSE, message = exception)
        }
        return false
    }

    fun verifyAssetHash(hash: String): Boolean {
        return verifyAssetHash(hash, compressed = true)
    }

    fun downloadAsset(url: String, hash: String, compress: Boolean, checkURL: Boolean = true) {
        if (verifyAssetHash(hash, compressed = compress)) {
            return
        }
        if (checkURL) {
            Util.checkURL(url)
        }
        Log.log(LogMessageType.ASSETS, level = LogLevels.VERBOSE, message = "Downloading %s -> %s", formatting = arrayOf<Any>(url, hash))
        if (compress) {
            Util.downloadFileAsGz(url, getAssetDiskPath(hash, compress))
            return
        }
        Util.downloadFile(url, getAssetDiskPath(hash, compress))
    }

    fun saveAsset(data: InputStream, compress: Boolean = true): String {
        var tempDestinationFile: File? = null
        while (tempDestinationFile == null || tempDestinationFile.exists()) { // file exist? lol
            tempDestinationFile = File(StaticConfiguration.TEMPORARY_FOLDER + "minosoft/" + Util.generateRandomString(32))
        }
        Util.createParentFolderIfNotExist(tempDestinationFile)

        var outputStream: OutputStream = FileOutputStream(tempDestinationFile)
        if (compress) {
            outputStream = GZIPOutputStream(outputStream)
        }
        val messageDigest = MessageDigest.getInstance("SHA-1")
        val buffer = ByteArray(ProtocolDefinition.DEFAULT_BUFFER_SIZE)
        var length: Int
        while (data.read(buffer, 0, buffer.size).also { length = it } != -1) {
            messageDigest.update(buffer, 0, length)
            outputStream.write(buffer, 0, length)
        }
        outputStream.close()
        val hash = Util.byteArrayToHexString(messageDigest.digest())

        // move file to desired destination
        val outputFile = File(getAssetDiskPath(hash, compress))
        Util.createParentFolderIfNotExist(outputFile)
        if (outputFile.exists()) {
            // file is already extracted
            if (!tempDestinationFile.delete()) {
                throw IllegalStateException("Could not delete temporary file ${tempDestinationFile.absolutePath}")
            }
            return hash
        }
        Files.move(tempDestinationFile.toPath(), outputFile.toPath())
        return hash
    }


    companion object {
        fun getAssetDiskPath(hash: String, compress: Boolean): String {
            if (hash.length != 40) {
                throw IllegalArgumentException("Invalid hash provided: $hash")
            }
            var path = StaticConfiguration.HOME_DIRECTORY + "assets/objects/${hash.substring(0, 2)}/$hash"

            if (compress) {
                path += ".gz"
            }

            return path
        }
    }
}
