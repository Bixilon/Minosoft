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
import de.bixilon.minosoft.gui.rendering.sound.SoundUtil.sound
import de.bixilon.minosoft.util.KUtil.toBoolean
import de.bixilon.minosoft.util.KUtil.toFloat
import de.bixilon.minosoft.util.KUtil.toInt
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.io.FileNotFoundException

data class Sound(
    val soundEvent: ResourceLocation,
    val path: ResourceLocation,
    val volume: Float = 1.0f,
    val pitch: Float = 1.0f,
    val weight: Int = 1,
    val stream: Boolean = false, // ToDo: Implement
    val attenuationDistance: Int = 16, // ToDo: Implement
    val preload: Boolean = false,
) {
    var data: SoundData? = null
        private set
    var buffer: OpenALBuffer? = null
        private set

    @Synchronized
    fun load(assetsManager: AssetsManager) {
        if (data != null) {
            return
        }
        Log.log(LogMessageType.AUDIO_LOADING, LogLevels.VERBOSE) { "Loading audio file: $path" }
        try {
            val data = SoundData(assetsManager, this)
            this.data = data
            this.buffer = OpenALBuffer(data)
        } catch (exception: FileNotFoundException) {
            Log.log(LogMessageType.AUDIO_LOADING, LogLevels.WARN) { "Can not load sound: $path: $exception" }
        }
    }

    @Synchronized
    fun unload() {
        data?.unload()
        buffer?.unload()
    }

    protected fun finalize() {
        unload()
    }

    companion object {

        operator fun invoke(soundEvent: ResourceLocation, data: Any): Sound {
            if (data is String) {
                return Sound(
                    soundEvent = soundEvent,
                    path = data.toResourceLocation().sound(),
                )
            }

            check(data is Map<*, *>)

            // ToDo: "type" attribute: event

            return Sound(
                soundEvent = soundEvent,
                path = data["name"].toResourceLocation().sound(),
                volume = data["volume"]?.toFloat() ?: 1.0f,
                pitch = data["pitch"]?.toFloat() ?: 1.0f,
                weight = data["weight"]?.toInt() ?: 1,
                stream = data["stream"]?.toBoolean() ?: false,
                attenuationDistance = data["attenuation_distance"]?.toInt() ?: 16,
                preload = data["preload"]?.toBoolean() ?: false,
            )
        }
    }
}
