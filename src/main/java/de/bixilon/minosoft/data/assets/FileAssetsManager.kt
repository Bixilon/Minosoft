/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
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
import java.io.*
import java.security.MessageDigest
import java.util.zip.GZIPOutputStream

interface FileAssetsManager : AssetsManager {

    fun getAssetSize(hash: String): Long

    fun saveAsset(data: ByteArray): String {
        val hash = Util.sha1(data)
        val destination = getAssetDiskPath(hash)
        val outFile = File(destination)
        if (outFile.exists() && outFile.length() > 0) {
            return hash
        }
        Util.createParentFolderIfNotExist(destination)
        val out: OutputStream = GZIPOutputStream(FileOutputStream(destination))
        out.write(data)
        out.close()
        return hash
    }

    fun verifyAssetHash(hash: String, compressed: Boolean): Boolean {
        if (getAssetSize(hash) < 0L) {
            // file does not exist
            return false
        }
        if (!Minosoft.getConfig().config.debug.verifyAssets) {
            // file exists AND we should not check the hash of our file
            return true
        }
        try {
            return if (compressed) {
                hash == Util.sha1Gzip(File(getAssetDiskPath(hash)))
            } else {
                hash == Util.sha1(File(getAssetDiskPath(hash)))
            }
        } catch (exception: IOException) {
            Log.printException(exception, LogLevels.DEBUG)
        }
        return false
    }

    fun verifyAssetHash(hash: String): Boolean {
        return verifyAssetHash(hash, true)
    }

    fun downloadAsset(url: String, hash: String, compress: Boolean, checkURL: Boolean = true) {
        if (verifyAssetHash(hash, compress)) {
            return
        }
        if (checkURL) {
            Util.checkURL(url)
        }
        Log.debug("Downloading %s -> %s", url, hash)
        if (compress) {
            Util.downloadFileAsGz(url, getAssetDiskPath(hash))
            return
        }
        Util.downloadFile(url, getAssetDiskPath(hash))
    }

    fun saveAsset(data: InputStream): String {
        var tempDestinationFile: File? = null
        while (tempDestinationFile == null || tempDestinationFile.exists()) { // file exist? lol
            tempDestinationFile = File(StaticConfiguration.TEMPORARY_FOLDER + "minosoft/" + Util.generateRandomString(32))
        }
        Util.createParentFolderIfNotExist(tempDestinationFile)

        val out: OutputStream = GZIPOutputStream(FileOutputStream(tempDestinationFile))
        val messageDigest = MessageDigest.getInstance("SHA-1")
        val buffer = ByteArray(ProtocolDefinition.DEFAULT_BUFFER_SIZE)
        var length: Int
        while (data.read(buffer, 0, buffer.size).also { length = it } != -1) {
            messageDigest.update(buffer, 0, length)
            out.write(buffer, 0, length)
        }
        out.close()
        val hash = Util.byteArrayToHexString(messageDigest.digest())

        // move file to desired destination
        val outputFile = File(getAssetDiskPath(hash))
        Util.createParentFolderIfNotExist(outputFile)
        if (outputFile.exists()) {
            // file is already extracted
            if (!tempDestinationFile.delete()) {
                throw IllegalStateException(String.format("Could not delete temporary file %s", tempDestinationFile.absolutePath))
            }
            return hash
        }
        if (!tempDestinationFile.renameTo(outputFile)) {
            throw IllegalStateException(String.format("Could not rename file %s to %s", tempDestinationFile.absolutePath, outputFile.absolutePath))
        }
        return hash
    }


    companion object {
        fun getAssetDiskPath(hash: String): String {
            if (hash.length != 40) {
                throw IllegalArgumentException("Invalid hash provided: $hash")
            }
            return StaticConfiguration.HOME_DIRECTORY + String.format("assets/objects/%s/%s.gz", hash.substring(0, 2), hash)
        }
    }
}
