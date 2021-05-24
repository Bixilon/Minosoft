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
package de.bixilon.minosoft.protocol.packets.c2s.play

import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.player.Hands
import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket
import de.bixilon.minosoft.protocol.protocol.PlayOutByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import glm_.vec3.Vec3
import glm_.vec3.Vec3i

class BlockPlaceC2SP(
    val position: Vec3i,
    val direction: Directions,
    val cursorPosition: Vec3,
    val item: ItemStack?,
    val hand: Hands,
    val insideBlock: Boolean,
) : PlayC2SPacket {

    override fun write(buffer: PlayOutByteBuffer) {
        if (buffer.versionId >= ProtocolVersions.V_19W03A) {
            buffer.writeVarInt(hand.ordinal)
        }
        if (buffer.versionId < ProtocolVersions.V_14W04A) {
            buffer.writeByteBlockPosition(position)
        } else {
            buffer.writePosition(position)
        }
        if (buffer.versionId < ProtocolVersions.V_15W31A) {
            buffer.writeByte(direction.ordinal)
            buffer.writeItemStack(item)
        } else {
            buffer.writeVarInt(direction.ordinal)
            if (buffer.versionId < ProtocolVersions.V_19W03A) {
                buffer.writeVarInt(hand.ordinal)
            }
        }
        if (buffer.versionId >= ProtocolVersions.V_19W03A) {
            buffer.writeBoolean(insideBlock)
        }
        if (buffer.versionId < ProtocolVersions.V_16W39C) {
            buffer.writeByte((cursorPosition.x * 15.0f).toInt())
            buffer.writeByte((cursorPosition.y * 15.0f).toInt())
            buffer.writeByte((cursorPosition.z * 15.0f).toInt())
        } else {
            buffer.writeFloat(cursorPosition.x)
            buffer.writeFloat(cursorPosition.y)
            buffer.writeFloat(cursorPosition.z)
        }
    }

    override fun log() {
        Log.log(LogMessageType.NETWORK_PACKETS_OUT, LogLevels.VERBOSE) { "Place block (position=$position, direction=$direction, item=$item, cursor=$cursorPosition, hand=$hand, insideBlock=$insideBlock)" }
    }
}
