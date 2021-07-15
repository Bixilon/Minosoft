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
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import org.lwjgl.BufferUtils
import org.lwjgl.openal.AL10.*
import org.lwjgl.stb.STBVorbis.*
import org.lwjgl.stb.STBVorbisInfo
import org.lwjgl.system.MemoryUtil
import java.io.FileNotFoundException
import java.nio.ByteBuffer

data class Sound(
    val path: ResourceLocation,
    val volume: Float = 1.0f,
    val pitch: Float = 1.0f,
    val weight: Int = 1,
    val stream: Boolean = false, // ToDo
    val attenuationDistance: Int = 16,
    val preload: Boolean = false,
    // ToDo: type
) {
    var length: Long = -1L
        private set
    var loaded: Boolean = false
        private set
    var loadFailed: Boolean = false
        private set
    var channels: Int = -1
        private set
    var sampleRate: Int = -1
        private set
    var samplesLength: Int = -1
        private set
    var sampleSeconds: Float = -1.0f
        private set
    var buffer = -1
        private set

    private var vorbisBuffer: ByteBuffer? = null

    @Synchronized
    fun load(assetsManager: AssetsManager) {
        if (loaded || loadFailed) {
            return
        }
        Log.log(LogMessageType.AUDIO_LOADING, LogLevels.VERBOSE) { "Loading audio file: $path" }
        try {
            val vorbisBuffer = assetsManager.readByteAsset(path)
            this.vorbisBuffer = vorbisBuffer

            val error = BufferUtils.createIntBuffer(1)
            val vorbis = stb_vorbis_open_memory(vorbisBuffer, error, null)
            if (vorbis == MemoryUtil.NULL) {
                throw IllegalStateException("Can not load vorbis: ${path}: ${error[0]}")
            }
            val info = stb_vorbis_get_info(vorbis, STBVorbisInfo.malloc())
            channels = info.channels()
            val format = when (channels) {
                1 -> AL_FORMAT_MONO16
                2 -> AL_FORMAT_STEREO16
                else -> TODO("Channels: $channels")
            }
            sampleRate = info.sample_rate()

            samplesLength = stb_vorbis_stream_length_in_samples(vorbis)
            sampleSeconds = stb_vorbis_stream_length_in_seconds(vorbis)
            length = (sampleSeconds * 1000).toLong()


            val pcm = BufferUtils.createShortBuffer(samplesLength)

            pcm.limit(stb_vorbis_get_samples_short_interleaved(vorbis, channels, pcm) * channels)
            //ToDo: Somehow crashed?: MemoryUtil.memFree(vorbisBuffer)

            this.buffer = alGenBuffers()

            alBufferData(buffer, format, pcm, sampleRate)
            loaded = true
        } catch (exception: FileNotFoundException) {
            loadFailed = true
            Log.log(LogMessageType.AUDIO_LOADING, LogLevels.WARN) { "Can not load sound: $path: $exception" }
        }
    }

    @Synchronized
    fun unload() {
        if (!loaded) {
            return
        }
        alDeleteBuffers(buffer)
        vorbisBuffer?.let { MemoryUtil.memFree(it) }
        buffer = -1
        channels = -1
        sampleRate = -1
        samplesLength = -1
        sampleSeconds = -1.0f
        loaded = false
    }
}
