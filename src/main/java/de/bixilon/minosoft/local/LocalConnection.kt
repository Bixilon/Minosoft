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

package de.bixilon.minosoft.local

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.chat.message.SimpleChatMessage
import de.bixilon.minosoft.data.chat.type.DefaultMessageTypes
import de.bixilon.minosoft.data.entities.entities.player.additional.AdditionalDataUpdate
import de.bixilon.minosoft.data.entities.entities.player.local.Abilities
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.local.generator.ChunkGenerator
import de.bixilon.minosoft.local.storage.WorldStorage
import de.bixilon.minosoft.modding.event.events.DimensionChangeEvent
import de.bixilon.minosoft.modding.event.events.TabListEntryChangeEvent
import de.bixilon.minosoft.modding.event.events.chat.ChatMessageEvent
import de.bixilon.minosoft.protocol.ServerConnection
import de.bixilon.minosoft.protocol.network.session.Session
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.network.session.play.PlaySessionStates
import de.bixilon.minosoft.protocol.packets.c2s.C2SPacket
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.move.PositionC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.move.PositionRotationC2SP
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class LocalConnection(
    val generator: (PlaySession) -> ChunkGenerator,
    val storage: (PlaySession) -> WorldStorage,
) : ServerConnection {
    override val identifier = "<local>"
    override var active by observed(false)
    private var detached = false
    private lateinit var session: PlaySession
    lateinit var chunks: LocalChunkManager


    fun sendMessage(message: Any, type: ResourceLocation = DefaultMessageTypes.CHAT) {
        val type = session.registries.messageType[type]!!
        session.events.fire(ChatMessageEvent(session, SimpleChatMessage(ChatComponent.of(message), type)))
    }


    override fun connect(session: Session) {
        if (session !is PlaySession) throw IllegalStateException("Not a play session?")
        Log.log(LogMessageType.NETWORK, LogLevels.INFO) { "Establishing debug connection" }
        active = true
        this.session = session
        this.chunks = LocalChunkManager(session, storage.invoke(session), generator.invoke(session))


        session.util.resetWorld()
        session.util.prepareSpawn()


        session.world.dimension = DimensionProperties()
        session.player.additional.gamemode = Gamemodes.CREATIVE


        session.world.entities.clear(session, local = true)
        session.world.entities.add(1, null, session.player)

        session.player.abilities = Abilities(flying = true, allowFly = true)

        session.events.fire(DimensionChangeEvent(session))
        session.state = PlaySessionStates.SPAWNING

        val additional = session.player.additional

        session.tabList.uuid[session.player.uuid] = additional
        session.tabList.name[session.player.additional.name] = additional

        session.events.fire(TabListEntryChangeEvent(session, mapOf(session.player.uuid to AdditionalDataUpdate())))

        session.player.physics.forceTeleport(Vec3d(0, 20, 0))
        session.state = PlaySessionStates.PLAYING

        sendMessage("§e${session.player.name} §ejoined the game!")


        chunks.storage.loadWorld(session.world)
        chunks.update()
    }

    override fun disconnect() {
        active = false
    }

    override fun detach() {
        detached = true
    }

    override fun send(packet: C2SPacket) {
        if (detached) return
        packet.log(true)
        when (packet) {
            is PositionRotationC2SP -> chunks.update()
            is PositionC2SP -> chunks.update()
        }
    }
}
