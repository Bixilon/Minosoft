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

package de.bixilon.minosoft.assets.util

import com.github.luben.zstd.ZstdInputStream
import com.github.luben.zstd.ZstdOutputStream
import de.bixilon.kutil.array.ByteArrayUtil.toHex
import de.bixilon.kutil.hex.HexUtil.isHexString
import de.bixilon.kutil.random.RandomStringUtil.randomString
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.KUtil
import java.io.*
import java.net.URL
import java.nio.file.Files
import java.security.MessageDigest

object FileAssetsUtil {
    private val EMPTY_BYTE_ARRAY = ByteArray(0)
    private val BASE_PATH = RunConfiguration.HOME_DIRECTORY + "assets/objects/"

    fun getPath(hash: String): String {
        if (!hash.isHexString) {
            throw IllegalArgumentException("String is not a hex string. Invalid data or manipulated?: $hash")
        }
        return BASE_PATH + hash.substring(0, 2) + "/" + hash
    }

    fun downloadAsset(url: String, compress: Boolean = true, hashType: HashTypes = HashTypes.SHA256): String {
        return saveAndGet(URL(url).openStream(), compress, false, hashType).first
    }

    fun downloadAndGetAsset(url: String, compress: Boolean = true, hashType: HashTypes = HashTypes.SHA256): Pair<String, ByteArray> {
        return saveAndGet(URL(url).openStream(), compress, true, hashType)
    }

    fun saveAndGet(stream: InputStream, compress: Boolean = true, get: Boolean = true, hashType: HashTypes = HashTypes.SHA256): Pair<String, ByteArray> {
        var tempFile: File
        do {
            tempFile = File(RunConfiguration.TEMPORARY_FOLDER + KUtil.RANDOM.randomString(32))
        } while (tempFile.exists())
        tempFile.parentFile.apply {
            mkdirs()
            if (!isDirectory) {
                throw IllegalStateException("Could not create folder: $this")
            }
        }
        val returnStream = if (get) {
            ByteArrayOutputStream()
        } else {
            ByteArrayOutputStream(0)
        }
        val digest = hashType.createDigest()
        var output: OutputStream = FileOutputStream(tempFile)
        if (compress) {
            output = ZstdOutputStream(output, 5)
        }

        val buffer = ByteArray(ProtocolDefinition.DEFAULT_BUFFER_SIZE)
        var length: Int
        while (true) {
            length = stream.read(buffer, 0, buffer.size)
            if (length < 0) {
                break
            }
            output.write(buffer, 0, length)
            digest.update(buffer, 0, length)
            if (get) {
                returnStream.write(buffer, 0, length)
            }
        }
        output.close()
        val hash = digest.digest().toHex()

        val file = File(getPath(hash))
        if (file.exists()) {
            // already downloaded. This was a waste
            tempFile.delete()
            return Pair(hash, returnStream.toByteArray())
        }

        file.parentFile.apply {
            mkdirs()
            if (!isDirectory) {
                tempFile.delete()
                throw IllegalStateException("Could not create folder $this")
            }
        }
        Files.move(tempFile.toPath(), file.toPath())

        return Pair(hash, returnStream.toByteArray())
    }

    fun saveAsset(stream: InputStream, compress: Boolean = true, hashType: HashTypes = HashTypes.SHA256): String {
        return saveAndGet(stream, compress, false, hashType).first
    }

    fun saveAsset(data: ByteArray, compress: Boolean = true, hashType: HashTypes = HashTypes.SHA256): String {
        return saveAndGet(ByteArrayInputStream(data), compress, false, hashType).first
    }

    fun saveAndGetAsset(data: ByteArray, hashType: HashTypes = HashTypes.SHA256, compress: Boolean = true): Pair<String, ByteArray> {
        return saveAndGet(ByteArrayInputStream(data), compress, false, hashType)
    }

    fun String.toAssetName(verifyPrefix: Boolean = true): ResourceLocation? {
        if (verifyPrefix && !startsWith("assets/")) {
            return null
        }
        val split = removePrefix("assets/").split("/", limit = 2)
        if (split.size != 2) {
            return null
        }
        return ResourceLocation(split[0], split[1])
    }

    fun verifyAsset(hash: String, file: File = File(getPath(hash)), verify: Boolean, hashType: HashTypes = HashTypes.SHA256, compress: Boolean = true): Boolean {
        if (!file.exists()) {
            return false
        }
        if (!file.isFile) {
            throw IllegalStateException("File is not a file: $file")
        }
        val size = file.length()
        if (size < 0) {
            file.delete()
            return false
        }
        if (!verify) {
            return true
        }

        try {
            val digest = hashType.createDigest()

            var input: InputStream = FileInputStream(file)
            if (compress) {
                input = ZstdInputStream(input)
            }

            val buffer = ByteArray(ProtocolDefinition.DEFAULT_BUFFER_SIZE)
            var length: Int
            while (true) {
                length = input.read(buffer, 0, buffer.size)
                if (length < 0) {
                    break
                }
                digest.update(buffer, 0, length)
            }
            val equals = hash == digest.digest().toHex()
            if (!equals) {
                file.delete()
            }
            return equals
        } catch (exception: Throwable) {
            file.delete()
            return false
        }
    }

    enum class HashTypes(
        val digestName: String,
        val length: Int,
    ) {
        SHA1("SHA-1", 40),
        SHA256("SHA-256", 64),
        ;

        fun createDigest(): MessageDigest {
            return MessageDigest.getInstance(digestName)
        }
    }
}
