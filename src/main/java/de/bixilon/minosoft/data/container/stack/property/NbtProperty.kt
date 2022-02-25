/*
 * Minosoft
 * Copyright (C) 2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.container.stack.property

import de.bixilon.kutil.json.MutableJsonObject
import de.bixilon.minosoft.data.container.stack.ItemStack

class NbtProperty(
    private val stack: ItemStack,
    val nbt: MutableJsonObject = mutableMapOf(),
) : Property {

    override fun hashCode(): Int {
        return nbt.hashCode()
    }

    override fun isDefault(): Boolean {
        return nbt.isEmpty()
    }

    override fun updateNbt(nbt: MutableJsonObject): Boolean {
        this.nbt.putAll(nbt)
        return isDefault()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is NbtProperty) {
            return false
        }
        if (other.hashCode() != hashCode()) {
            return false
        }
        return nbt == other.nbt
    }

    fun copy(
        stack: ItemStack,
        nbt: MutableJsonObject = this.nbt.toMutableMap(),
    ): NbtProperty {
        return NbtProperty(stack, nbt)
    }
}
