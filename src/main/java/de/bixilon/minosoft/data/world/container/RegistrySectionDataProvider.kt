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

package de.bixilon.minosoft.data.world.container

import de.bixilon.minosoft.data.registries.registries.registry.AbstractRegistry
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

class RegistrySectionDataProvider<T>(
    val registry: AbstractRegistry<T>,
    data: Array<Any?>? = null,
    checkSize: Boolean = false,
) : SectionDataProvider<T>(data, checkSize = checkSize) {

    @Suppress("UNCHECKED_CAST")
    fun setIdData(ids: Array<Int>) {
        val data: Array<Any?> = arrayOfNulls(ProtocolDefinition.BLOCKS_PER_SECTION)

        for ((index, id) in ids.withIndex()) {
            data[index] = registry[id]
        }

        setData(data as Array<T>)
    }


    override fun copy(): RegistrySectionDataProvider<T> {
        acquire()
        val clone = RegistrySectionDataProvider(registry, data?.clone())
        release()

        return clone
    }
}
