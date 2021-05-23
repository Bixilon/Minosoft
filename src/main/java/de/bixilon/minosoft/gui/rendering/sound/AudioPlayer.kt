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

package de.bixilon.minosoft.gui.rendering.sound

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.sounds.SoundEvent
import de.bixilon.minosoft.gui.rendering.Rendering
import de.bixilon.minosoft.gui.rendering.sound.sounds.Sound
import de.bixilon.minosoft.gui.rendering.sound.sounds.SoundList
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.CountUpAndDownLatch
import de.bixilon.minosoft.util.KUtil.asResourceLocation
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import org.lwjgl.openal.AL
import org.lwjgl.openal.AL10.*
import org.lwjgl.openal.ALC
import org.lwjgl.openal.ALC10.*
import org.lwjgl.openal.EXTThreadLocalContext.alcSetThreadContext
import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer


class AudioPlayer(
    val connection: PlayConnection,
    val rendering: Rendering,
) {
    private val sounds: MutableMap<SoundEvent, SoundList> = mutableMapOf()

    var initialized = false
        private set

    private var device = 0L
    private var context = 0L

    private var source = 0

    private var pcm: ShortBuffer? = null


    fun init(latch: CountUpAndDownLatch) {
        Log.log(LogMessageType.RENDERING_LOADING, LogLevels.INFO) { "Loading OpenAL..." }

        Log.log(LogMessageType.RENDERING_LOADING, LogLevels.VERBOSE) { "Loading sounds.json" }
        loadSounds()
        Log.log(LogMessageType.RENDERING_LOADING, LogLevels.VERBOSE) { "Loading all sounds into memory" }
        for (soundList in sounds.values) {
            for (sound in soundList.sounds) {
                // sound.load(connection.assetsManager)
            }
        }


        Log.log(LogMessageType.RENDERING_LOADING, LogLevels.VERBOSE) { "Initializing OpenAL..." }
        device = alcOpenDevice(null as ByteBuffer?)
        check(device != MemoryUtil.NULL) { "Failed to open the default device." }

        context = alcCreateContext(device, null as IntBuffer?)
        check(context != MemoryUtil.NULL) { "Failed to create an OpenAL context." }

        alcSetThreadContext(context)

        val deviceCaps = ALC.createCapabilities(device)
        AL.createCapabilities(deviceCaps)

        val listenerOri = floatArrayOf(0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f)

        alListener3f(AL_POSITION, 0f, 0f, 1.0f)
        alListener3f(AL_VELOCITY, 0f, 0f, 0f)
        alListenerfv(AL_ORIENTATION, listenerOri)

        val source = alGenSources()

        alSourcef(source, AL_PITCH, 1.0f)
        alSourcef(source, AL_GAIN, 1.0f)
        alSource3f(source, AL_POSITION, 0.0f, 0.0f, 0.0f)
        alSource3f(source, AL_VELOCITY, 0.0f, 0.0f, 0.0f)
        alSourcei(source, AL_LOOPING, AL_FALSE)


        // Testing, ToDo
        val buffer = alGenBuffers()

        val soundEvent = sounds[connection.registries.soundEventRegistry[0]]!!.getRandom()



        Log.log(LogMessageType.RENDERING_LOADING, LogLevels.INFO) { "OpenAL loaded!" }
        initialized = true
        latch.countDown()
    }

    fun startLoop() {
        while (connection.isConnected) {
            Thread.sleep(1L)
        }
    }

    fun exit() {
        //  alDeleteBuffers(buffers)
        alDeleteSources(source)

        //MemoryUtil.memFree(buffers)
        MemoryUtil.memFree(pcm)

        alcSetThreadContext(MemoryUtil.NULL)
        alcDestroyContext(context)
        alcCloseDevice(device)
    }

    private fun loadSounds() {
        val data = connection.assetsManager.readJsonAsset(SOUNDS_INDEX_FILE)

        for ((soundEventResourceLocation, json) in data.entrySet()) {
            check(json is JsonObject)
            val soundEvent = connection.registries.soundEventRegistry[ResourceLocation(ProtocolDefinition.DEFAULT_NAMESPACE, soundEventResourceLocation)]!!

            val sounds: MutableSet<Sound> = mutableSetOf()

            fun String.getSoundLocation(): ResourceLocation {
                return ResourceLocation(ProtocolDefinition.DEFAULT_NAMESPACE, "sounds/${this}".replace('.', '/') + ".ogg")
            }

            for (soundJson in json["sounds"].asJsonArray) {
                when (soundJson) {
                    is JsonPrimitive -> {
                        sounds += Sound(soundJson.asString.getSoundLocation())
                    }
                    is JsonObject -> {
                        sounds += Sound(
                            path = soundJson["name"].asString.getSoundLocation(),
                            weight = soundJson["weight"]?.asInt ?: 1,
                            volume = soundJson["volume"]?.asFloat ?: 1.0f,
                        )
                    }
                    is JsonArray -> TODO()
                }
            }
            this.sounds[soundEvent] = SoundList(
                soundEvent = soundEvent,
                sounds = sounds.toSet(),
                subTitle = json["subtitle"]?.asString?.let { ResourceLocation(ProtocolDefinition.DEFAULT_NAMESPACE, it) },
            )
        }
    }


    companion object {
        private val SOUNDS_INDEX_FILE = "minecraft:sounds.json".asResourceLocation()
    }
}
