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

package de.bixilon.minosoft.data.registries.sound

import de.bixilon.kutil.primitive.FloatUtil.toFloat
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.registry.RegistryFakeEnumerable
import de.bixilon.minosoft.data.registries.registries.registry.codec.IdCodec

class SoundGroup(
    override val name: String,
    val destroy: ResourceLocation?,
    val step: ResourceLocation?,
    val place: ResourceLocation?,
    val hit: ResourceLocation?,
    val fall: ResourceLocation?,
    val volume: Float = 1.0f,
    val pitch: Float = 1.0f,
) : RegistryFakeEnumerable {

    companion object : IdCodec<SoundGroup> {

        override fun deserialize(registries: Registries, data: Map<String, Any>): SoundGroup {
            return SoundGroup(
                name = data["name"]?.toString()!!,
                destroy = data["break_sound_type"]?.toInt()?.let { registries.soundEventRegistry[it] },
                step = data["step_sound_type"]?.toInt()?.let { registries.soundEventRegistry[it] },
                place = data["place_sound_type"]?.toInt()?.let { registries.soundEventRegistry[it] },
                hit = data["hit_sound_type"]?.toInt()?.let { registries.soundEventRegistry[it] },
                fall = data["fall_sound_type"]?.toInt()?.let { registries.soundEventRegistry[it] },
                volume = data["sound_type_volume"]?.toFloat() ?: 1.0f,
                pitch = data["sound_type_pitch"]?.toFloat() ?: 1.0f,
            )
        }
    }
}
