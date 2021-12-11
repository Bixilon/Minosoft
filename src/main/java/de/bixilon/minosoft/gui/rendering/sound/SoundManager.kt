package de.bixilon.minosoft.gui.rendering.sound

import de.bixilon.minosoft.assets.util.FileUtil.readJsonObject
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.sound.sounds.Sound
import de.bixilon.minosoft.gui.rendering.sound.sounds.SoundType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.asCompound
import java.util.*

class SoundManager(
    private val connection: PlayConnection,
) {
    private val random = Random()
    private val sounds: MutableMap<ResourceLocation, SoundType> = mutableMapOf()


    fun load() {
        val soundsIndex = connection.assetsManager[SOUNDS_INDEX_FILE].readJsonObject()

        for ((name, data) in soundsIndex) {
            val resourceLocation = name.toResourceLocation()
            sounds[resourceLocation] = SoundType(resourceLocation, data.asCompound())
        }
    }

    @Synchronized
    fun unload() {
        for (soundType in sounds.values) {
            for (sound in soundType.sounds) {
                sound.unload()
            }
        }
    }

    @Synchronized
    fun preload() {
        for (soundType in sounds.values) {
            for (sound in soundType.sounds) {
                if (!sound.preload) {
                    continue
                }
                sound.load(connection.assetsManager)
            }
        }
    }

    operator fun get(sound: ResourceLocation): Sound? {
        return sounds[sound]?.getSound(random)
    }

    companion object {
        private val SOUNDS_INDEX_FILE = "minecraft:sounds.json".toResourceLocation()
    }
}
