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

package de.bixilon.minosoft.data.registries.blocks.properties.primitives

import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperty

class BooleanProperty(name: String) : BlockProperty<Boolean>(name) {
    val values get() = VALUES

    override fun parse(value: Any): Boolean {
        return value.toBoolean()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is BooleanProperty) return false
        return other.name == name
    }

    override fun iterator(): Iterator<Boolean> {
        return values.iterator()
    }

    companion object {
        val VALUES = booleanArrayOf(false, true)
    }
}
