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

package de.bixilon.minosoft.data.registries.registries.registry

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.collections.primitive.Clearable
import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.util.json.ResourceLocationJsonMap.toResourceLocationMap

interface AbstractRegistry<T> : Iterable<T>, Clearable, Parentable<AbstractRegistry<T>> {
    val size: Int

    operator fun get(any: Any?): T?

    operator fun get(id: Int): T {
        return getOrNull(id) ?: throw NullPointerException("Can not get entry with id $id!")
    }

    fun getOrNull(id: Int): T?

    fun getId(value: T): Int

    fun rawUpdate(data: Map<String, Any>?, registries: Registries?) {
        val map: Map<ResourceLocation, JsonObject> = data?.toResourceLocationMap()?.unsafeCast() ?: return
        update(map, registries)
    }

    fun addItem(resourceLocation: ResourceLocation, id: Int?, data: JsonObject, registries: Registries?): T?

    fun update(data: List<JsonObject>, registries: Registries?) = Unit
    fun update(data: Map<ResourceLocation, Any>, registries: Registries?) = Unit

    fun noParentIterator(): Iterator<T>

    override fun iterator(): Iterator<T> {
        return RegistryIterator(this)
    }
}
