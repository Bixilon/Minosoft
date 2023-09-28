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

package de.bixilon.minosoft.data.registries.blocks.properties

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.enums.AliasableEnum
import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum

enum class MultipartDirections(
    vararg names: String,
) : AliasableEnum {
    NONE("false"),
    LOW,
    UP,
    SIDE("true"),
    TALL,
    ;

    override val names: Array<String> = names.unsafeCast()

    companion object : ValuesEnum<MultipartDirections> {
        override val VALUES: Array<MultipartDirections> = values()
        override val NAME_MAP = EnumUtil.getEnumValues(VALUES)

        override fun get(any: Any): MultipartDirections? {
            if (any is Boolean) {
                return if (any) SIDE else NONE
            }
            return super.get(any)
        }
    }
}
