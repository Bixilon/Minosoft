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

package de.bixilon.minosoft.data.registries.particle.data.vibration

import de.bixilon.minosoft.data.registries.factory.DefaultFactory
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_20_2
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer

object VibrationSources : DefaultFactory<VibrationFactory<*>>(
    BlockSource,
    EntitySource,
) {

    fun read(buffer: PlayInByteBuffer): VibrationSource {
        val type = if (buffer.versionId <= V_1_20_2) buffer.readResourceLocation() else buffer.readRegistryItem(buffer.connection.registries.vibrationSource) // TODO: fix out version
        val factory = this[type] ?: throw IllegalArgumentException("Can not find vibration source: $type")

        return factory.read(buffer)
    }
}
