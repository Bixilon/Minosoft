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

package de.bixilon.minosoft.gui.rendering.models.raw.display

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.enums.AliasableEnum
import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum

enum class DisplayPositions(vararg names: String = arrayOf()) : AliasableEnum {
    THIRD_PERSON_RIGHT_HAND("thirdperson_righthand"),
    THIRD_PERSON_LEFT_HAND("thirdperson_lefthand"),
    FIRST_PERSON_RIGHT_HAND("firstperson_righthand"),
    FIRST_PERSON_LEFT_HAND("firstperson_lefthand"),
    GUI,
    HEAD,
    GROUND,
    FIXED,
    ;

    override val names: Array<String> = names.unsafeCast()

    companion object : ValuesEnum<DisplayPositions> {
        override val VALUES: Array<DisplayPositions> = values()
        override val NAME_MAP: Map<String, DisplayPositions> = EnumUtil.getEnumValues(VALUES)
    }
}
