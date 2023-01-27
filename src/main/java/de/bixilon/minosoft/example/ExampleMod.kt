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

package de.bixilon.minosoft.example

import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.assets.util.InputStreamUtil.readAsString
import de.bixilon.minosoft.modding.event.events.chat.ChatMessageEvent
import de.bixilon.minosoft.modding.event.events.connection.play.PlayConnectionCreateEvent
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.modding.loader.mod.ModMain
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toResourceLocation

object ExampleMod : ModMain() {

    override fun init() {
        logger.info { "This mod can not do anything!" }
        val message = assets["example:message.txt".toResourceLocation()].readAsString()
        logger.info { "Stored message is: $message" }
    }

    override fun postInit() {
        logger.warn { "This mod can not do much yet!" }
        GlobalEventMaster.listen<PlayConnectionCreateEvent> { it.connection.startListening() }
    }

    private fun PlayConnection.startListening() {
        if (this.address.hostname != "localhost" && this.address.hostname != "127.0.0.1") {
            return
        }
        events.listen<ChatMessageEvent> {
            if ("hide me" !in it.message.text.message.lowercase()) {
                return@listen
            }
            it.cancelled = true
            logger.info { "A chat message was hidden!" }
        }

        this.world::weather.observe(this) { logger.info { "The weather just changed!" } }
    }
}
