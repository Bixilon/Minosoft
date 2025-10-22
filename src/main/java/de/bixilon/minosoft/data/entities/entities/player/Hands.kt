/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.entities.entities.player

import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.data.container.equipment.EquipmentSlots

enum class Hands(val slot: EquipmentSlots) {
    MAIN(EquipmentSlots.MAIN_HAND),
    OFF(EquipmentSlots.OFF_HAND),
    ;

    fun getArm(main: Arms): Arms {
        if (this == MAIN) {
            return main
        }
        return if (main == Arms.LEFT) Arms.RIGHT else Arms.LEFT
    }

    companion object : ValuesEnum<Hands> {
        override val VALUES: Array<Hands> = values()
        override val NAME_MAP: Map<String, Hands> = EnumUtil.getEnumValues(VALUES)
    }
}
