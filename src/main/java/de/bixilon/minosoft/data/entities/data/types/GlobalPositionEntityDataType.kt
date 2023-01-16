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

package de.bixilon.minosoft.data.entities.data.types

import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.minosoft.data.entities.GlobalPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.toVec3i
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer

object GlobalPositionEntityDataType : EntityDataType<GlobalPosition> {

    override fun read(buffer: PlayInByteBuffer): GlobalPosition? {
        if (buffer.versionId < ProtocolVersions.V_1_19_PRE2) { // ToDo: find out version
            return buffer.readNBT()?.toJsonObject()?.toGlobalPosition(buffer.connection)
        }
        val dimension = buffer.readLegacyRegistryItem(buffer.connection.registries.dimension)
        val position = buffer.readBlockPosition()
        return GlobalPosition(dimension, position)
    }


    fun JsonObject.toGlobalPosition(connection: PlayConnection): GlobalPosition {
        val dimension = connection.registries.dimension[this["dimension"]]
        val position = this["pos"].toVec3i()
        return GlobalPosition(dimension, position)
    }
}
