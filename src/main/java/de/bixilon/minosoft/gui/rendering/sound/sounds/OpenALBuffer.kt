package de.bixilon.minosoft.gui.rendering.sound.sounds

import org.lwjgl.openal.AL10.*
import org.lwjgl.system.MemoryUtil.memFree
import java.nio.ShortBuffer

class OpenALBuffer(
    val data: SoundData,
) {
    val buffer: Int
    private val pcm: ShortBuffer
    var unloaded: Boolean = false
        private set

    init {
        val pcm = data.createPCM()
        this.pcm = pcm

        this.buffer = alGenBuffers()

        alBufferData(buffer, data.format, pcm, data.sampleRate)
    }

    @Synchronized
    fun unload() {
        if (unloaded) {
            return
        }
        alDeleteBuffers(buffer)
        memFree(pcm)
        unloaded = true
    }

    protected fun finalize() {
        unload()
    }

}
