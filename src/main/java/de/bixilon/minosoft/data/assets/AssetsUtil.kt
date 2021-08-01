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

import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.terminal.RunConfiguration
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.zip.GZIPInputStream

object AssetsUtil {
    fun getAssetDiskPath(hash: String, compress: Boolean): String {
        if (hash.length != 40) {
            throw IllegalArgumentException("Invalid hash provided: $hash")
        }
        var path = RunConfiguration.HOME_DIRECTORY + "assets/objects/${hash.substring(0, 2)}/$hash"

        if (compress) {
            path += ".gz"
        }

        return path
    }

    fun readAsset(hash: String, compress: Boolean): ByteArray {
        val inputStream = readAssetAsStream(hash, compress)

        val output = ByteArrayOutputStream()
        val buffer = ByteArray(ProtocolDefinition.DEFAULT_BUFFER_SIZE)
        var length: Int
        while (inputStream.read(buffer, 0, buffer.size).also { length = it } != -1) {
            output.write(buffer, 0, length)
        }

        return output.toByteArray()
    }

    fun readAssetAsStream(hash: String, compress: Boolean): InputStream {
        val inputStream = FileInputStream(getAssetDiskPath(hash, compress))
        if (compress) {
            return GZIPInputStream(inputStream)
        }
        return inputStream
    }

    fun deleteAsset(hash: String, compress: Boolean) {
        File(getAssetDiskPath(hash, compress)).delete()
    }
}
