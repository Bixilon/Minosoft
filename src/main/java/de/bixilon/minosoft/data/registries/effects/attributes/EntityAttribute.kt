/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.effects.attributes

import de.bixilon.kutil.collections.CollectionUtil.synchronizedMapOf
import java.util.*

data class EntityAttribute(
    var baseValue: Double = 1.0,
    val modifiers: MutableMap<UUID, EntityAttributeModifier> = synchronizedMapOf(),
) {
    fun merge(other: EntityAttribute) {
        baseValue = other.baseValue
        for ((key, modifier) in other.modifiers) {
            modifiers[key] = modifier
        }
    }
}
