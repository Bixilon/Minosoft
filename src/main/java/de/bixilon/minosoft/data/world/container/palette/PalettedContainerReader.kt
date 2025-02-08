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

package de.bixilon.minosoft.data.world.container.palette

import de.bixilon.minosoft.data.registries.registries.registry.AbstractRegistry
import de.bixilon.minosoft.data.world.container.palette.data.PaletteData
import de.bixilon.minosoft.data.world.container.palette.palettes.PaletteFactory
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer

object PalettedContainerReader {

    fun <T> read(buffer: PlayInByteBuffer, registry: AbstractRegistry<T?>, factory: PaletteFactory): PalettedContainer<T> {
        val bits = buffer.readUnsignedByte()

        val palette = factory.createPalette(registry, bits, buffer.versionId)
        palette.read(buffer)

        val data = PaletteData.create(buffer.versionId, palette.bits, factory.containerSize)
        data.read(buffer)

        return PalettedContainer(factory.edgeBits, palette, data)
    }

    inline fun <reified T> unpack(buffer: PlayInByteBuffer, registry: AbstractRegistry<T?>, factory: PaletteFactory): Array<T>? {
        val bits = buffer.readUnsignedByte()

        val palette = factory.createPalette(registry, bits, buffer.versionId)
        palette.read(buffer)

        val data = PaletteData.create(buffer.versionId, palette.bits, factory.containerSize)
        try {
            data.read(buffer)
            val container = PalettedContainer(factory.edgeBits, palette, data)

            if (container.isEmpty) return null
            val unpacked = container.unpack<T>()

            if (unpacked.isEmpty()) return null

            return unpacked
        } finally {
            data.free()
        }
    }
}
