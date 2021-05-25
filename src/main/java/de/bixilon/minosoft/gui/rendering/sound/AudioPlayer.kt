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
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.sounds.SoundEvent
import de.bixilon.minosoft.gui.rendering.Rendering
import de.bixilon.minosoft.gui.rendering.input.camera.Camera
import de.bixilon.minosoft.gui.rendering.modding.events.CameraPositionChangeEvent
import de.bixilon.minosoft.gui.rendering.sound.sounds.Sound
import de.bixilon.minosoft.gui.rendering.sound.sounds.SoundList
import de.bixilon.minosoft.gui.rendering.util.VecUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3
import de.bixilon.minosoft.modding.event.CallbackEventInvoker
import de.bixilon.minosoft.modding.event.events.PlaySoundEvent
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ConnectionStates
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.CountUpAndDownLatch
import de.bixilon.minosoft.util.KUtil.asResourceLocation
import de.bixilon.minosoft.util.KUtil.synchronizedListOf
import de.bixilon.minosoft.util.KUtil.toSynchronizedList
import de.bixilon.minosoft.util.Queue
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import glm_.vec3.Vec3
import org.lwjgl.openal.AL
import org.lwjgl.openal.ALC
import org.lwjgl.openal.ALC10.*
import org.lwjgl.openal.EXTThreadLocalContext.alcSetThreadContext
import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer
import java.nio.IntBuffer


