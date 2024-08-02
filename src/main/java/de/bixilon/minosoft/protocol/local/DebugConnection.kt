/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.local

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.entities.entities.player.local.Abilities
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.biome.source.DummyBiomeSource
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.modding.event.events.DimensionChangeEvent
import de.bixilon.minosoft.protocol.ServerConnection
import de.bixilon.minosoft.protocol.network.session.Session
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.network.session.play.PlaySessionStates
import de.bixilon.minosoft.protocol.packets.c2s.C2SPacket
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import kotlin.math.sqrt

class DebugConnection : ServerConnection {
    override val identifier = "dbg"
    override var active by observed(false)
    private var detached = false
    private lateinit var session: PlaySession


    private fun World.createChunks(position: ChunkPosition, radius: Int) {
        val biome = session.registries.biome[minecraft("plains")]
        for (x in position.x - radius until position.x + radius) {
            for (z in position.y - radius until position.y + radius) {
                val chunk = chunks.create(ChunkPosition(x, z))
                chunk.biomeSource = DummyBiomeSource(biome)
                chunk[0, 0, 0] = null // create lowest section
            }
        }
    }

    private fun fillDebug() {
        val total = session.registries.blockState.size
        val width = sqrt(total.toFloat()).toInt() + 1
        var count = 0

        for (block in session.registries.block) {
            for (state in block.states) {
                val x = (count / width - (width / 2)) * 2
                val z = (count % width - (width / 2)) * 2
                val chunk = session.world.chunks.create(ChunkPosition(x shr 4, z shr 4))
                chunk[x and 0x0F, 8, z and 0x0F] = state
                count++
            }
        }
    }

    override fun connect(session: Session) {
        if (session !is PlaySession) throw IllegalStateException("Not a play session?")
        Log.log(LogMessageType.NETWORK, LogLevels.INFO) { "Establishing debug connection" }
        active = true
        this.session = session


        session.util.resetWorld()
        session.util.prepareSpawn()


        session.world.dimension = DimensionProperties()
        session.player.additional.gamemode = Gamemodes.CREATIVE


        session.world.entities.clear(session, local = true)
        session.world.entities.add(1, null, session.player)

        session.player.abilities = Abilities(false, true, true)
        session.world.createChunks(ChunkPosition(0, 0), 5)

        session.events.fire(DimensionChangeEvent(session))
        session.state = PlaySessionStates.SPAWNING
        session.player.physics.forceTeleport(Vec3d(0, 20, 0))
        session.state = PlaySessionStates.PLAYING



        fillDebug()
    }

    override fun disconnect() {
        active = false
    }

    override fun detach() {
        detached = true
    }

    override fun send(packet: C2SPacket) {
        if (detached) return
        packet.log(false)
    }
}
