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

package de.bixilon.minosoft.assets.minecraft.index

import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.assets.util.FileAssetsTypes
import de.bixilon.minosoft.data.registries.identified.Namespaces
import de.bixilon.minosoft.data.registries.identified.ResourceLocation

enum class IndexAssetsType(val type: String) {
    LANGUAGE(FileAssetsTypes.GAME),
    SOUNDS(FileAssetsTypes.SOUNDS),
    TEXTURES(FileAssetsTypes.GAME),
    OTHER(FileAssetsTypes.GAME),
    ;

    companion object : ValuesEnum<IndexAssetsType> {
        override val VALUES: Array<IndexAssetsType> = values()
        override val NAME_MAP: Map<String, IndexAssetsType> = EnumUtil.getEnumValues(VALUES)


        fun determinate(identifier: ResourceLocation): IndexAssetsType? {
            if (identifier.namespace != Namespaces.MINECRAFT) return null
            val path = identifier.path
            return when {
                path == "sounds.json" -> SOUNDS
                path.startsWith("sounds/") -> SOUNDS
                path.startsWith("lang/") -> LANGUAGE
                path.startsWith("textures/") -> TEXTURES
                path.startsWith("font/") -> OTHER
                else -> null
            }
        }
    }
}
