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
package de.bixilon.minosoft.protocol.packets.s2c.play.world

import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.kutil.enums.ValuesEnum.Companion.names
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.update.block.ChunkLocalBlockUpdate
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.blockPosition
import de.bixilon.minosoft.modding.event.events.ExplosionEvent
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_17
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_23W45A
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class ExplosionS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val position = if (buffer.versionId >= ProtocolVersions.V_22W42A) buffer.readVec3d() else Vec3d(buffer.readVec3f())
    val power = buffer.readFloat()
    val explodedBlocks: Array<BlockPosition> = buffer.readArray(if(buffer.versionId < V_1_17)buffer.readInt() else buffer.readVarInt()) { BlockPosition(buffer.readByte().toInt(), buffer.readByte().toInt(), buffer.readByte().toInt()) } // ToDo: Find out version
    val velocity = buffer.readVec3f()
    val destruct = if (buffer.versionId >= V_23W45A) buffer.readEnum(DestructionTypes) else DestructionTypes.DESTROY
    val particle = if (buffer.versionId >= V_23W45A) buffer.readParticleData() else null
    val emitter = if (buffer.versionId >= V_23W45A) buffer.readParticleData() else null
    val sound = if (buffer.versionId >= V_23W45A) buffer.readNamedSound() else null

    override fun check(session: PlaySession) {
        require(power <= 100.0f) {
            // maybe somebody tries to make bullshit?
            // Sorry, Maximilian Rosenmüller
            "Explosion to big $power > 100.0F"
        }
    }

    private fun World.clearBlock(offset: BlockPosition, position: BlockPosition) {
        this[offset + position] = null
    }

    private fun World.clearBlocks(offset: BlockPosition, positions: Array<BlockPosition>) {
        if (positions.isEmpty()) return
        if (positions.size == 1) return clearBlock(offset, positions.first())

        val updates: MutableMap<Chunk, MutableSet<ChunkLocalBlockUpdate.LocalUpdate>> = hashMapOf()

        var chunk: Chunk? = null

        for (entry in positions) {
            val total = offset + entry
            val chunkPosition = total.chunkPosition

            if (chunk == null) {
                chunk = this.chunks[chunkPosition] ?: continue // TODO: Don't query same chunk multiple times
            } else if (chunk.position != chunkPosition) {
                chunk = chunk.neighbours.traceChunk(chunkPosition - chunk.position) ?: continue
            }

            val inChunkPosition = total.inChunkPosition
            if (chunk[inChunkPosition] == null) continue

            val update = ChunkLocalBlockUpdate.LocalUpdate(inChunkPosition, null)

            updates.getOrPut(chunk) { hashSetOf() } += update
        }

        for ((chunk, updates) in updates) {
            chunk.apply(updates)
        }
    }

    override fun handle(session: PlaySession) {
        if (destruct == DestructionTypes.DESTROY) { // TODO: handle DECAY, TRIGGER
            session.world.clearBlocks(position.blockPosition, this.explodedBlocks)
        }
        session.player.physics.velocity += velocity

        session.events.fire(ExplosionEvent(session, this))
    }

    enum class DestructionTypes {
        KEEP,
        DESTROY,
        DECAY,
        TRIGGER,
        ;

        companion object : ValuesEnum<DestructionTypes> {
            override val VALUES = values()
            override val NAME_MAP = names()
        }
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Explosion (position=$position, power=$power, explodedBlocks=$explodedBlocks, velocity=$velocity)" }
    }
}
