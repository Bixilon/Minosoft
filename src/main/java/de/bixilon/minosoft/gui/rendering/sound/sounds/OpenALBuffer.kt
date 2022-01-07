/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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
