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

import de.bixilon.kutil.json.JsonUtil.asJsonObject
import de.bixilon.minosoft.assets.util.FileUtil.readJsonObject
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.sound.sounds.Sound
import de.bixilon.minosoft.gui.rendering.sound.sounds.SoundType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toResourceLocation
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
            sounds[resourceLocation] = SoundType(resourceLocation, data.asJsonObject())
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