class AudioPlayer(
    val connection: PlayConnection,
    val rendering: Rendering,
) {
    private val sounds: MutableMap<SoundEvent, SoundList> = mutableMapOf()

    var initialized = false
        private set

    private var device = 0L
    private var context = 0L

    private val queue = Queue()
    private lateinit var listener: SoundListener
    private val sources: MutableList<SoundSource> = synchronizedListOf()


    private fun preloadSounds() {
        Log.log(LogMessageType.RENDERING_LOADING, LogLevels.VERBOSE) { "Preloading sounds..." }
        if (SoundConstants.DISABLE_PRELOADING) {
            return
        }

        for (soundList in sounds.values) {
            for (sound in soundList.sounds) {
                if (SoundConstants.PRELOAD_ALL_SOUNDS || sound.preload) {
                    sound.load(connection.assetsManager)
                }
            }
        }
    }


    fun init(latch: CountUpAndDownLatch) {
        Log.log(LogMessageType.AUDIO_LOADING, LogLevels.INFO) { "Loading OpenAL..." }

        loadSounds()
        preloadSounds()



        Log.log(LogMessageType.AUDIO_LOADING, LogLevels.VERBOSE) { "Initializing OpenAL..." }
        device = alcOpenDevice(null as ByteBuffer?)
        check(device != MemoryUtil.NULL) { "Failed to open the default device." }

        context = alcCreateContext(device, null as IntBuffer?)
        check(context != MemoryUtil.NULL) { "Failed to create an OpenAL context." }

        alcSetThreadContext(context)

        val deviceCaps = ALC.createCapabilities(device)
        AL.createCapabilities(deviceCaps)

        listener = SoundListener()

        connection.registerEvent(CallbackEventInvoker.of<CameraPositionChangeEvent> {
            queue += {
                listener.position = it.newPosition
                listener.setOrientation(it.renderWindow.inputHandler.camera.cameraFront, Camera.CAMERA_UP_VEC3)
            }
        })

        connection.registerEvent(CallbackEventInvoker.of<PlaySoundEvent> {
            playSoundEvent(it.soundEvent, it.position.toVec3, it.volume, it.pitch)
        })

        Log.log(LogMessageType.AUDIO_LOADING, LogLevels.INFO) { "OpenAL loaded!" }


        initialized = true
        latch.dec()
    }

    fun playSoundEvent(soundEvent: SoundEvent, position: Vec3? = null, volume: Float = 1.0f, pitch: Float = 1.0f) {
        if (!initialized) {
            return
        }
        playSound(sounds[soundEvent]!!.getRandom(), position, volume, pitch)
    }

    private fun getAvailableSource(): SoundSource? {
        for (source in sources.toSynchronizedList()) {
            if (source.available) {
                return source
            }
        }
        // no source available
        if (sources.size > SoundConstants.MAX_SOURCES_AMOUNT) {
            return null
        }
        val source = SoundSource()
        sources += source

        return source
    }


    private fun playSound(sound: Sound, position: Vec3? = null, volume: Float = 1.0f, pitch: Float = 1.0f) {
        queue += add@{
            sound.load(connection.assetsManager)
            if (sound.loadFailed) {
                return@add
            }
            val source = getAvailableSource() ?: let {
                // ToDo: Queue sound for later (and check a certain delay to not make the game feel laggy)
                Log.log(LogMessageType.AUDIO_LOADING, LogLevels.WARN) { "Can not play sound: No source available!" }
                return@add
            }
            position?.let {
                source.relative = false
                source.position = it
            } ?: let {
                source.position = Vec3.EMPTY
                source.relative = true
            }
            source.sound = sound
            source.pitch = pitch * sound.pitch
            source.gain = volume * sound.volume * Minosoft.config.config.game.sound.masterVolume
            source.play()
        }
    }

    fun startLoop() {
        while (connection.connectionState != ConnectionStates.DISCONNECTING) {
            queue.work()
            Thread.sleep(1L)
        }
    }

    fun exit() {
        Log.log(LogMessageType.AUDIO_LOADING, LogLevels.INFO) { "Unloading OpenAL..." }

        Log.log(LogMessageType.AUDIO_LOADING, LogLevels.VERBOSE) { "Unloading sounds..." }
        for (soundList in sounds.values) {
            for (sound in soundList.sounds) {
                sound.unload()
            }
        }
        Log.log(LogMessageType.AUDIO_LOADING, LogLevels.VERBOSE) { "Unloading sources..." }
        for (source in sources.toSynchronizedList()) {
            source.unload()
        }

        Log.log(LogMessageType.AUDIO_LOADING, LogLevels.VERBOSE) { "Destroying OpenAL context..." }

        alcSetThreadContext(MemoryUtil.NULL)
        alcDestroyContext(context)
        alcCloseDevice(device)
        Log.log(LogMessageType.AUDIO_LOADING, LogLevels.INFO) { "Unloaded OpenAL!" }
    }

    private fun loadSounds() {
        Log.log(LogMessageType.RENDERING_LOADING, LogLevels.VERBOSE) { "Loading sounds.json" }
        val data = connection.assetsManager.readJsonAsset(SOUNDS_INDEX_FILE)

        for ((soundEventResourceLocation, json) in data.entrySet()) {
            check(json is JsonObject)
            val soundEvent = connection.registries.soundEventRegistry[ResourceLocation(soundEventResourceLocation)]!!

            val sounds: MutableSet<Sound> = mutableSetOf()

            fun String.getSoundLocation(): ResourceLocation {
                return ResourceLocation(ProtocolDefinition.DEFAULT_NAMESPACE, "sounds/${this}".replace('.', '/') + ".ogg") // ToDo: Resource Location
            }

            for (soundJson in json["sounds"].asJsonArray) {
                when (soundJson) {
                    is JsonPrimitive -> {
                        sounds += Sound(soundJson.asString.getSoundLocation())
                    }
                    is JsonObject -> {
                        sounds += Sound(
                            path = soundJson["name"].asString.getSoundLocation(),
                            volume = soundJson["volume"]?.asFloat ?: 1.0f,
                            pitch = soundJson["pitch"]?.asFloat ?: 1.0f,
                            weight = soundJson["weight"]?.asInt ?: 1,
                            stream = soundJson["stream"]?.asBoolean ?: false,
                            attenuationDistance = soundJson["attenuation_distance"]?.asInt ?: 16,
                            preload = soundJson["preload"]?.asBoolean ?: false,
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
