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

import de.bixilon.minosoft.data.registries.registries.registry.AbstractRegistry
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition.FLATTENING_VERSION

object BlockStatePaletteFactory : PaletteFactory {
    override val edgeBits get() = 4

    override fun <T : Any?> createPalette(registry: AbstractRegistry<T?>, bits: Int, version: Int): Palette<T> {
        when (bits) {
            0 -> return SingularPalette(registry)
            1, 2, 3, 4 -> return ArrayPalette(registry, 4)
            5, 6, 7, 8 -> return ArrayPalette(registry, bits)
        }
        if (version < FLATTENING_VERSION) return RegistryPalette(registry, 13) // minecraft uses 13 bits to encode the blocks (8 bits id + 4 bits meta + 1 magic bit, thanks)

        return RegistryPalette(registry) // flattened
    }
}
