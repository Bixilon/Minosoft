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

package de.bixilon.minosoft.protocol.network.connection.play.settings

import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.language.LanguageUtil
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
        blocks::viewDistance.observe(this) {
            connection.world.view.viewDistance = it
            sendClientSettings()
        }

        blocks::simulationDistance.observe(this) { connection.world.view.simulationDistance = it }


        val hud = connection.profiles.gui
        val chat = hud.chat
        chat::chatMode.observe(this) { sendClientSettings() }
        chat::textFiltering.observe(this) { sendClientSettings() }
        chat::chatColors.observe(this) { sendClientSettings() }

        profile::mainArm.observe(this) { sendClientSettings() }
        profile::playerListing.observe(this) { sendClientSettings() }

        profile::language.observe(this) { sendLanguage() }
        connection.profiles.eros.general::language.observe(this) { sendLanguage() }
    }

    @Synchronized
    private fun sendLanguage() {
        val language = profile.language ?: connection.profiles.eros.general.language
        if (this.language == language) {
            return
        }
        this.language = language
        connection.language = LanguageUtil.load(language, connection.version, connection.assetsManager)
        sendClientSettings()
    }

    fun sendClientSettings() {
        if (connection.network.state != ProtocolStates.PLAY) {
            return
        }
        connection.sendPacket(
            SettingsC2SP(
                locale = language,
                chatColors = connection.profiles.gui.chat.chatColors,
                viewDistance = connection.profiles.block.viewDistance,
                chatMode = connection.profiles.gui.chat.chatMode,
                skinParts = profile.skin.parts.toTypedArray(),
                mainArm = profile.mainArm,
                disableTextFiltering = !connection.profiles.gui.chat.textFiltering,
                allowListing = profile.playerListing,
            )
        )
    }
}
