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

package de.bixilon.minosoft.protocol.network.connection.play.clientsettings

import de.bixilon.minosoft.config.profile.delegate.watcher.SimpleProfileDelegateWatcher.Companion.profileWatch
import de.bixilon.minosoft.data.language.LanguageManager
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.c2s.play.SettingsC2SP
import de.bixilon.minosoft.protocol.protocol.ProtocolStates

class ClientSettingsManager(
    private val connection: PlayConnection,
) {
    private val profile = connection.profiles.connection
    private var language = profile.language ?: connection.profiles.eros.general.language

    init {
        val blocks = connection.profiles.block
        blocks::viewDistance.profileWatch(this, profile = blocks) {
            connection.world.view.viewDistance = it
            sendClientSettings()
        }

        blocks::simulationDistance.profileWatch(this, profile = blocks) { connection.world.view.simulationDistance = it }


        val hud = connection.profiles.hud
        val chat = hud.chat
        chat::chatMode.profileWatch(this, profile = hud) { sendClientSettings() }
        chat::textFiltering.profileWatch(this, profile = hud) { sendClientSettings() }
        chat::chatColors.profileWatch(this, profile = hud) { sendClientSettings() }

        profile::mainArm.profileWatch(this, profile = profile) { sendClientSettings() }
        profile::playerListing.profileWatch(this, profile = profile) { sendClientSettings() }

        val skin = profile.skin
        skin::cape.profileWatch(this, profile = profile) { sendClientSettings() }
        skin::jacket.profileWatch(this, profile = profile) { sendClientSettings() }
        skin::leftSleeve.profileWatch(this, profile = profile) { sendClientSettings() }
        skin::rightSleeve.profileWatch(this, profile = profile) { sendClientSettings() }
        skin::leftPants.profileWatch(this, profile = profile) { sendClientSettings() }
        skin::rightPants.profileWatch(this, profile = profile) { sendClientSettings() }
        skin::hat.profileWatch(this, profile = profile) { sendClientSettings() }

        profile::language.profileWatch(this, profile = profile) { sendLanguage() }
        connection.profiles.eros.general::language.profileWatch(this, profile = connection.profiles.eros) { sendLanguage() }
    }

    @Synchronized
    private fun sendLanguage() {
        val language = profile.language ?: connection.profiles.eros.general.language
        if (this.language == language) {
            return
        }
        this.language = language
        connection.language = LanguageManager.load(language, connection.version, connection.assetsManager)
        sendClientSettings()
    }

    fun sendClientSettings() {
        if (connection.network.state != ProtocolStates.PLAY) {
            return
        }
        connection.sendPacket(SettingsC2SP(
            locale = language,
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
