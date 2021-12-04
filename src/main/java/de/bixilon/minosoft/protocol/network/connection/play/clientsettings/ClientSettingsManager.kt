/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.network.connection.play.clientsettings

import de.bixilon.minosoft.config.profile.change.listener.SimpleChangeListener.Companion.listen
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.c2s.play.ClientSettingsC2SP

class ClientSettingsManager(
    private val connection: PlayConnection,
) {
    private val profile = connection.profiles.connection
    private var language = profile.language ?: connection.profiles.eros.general.language

    init {
        val blocks = connection.profiles.block
        blocks::viewDistance.listen(this, profile = blocks) {
            connection.world.view.viewDistance = it
            sendClientSettings()
        }

        blocks::simulationDistance.listen(this, profile = blocks) { connection.world.view.simulationDistance = it }


        val hud = connection.profiles.hud
        val chat = hud.chat
        chat::chatMode.listen(this, profile = hud) { sendClientSettings() }
        chat::textFiltering.listen(this, profile = hud) { sendClientSettings() }
        chat::chatColors.listen(this, profile = hud) { sendClientSettings() }

        profile::mainArm.listen(this, profile = profile) { sendClientSettings() }
        profile::playerListing.listen(this, profile = profile) { sendClientSettings() }

        val skin = profile.skin
        skin::cape.listen(this, profile = profile) { sendClientSettings() }
        skin::jacket.listen(this, profile = profile) { sendClientSettings() }
        skin::leftSleeve.listen(this, profile = profile) { sendClientSettings() }
        skin::rightSleeve.listen(this, profile = profile) { sendClientSettings() }
        skin::leftPants.listen(this, profile = profile) { sendClientSettings() }
        skin::rightPants.listen(this, profile = profile) { sendClientSettings() }
        skin::hat.listen(this, profile = profile) { sendClientSettings() }

        profile::language.listen(this, profile = profile) { sendLanguage() }
        connection.profiles.eros.general::language.listen(this, profile = connection.profiles.eros) { sendLanguage() }

        // ToDo: Load new language files
    }

    @Synchronized
    private fun sendLanguage() {
        val language = profile.language ?: connection.profiles.eros.general.language
        if (this.language == language) {
            return
        }
        this.language = language
        sendClientSettings()
    }

    fun sendClientSettings() {
        connection.sendPacket(ClientSettingsC2SP(
            locale = language.toString(),
            chatColors = connection.profiles.hud.chat.chatColors,
            viewDistance = connection.profiles.block.viewDistance,
            chatMode = connection.profiles.hud.chat.chatMode,
            skinParts = profile.skin.skinParts,
            mainArm = profile.mainArm,
            disableTextFiltering = !connection.profiles.hud.chat.textFiltering,
            allowListing = profile.playerListing,
        ))
    }
}
