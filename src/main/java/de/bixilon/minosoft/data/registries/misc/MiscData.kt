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

package de.bixilon.minosoft.data.registries.misc

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.registry.Parentable
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap

class MiscData(override var parent: MiscData? = null) : Parentable<MiscData> {
    private val fuelTime = Object2IntOpenHashMap<Item>()

    init {
        fuelTime.defaultReturnValue(-1)
    }

    fun getFuelTime(item: Item): Int {
        val time = fuelTime.getInt(item)
        if (time < 0) {
            return parent?.getFuelTime(item) ?: -1
        }
        return time
    }

    fun rawUpdate(data: JsonObject?, registries: Registries) {
        if (data == null) {
            return
        }
        data["fuel_time"]?.let { rawUpdateFuelTime(it.unsafeCast(), registries) }

    }

    private fun rawUpdateFuelTime(data: Map<Any, Any>, registries: Registries) {
        for ((itemId, time) in data) {
            val item = registries.itemRegistry[itemId] ?: continue
            fuelTime[item] = time.toInt()
        }
    }
}
