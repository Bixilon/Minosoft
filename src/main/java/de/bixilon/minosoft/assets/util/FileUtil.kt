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
import de.bixilon.kutil.buffer.BufferDefinition
import de.bixilon.kutil.random.RandomStringUtil.randomString
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.KUtil
import java.io.*
import java.nio.file.Path
import java.security.MessageDigest

object FileUtil {

    fun safeReadFile(path: Path, compressed: Boolean = true): InputStream? {
        return safeReadFile(path.toFile(), compressed)
    }

    fun safeReadFile(file: File, compressed: Boolean = true): InputStream? {
        if (!file.exists()) {
            return null
        }
        return readFile(file, compressed)
    }

    fun readFile(file: File, compressed: Boolean = true): InputStream {
        var stream: InputStream = FileInputStream(file)
        if (compressed) {
            stream = ZstdInputStream(stream)
        }

        return stream
    }

    fun readFile(path: Path, compressed: Boolean = true): InputStream {
        return readFile(path.toFile(), compressed)
    }

    fun File.mkdirParent() {
        val parent = this.parentFile
        if (parent.exists()) {
            return
        }
        if (!parent.mkdirs()) {
            throw IOException("Can not create parent of $this")
        }
    }


    fun createTempFile(): File {
        var file: File

        for (i in 0 until AssetsOptions.MAX_FILE_CHECKING) {
            file = RunConfiguration.TEMPORARY_FOLDER.resolve(KUtil.RANDOM.randomString(32)).toFile()
            if (!file.exists()) {
                return file
            }
        }

        throw IOException("Can not find temporary file after ${AssetsOptions.MAX_FILE_CHECKING} tries!")
    }

    fun InputStream.copy(vararg output: OutputStream, digest: MessageDigest?) {
        val buffer = ByteArray(BufferDefinition.DEFAULT_BUFFER_SIZE)

        while (true) {
            val length = read(buffer, 0, buffer.size)
            if (length < 0) {
                break
            }
            for (stream in output) {
                stream.write(buffer, 0, length)
            }
            digest?.update(buffer, 0, length)
        }
    }
}
