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

package de.bixilon.minosoft.data.world.container.palette.palettes

import de.bixilon.kutil.math.simple.IntMath.binaryBase
import de.bixilon.minosoft.data.registries.registries.registry.AbstractRegistry
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_12_2
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer

class RegistryPalette<T>(
    private val registry: AbstractRegistry<T?>,
    override val bits: Int = registry.size.binaryBase,
) : Palette<T> {

    override val isEmpty: Boolean
        get() = false

    override fun read(buffer: PlayInByteBuffer) {
        if (buffer.versionId <= V_1_12_2) { // TODO: find out exact version
            buffer.readVarInt() // no data
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun getOrNull(id: Int): T? {
        return registry.getOrNull(id)
    }
}
