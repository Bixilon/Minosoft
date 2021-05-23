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

package de.bixilon.minosoft.gui.rendering.sound.sounds

import de.bixilon.minosoft.data.assets.AssetsManager
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import org.lwjgl.stb.STBVorbis
import org.lwjgl.stb.STBVorbisInfo
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.io.FileNotFoundException
import java.nio.ByteBuffer

data class Sound(
    val path: ResourceLocation,
    val weight: Int = 1,
    val volume: Float = 1.0f,
) {
    var loaded: Boolean = false
        private set
    var loadFailed: Boolean = false
        private set
    private var buffer: ByteBuffer? = null
    private var handle: Long = -1L

    fun load(assetsManager: AssetsManager) {
        if (loaded || loadFailed) {
            return
        }
        Log.log(LogMessageType.RENDERING_LOADING, LogLevels.VERBOSE) { "Loading audio file: $path" }
        try {

            val buffer = assetsManager.readByteAsset(path)
            this.buffer = buffer

            MemoryStack.stackPush().use { stack ->
                val error = stack.mallocInt(1)
                handle = STBVorbis.stb_vorbis_open_memory(buffer, error, null)
                if (handle == MemoryUtil.NULL) {
                    throw IllegalStateException("Can not load vorbis: ${path}: ${error[0]}")
                }
                val info = STBVorbisInfo.mallocStack(stack)
            }

            loaded = true
        } catch (exception: FileNotFoundException) {
            loadFailed = true
            Log.log(LogMessageType.RENDERING_LOADING, LogLevels.WARN) { "Can not load sound: $path: $exception" }
        }
    }
}
