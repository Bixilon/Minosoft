package de.bixilon.minosoft.gui.rendering.sound.sounds

import de.bixilon.minosoft.assets.AssetsManager
import org.lwjgl.BufferUtils
import org.lwjgl.openal.AL10.AL_FORMAT_MONO16
import org.lwjgl.openal.AL10.AL_FORMAT_STEREO16
import org.lwjgl.stb.STBVorbis.*
import org.lwjgl.stb.STBVorbisInfo
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.MemoryUtil.memFree
import java.nio.ByteBuffer
import java.nio.ShortBuffer

class SoundData(
    val vorbis: Long,
    val format: Int,
    val buffer: ByteBuffer,
    val length: Long,
    val channels: Int,
    val sampleRate: Int,
    val samplesLength: Int,
    val sampleSeconds: Float,
) {
    private var unloaded = false

    @Synchronized
    fun unload() {
        if (unloaded) {
            return
        }
        memFree(buffer)
        unloaded = true
    }

    protected fun finalize() {
        unload()
    }

    fun createPCM(): ShortBuffer {
        val pcm = BufferUtils.createShortBuffer(samplesLength)
        pcm.limit(stb_vorbis_get_samples_short_interleaved(vorbis, channels, pcm) * channels)
        return pcm
    }

    companion object {

        operator fun invoke(assetsManager: AssetsManager, sound: Sound): SoundData {
            val vorbisData = assetsManager[sound.path].readAllBytes()
            val buffer = BufferUtils.createByteBuffer(vorbisData.size)
            buffer.put(vorbisData)
            buffer.rewind()

            val error = BufferUtils.createIntBuffer(1)
            val vorbis = stb_vorbis_open_memory(buffer, error, null)
            if (vorbis == MemoryUtil.NULL) {
                throw IllegalStateException("Can not load vorbis: ${sound.path}: ${error[0]}")
            }
            val info = stb_vorbis_get_info(vorbis, STBVorbisInfo.malloc())
            val channels = info.channels()
            val format = when (channels) {
                1 -> AL_FORMAT_MONO16
                2 -> AL_FORMAT_STEREO16
                else -> error("Don't know vorbis channels: $channels")
            }
            val sampleRate = info.sample_rate()

            val samplesLength = stb_vorbis_stream_length_in_samples(vorbis)
            val sampleSeconds = stb_vorbis_stream_length_in_seconds(vorbis)
            val length = (sampleSeconds * 1000).toLong()

            return SoundData(
                vorbis = vorbis,
                format = format,
                buffer = buffer,
                length = length,
                channels = channels,
                sampleRate = sampleRate,
                samplesLength = samplesLength,
                sampleSeconds = sampleSeconds,
            )
        }
    }
}
