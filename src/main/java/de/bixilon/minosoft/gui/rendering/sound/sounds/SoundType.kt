/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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

import de.bixilon.kutil.json.JsonUtil.asJsonList
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import java.util.*
import kotlin.math.abs

data class SoundType(
    val soundEvent: ResourceLocation,
    val sounds: Set<Sound>,
    val subtitle: ResourceLocation?,
) {
    val totalWeight: Int

    init {
        var totalWeight = 0
        for (sound in sounds) {
            totalWeight += sound.weight
        }

        this.totalWeight = totalWeight
    }

    fun getSound(random: Random): Sound? {
        if (sounds.isEmpty()) {
            return null
        }
        var weightLeft = abs(random.nextLong() % totalWeight)

        for (sound in sounds) {
            weightLeft -= sound.weight
            if (weightLeft < 0) {
                return sound
            }
        }

        throw IllegalStateException("Could not find sound: This should never happen!")
    }

    companion object {

        operator fun invoke(soundEvent: ResourceLocation, data: Map<String, Any>): SoundType {
            // ToDo: "replace" attribute
            val subtitle = data["subtitle"]?.toResourceLocation()
            val sounds: MutableSet<Sound> = mutableSetOf()

            for (soundData in data["sounds"].asJsonList()) {
                sounds += Sound(soundEvent, soundData ?: continue)
            }
            return SoundType(
                soundEvent = soundEvent,
                sounds = sounds,
                subtitle = subtitle,
            )
        }
    }
}
