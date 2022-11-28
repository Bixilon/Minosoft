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

package de.bixilon.minosoft.gui.rendering.sound

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.collections.CollectionUtil.synchronizedListOf
import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedList
import de.bixilon.kutil.concurrent.queue.Queue
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.world.audio.AbstractAudioPlayer
import de.bixilon.minosoft.gui.rendering.Rendering
import de.bixilon.minosoft.gui.rendering.camera.CameraDefinition.CAMERA_UP_VEC3
import de.bixilon.minosoft.gui.rendering.events.CameraPositionChangeEvent
import de.bixilon.minosoft.gui.rendering.sound.sounds.Sound
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
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
) : AbstractAudioPlayer {
    private val profile = connection.profiles.audio
    private val soundManager = SoundManager(connection)
    var initialized = false
        private set

    private var device = -1L
    private var context = -1L

    private val queue = Queue()
    private lateinit var listener: SoundListener
    private val sources: MutableList<SoundSource> = synchronizedListOf()

    var availableSources: Int = 0
        private set

    val sourcesCount: Int
        get() = sources.size

    private var enabled = profile.enabled


    fun init(latch: CountUpAndDownLatch) {
        Log.log(LogMessageType.AUDIO, LogLevels.INFO) { "Loading OpenAL..." }

        soundManager.load()
        Log.log(LogMessageType.AUDIO, LogLevels.VERBOSE) { "Preloading sounds..." }
        soundManager.preload()



        Log.log(LogMessageType.AUDIO, LogLevels.VERBOSE) { "Initializing OpenAL..." }
        device = alcOpenDevice(null as ByteBuffer?)
        check(device != MemoryUtil.NULL) { "Failed to open the default device." }

        context = alcCreateContext(device, null as IntBuffer?)
        check(context != MemoryUtil.NULL) { "Failed to create an OpenAL context." }

        alcSetThreadContext(context)

        val deviceCaps = ALC.createCapabilities(device)
        AL.createCapabilities(deviceCaps)

        listener = SoundListener()

        val volumeConfig = connection.profiles.audio.volume

        listener.masterVolume = volumeConfig.master
        volumeConfig::master.observe(this) { queue += { listener.masterVolume = it } }

        connection.events.listen<CameraPositionChangeEvent> {
            queue += {
                listener.position = Vec3(it.newPosition)
                listener.setOrientation(it.renderWindow.camera.view.view.front, CAMERA_UP_VEC3)
            }
        }

        DefaultAudioBehavior.register(connection)

        Log.log(LogMessageType.AUDIO, LogLevels.INFO) { "OpenAL loaded!" }

        profile::enabled.observe(this, false) {
            if (it) {
                enabled = true
                return@observe
            }
            queue += {
                for (source in sources) {
                    source.stop()
                }
                enabled = false
            }
        }
        initialized = true
        connection.world.audioPlayer = this
        latch.dec()
    }

    override fun playSound(sound: ResourceLocation, position: Vec3?, volume: Float, pitch: Float) {
        if (!initialized) {
            return
        }
        queue += add@{ playSound(soundManager[sound] ?: return@add, position, volume, pitch) }
    }

    override fun play2DSound(sound: ResourceLocation, volume: Float, pitch: Float) {
        if (!connection.profiles.audio.gui.enabled) {
            return
        }
        super.play2DSound(sound, volume, pitch)
    }

    override fun stopSound(sound: ResourceLocation) {
        if (!profile.enabled) {
            return
        }
        queue += {
            for (source in sources) {
                if (!source.isPlaying) {
                    continue
                }
                if (source.sound?.soundEvent != sound) {
                    continue
                }
                source.stop()
            }
        }
    }

    override fun stopAllSounds() {
        queue += {
            for (source in sources) {
                if (!source.isPlaying) {
                    continue
                }
                source.stop()
            }
        }
    }

    @Synchronized
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
        if (!profile.enabled) {
            return
        }
        position?.let {
            val distance = (this.listener.position - it).length()
            if (distance >= sound.attenuationDistance) {
                return
            }
        }
        queue += add@{
            sound.load(connection.assetsManager)
            val source = getAvailableSource()
            if (source == null) {
                Log.log(LogMessageType.AUDIO, LogLevels.WARN) { "No source available: $sound" }
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
            source.gain = volume * sound.volume
            source.play()
        }
    }

    private fun calculateAvailableSources() {
        var availableSources = 0
        for (source in sources) {
            if (source.available) {
                availableSources++
            }
        }
        this.availableSources = availableSources
    }

    fun startLoop() {
        while (true) {
            if (connection.wasConnected || connection.error != null) {
                break
            }
            queue.work()
            calculateAvailableSources()
            while (!enabled) {
                Thread.sleep(1L)
            }
            Thread.sleep(1L)
        }
    }

    fun exit() {
        Log.log(LogMessageType.AUDIO, LogLevels.INFO) { "Unloading OpenAL..." }

        Log.log(LogMessageType.AUDIO, LogLevels.VERBOSE) { "Unloading sounds..." }
        soundManager.unload()

        Log.log(LogMessageType.AUDIO, LogLevels.VERBOSE) { "Unloading sources..." }
        for (source in sources.toSynchronizedList()) {
            source.unload()
        }

        Log.log(LogMessageType.AUDIO, LogLevels.VERBOSE) { "Destroying OpenAL context..." }

        alcSetThreadContext(MemoryUtil.NULL)
        alcDestroyContext(context)
        alcCloseDevice(device)

        Log.log(LogMessageType.AUDIO, LogLevels.INFO) { "Unloaded OpenAL!" }
    }
}
