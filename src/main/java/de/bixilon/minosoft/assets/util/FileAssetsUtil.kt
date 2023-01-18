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

package de.bixilon.minosoft.assets.util

import com.github.luben.zstd.ZstdInputStream
import com.github.luben.zstd.ZstdOutputStream
import de.bixilon.kutil.array.ByteArrayUtil.toHex
import de.bixilon.kutil.file.FileUtil.createParent
import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.assets.util.FileUtil.copy
import de.bixilon.minosoft.assets.util.HashTypes.Companion.hashType
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.io.*
import java.nio.file.Files

object FileAssetsUtil {

    private fun save(input: InputStream, type: String, compress: Boolean, hash: HashTypes, close: Boolean, store: OutputStream?): String {
        val temp = FileUtil.createTempFile()
        temp.createParent()

        val digest = hash.createDigest()
        val output = FileOutputStream(temp).upgrade(compress)

        if (store == null) {
            input.copy(output, digest = digest)
        } else {
            input.copy(output, store, digest = digest)
        }

        if (close) input.close()
        output.close()

        val hash = digest.digest().toHex()

        val file = PathUtil.getAssetsPath(hash = hash, type = type).toFile()

        if (file.exists()) {
            Log.log(LogMessageType.ASSETS, LogLevels.WARN) { "Storing of file (type=$type, hash=$hash) was useless! Please check you code to avoid bandwidth usage!" }
            temp.delete()
            return hash
        }

        file.createParent()

        Files.move(temp.toPath(), file.toPath())

        return hash
    }

    fun save(input: InputStream, type: String, compress: Boolean = true, hash: HashTypes = HashTypes.SHA256, close: Boolean = true): String {
        return save(input, type, compress, hash, close, null)
    }

    fun read(input: InputStream, type: String, compress: Boolean = true, hash: HashTypes = HashTypes.SHA256, close: Boolean = true): SavedAsset {
        val output = ByteArrayOutputStream()
        val hash = save(input, type, compress, hash, close, output)
        return SavedAsset(hash, output.toByteArray())
    }

    fun verify(hash: String, type: String, lazy: Boolean = true, compress: Boolean = true): Boolean {
        val file = PathUtil.getAssetsPath(hash = hash, type = type).toFile()

        if (!file.verify()) return false

        if (lazy) return true

        val digest = hash.hashType.createDigest()
        val stream = FileInputStream(file).upgrade(compress)

        stream.copy(digest = digest)

        stream.close()

        if (digest.digest().toHex() != hash) {
            file.delete()
            return false
        }
        return true
    }

    fun read(hash: String, type: String, verify: Boolean = true, compress: Boolean = true): ByteArray {
        val file = PathUtil.getAssetsPath(hash = hash, type = type).toFile()

        if (!file.verify()) throw IOException("Invalid file: $file")

        val digest = hash.hashType.createDigest()
        val stream = FileInputStream(file).upgrade(compress)
        val output = ByteArrayOutputStream(stream.available())

        stream.copy(output, digest = if (verify) digest else null)

        stream.close()

        val result = digest.digest().toHex()
        if (result != hash) {
            file.delete()
            throw IOException("Hash does not match (type=$type, expected=$hash, hash=$hash)")
        }
        return output.toByteArray()
    }

    fun readOrNull(hash: String, type: String, verify: Boolean = true, compress: Boolean = true): ByteArray? {
        val file = PathUtil.getAssetsPath(hash = hash, type = type).toFile()

        if (!file.verify()) return null

        val digest = hash.hashType.createDigest()
        val stream = FileInputStream(file).upgrade(compress)
        val output = ByteArrayOutputStream(stream.available())

        stream.copy(output, digest = if (verify) digest else null)

        stream.close()

        val result = digest.digest().toHex()
        if (result != hash) {
            Log.log(LogMessageType.ASSETS, LogLevels.WARN) { "Asset stored corrupted: (type=$type, hash=$hash)" }
            file.delete()
            return null
        }
        return output.toByteArray()
    }


    fun String.toAssetName(verifyPrefix: Boolean = true, prefix: String = AssetsManager.DEFAULT_ASSETS_PREFIX): ResourceLocation? {
        if (verifyPrefix && !startsWith("$prefix/")) {
            return null
        }
        val split = removePrefix("$prefix/").split("/", limit = 2)
        if (split.size != 2) {
            return null
        }
        return ResourceLocation(split[0], split[1])
    }

    private fun OutputStream.upgrade(compress: Boolean): OutputStream {
        if (compress && AssetsOptions.COMPRESS_ASSETS) {
            return ZstdOutputStream(this)
        }
        return this
    }

    private fun InputStream.upgrade(compress: Boolean): InputStream {
        if (compress && AssetsOptions.COMPRESS_ASSETS) {
            return ZstdInputStream(this)
        }
        return this
    }

    private fun File.verify(): Boolean {
        if (!exists() || !isFile) {
            return false
        }
        val size = length()
        if (size < 0) {
            delete()
            return false
        }
        return true
    }
}
