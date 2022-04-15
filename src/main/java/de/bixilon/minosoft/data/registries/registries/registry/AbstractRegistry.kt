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

package de.bixilon.minosoft.data.registries.registries.registry

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.util.collections.Clearable
import de.bixilon.minosoft.util.json.ResourceLocationJsonMap.toResourceLocationMap

interface AbstractRegistry<T> : Iterable<T>, Clearable, Parentable<AbstractRegistry<T>> {
    val size: Int

    operator fun get(any: Any?): T?

    operator fun get(id: Int): T?

    fun getId(value: T): Int

    fun rawInitialize(data: Map<String, Any>?, registries: Registries?, deserializer: ResourceLocationDeserializer<T>?, flattened: Boolean = true, metaType: Registry.MetaTypes = Registry.MetaTypes.NONE, alternative: AbstractRegistry<T>? = null): AbstractRegistry<T> {
        return initialize(data?.toResourceLocationMap(), registries, deserializer, flattened, metaType, alternative)
    }

    fun initialize(data: Map<ResourceLocation, Any>?, registries: Registries?, deserializer: ResourceLocationDeserializer<T>?, flattened: Boolean = true, metaType: Registry.MetaTypes = Registry.MetaTypes.NONE, alternative: AbstractRegistry<T>? = null): AbstractRegistry<T>

    fun noParentIterator(): Iterator<T>

    override fun iterator(): Iterator<T> {
        return RegistryIterator(this)
    }
}
