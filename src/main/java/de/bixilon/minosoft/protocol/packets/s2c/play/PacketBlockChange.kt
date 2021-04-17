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
package de.bixilon.minosoft.protocol.packets.s2c.play

import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.data.mappings.tweaker.VersionTweaker
import de.bixilon.minosoft.gui.rendering.util.VecUtil.chunkPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inChunkSectionPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight

import de.bixilon.minosoft.modding.event.events.BlockChangeEvent
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log
import glm_.vec3.Vec3i

class PacketBlockChange(buffer: PlayInByteBuffer) : PlayS2CPacket() {
    val blockPosition: Vec3i
    val block: BlockState?

    init {
        if (buffer.versionId < ProtocolVersions.V_14W03B) {
            blockPosition = buffer.readByteBlockPosition()
            block = buffer.connection.mapping.getBlockState(buffer.readVarInt() shl 4 or buffer.readByte().toInt()) // ToDo: When was the meta data "compacted"? (between 1.7.10 - 1.8)
        } else {
            blockPosition = buffer.readBlockPosition()
            block = buffer.connection.mapping.getBlockState(buffer.readVarInt())
        }
    }

    override fun handle(connection: PlayConnection) {
        val chunk = connection.world.getChunk(blockPosition.chunkPosition) ?: return // thanks mojang
        if (!chunk.isFullyLoaded) {
            return
        }
        connection.fireEvent(BlockChangeEvent(connection, this))
        val sectionHeight = blockPosition.sectionHeight
        val inChunkSectionPosition = blockPosition.inChunkSectionPosition
        val section = chunk.getSectionOrCreate(sectionHeight)

        // tweak
        if (!connection.version.isFlattened()) {
            val block = VersionTweaker.transformBlock(block!!, chunk.sections!!, inChunkSectionPosition, sectionHeight)
            section.setBlockState(inChunkSectionPosition, block)
        } else {
            section.setBlockState(inChunkSectionPosition, block)
        }

        connection.renderer?.renderWindow?.worldRenderer?.prepareChunkSection(blockPosition.chunkPosition, sectionHeight)
    }

    override fun log() {
        Log.protocol("[IN] Block change received (position=${blockPosition}, block=$block)")
    }
}
