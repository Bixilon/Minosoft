/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.world.palette

import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import kotlin.math.ceil
import kotlin.math.ln

class DirectPalette(buffer: PlayInByteBuffer) : Palette {
    private var connection: PlayConnection = buffer.connection

    init {
        if (buffer.versionId < ProtocolVersions.V_17W47A) {
            buffer.readVarInt()
        }
    }

    override fun blockById(id: Int): BlockState? {
        return connection.registries.getBlockState(id)
    }

    override val bitsPerBlock: Int
        get() {
            if (this.connection.version.versionId < ProtocolVersions.V_18W10D) {
                return 13
            }
            return ceil(ln(connection.registries.blockStateCount.toDouble()) / ln(2.0)).toInt()
        }
}
