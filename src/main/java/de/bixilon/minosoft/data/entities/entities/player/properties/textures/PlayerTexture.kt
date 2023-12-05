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

package de.bixilon.minosoft.data.entities.entities.player.properties.textures

import com.fasterxml.jackson.annotation.JsonIgnore
import de.bixilon.kutil.hex.HexUtil.isHexString
import de.bixilon.kutil.string.StringUtil.fill
import de.bixilon.kutil.url.URLUtil.checkWeb
import de.bixilon.minosoft.assets.util.FileAssetsTypes
import de.bixilon.minosoft.assets.util.FileAssetsUtil
import de.bixilon.minosoft.assets.util.HashTypes
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.net.URL

open class PlayerTexture(
    url: URL,
) {
    @JsonIgnore
    var data: ByteArray? = null
        private set

    val url = if (url.protocol == "http") URL("https://" + url.toString().removePrefix("http://")) else url

    init {
        url.checkWeb()
    }

    @JsonIgnore
    fun getHash(): String {
        when (url.host) {
            "textures.minecraft.net" -> {
                val hash = url.file.split("/").last().fill('0', HashTypes.SHA256.length)
                if (hash.length != HashTypes.SHA256.length || !hash.isHexString) {
                    throw IllegalArgumentException("Invalid hash: $hash")
                }
                return hash
            }

            else -> TODO("Can not get texture identifier: $url")
        }
    }

    @Synchronized
    fun read(): ByteArray {
        this.data?.let { return it }
        val hash = getHash()

        FileAssetsUtil.readOrNull(hash, FileAssetsTypes.SKINS)?.let {
            this.data = it
            return it
        }

        val input = url.openStream()
        if (input.available() > MAX_TEXTURE_SIZE) {
            throw IllegalStateException("Texture is too big: ${input.available()}!")
        }
        val asset = FileAssetsUtil.read(input, type = FileAssetsTypes.SKINS, hash = HashTypes.SHA256)
        Log.log(LogMessageType.ASSETS, LogLevels.VERBOSE) { "Downloaded player texture ($url)" }
        return asset.data
    }

    companion object {
        private const val MAX_TEXTURE_SIZE = 64 * 64 * 4 + 100 // width * height * rgba + some padding. Note: normal textures are compressed, so they are roughly 2kb in size
    }
}
