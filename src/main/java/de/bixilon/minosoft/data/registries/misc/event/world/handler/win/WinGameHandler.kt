/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.misc.event.world.handler.win

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.misc.event.game.GameEventHandler
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.c2s.play.ClientActionC2SP
import de.bixilon.minosoft.util.KUtil.toResourceLocation

object WinGameHandler : GameEventHandler {
    override val RESOURCE_LOCATION: ResourceLocation = "minecraft:win_game".toResourceLocation()

    override fun handle(data: Float, connection: PlayConnection) {
        val credits = data.toInt() == 0x01
        connection.fire(WinGameEvent(connection, credits))
        if (!credits) {
            connection.sendPacket(ClientActionC2SP(ClientActionC2SP.ClientActions.PERFORM_RESPAWN))
        }
    }
}
