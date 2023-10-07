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

package de.bixilon.minosoft.data.registries.blocks.settings

import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.sound.SoundGroup
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class BlockSettings(
    val version: Version,
    val soundGroup: SoundGroup? = null,
    val translationKey: ResourceLocation? = null,
) {

    companion object {

        fun of(version: Version, registries: Registries, data: JsonObject): BlockSettings {
            val soundGroup = data["sound_group"]?.toInt()?.let { registries.soundGroup[it] }
            val translationKey = data["translation_key"]?.toResourceLocation()


            return BlockSettings(version, soundGroup = soundGroup, translationKey = translationKey)
        }
    }
}
