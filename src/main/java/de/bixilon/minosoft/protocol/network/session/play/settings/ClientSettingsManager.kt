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

package de.bixilon.minosoft.protocol.network.session.play.settings

import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.observer.set.SetObserver.Companion.observeSet
import de.bixilon.minosoft.data.language.LanguageUtil
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.packets.c2s.common.SettingsC2SP
import de.bixilon.minosoft.protocol.protocol.ProtocolStates
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_20_2_PRE1

class ClientSettingsManager(
    private val session: PlaySession,
) {
    private val profile = session.profiles.session
    private var language = profile.language ?: session.profiles.eros.general.language

    init {
        val blocks = session.profiles.block
        blocks::viewDistance.observe(this) {
            session.world.view.viewDistance = it
            sendClientSettings()
        }

        blocks::simulationDistance.observe(this) { session.world.view.simulationDistance = it }


        val hud = session.profiles.gui
        val chat = hud.chat
        chat::chatMode.observe(this) { sendClientSettings() }
        chat::textFiltering.observe(this) { sendClientSettings() }
        chat::chatColors.observe(this) { sendClientSettings() }

        profile::mainArm.observe(this) { sendClientSettings() }
        profile::playerListing.observe(this) { sendClientSettings() }

        profile::language.observe(this) { sendLanguage() }
        session.profiles.eros.general::language.observe(this) { sendLanguage() }
    }

    fun initSkins() {
        session.profiles.session.skin::parts.observeSet(this, true) { session.player.skinParts += it.adds; session.player.skinParts -= it.removes; sendClientSettings() }
    }

    @Synchronized
    private fun sendLanguage() {
        val language = profile.language ?: session.profiles.eros.general.language
        if (this.language == language) {
            return
        }
        this.language = language
        session.language = LanguageUtil.load(language, session.version, session.assetsManager)
        sendClientSettings()
    }

    private fun canSendSettings(): Boolean {
        if (session.network.state == ProtocolStates.PLAY) return true
        if (session.version > V_1_20_2_PRE1 && session.network.state == ProtocolStates.CONFIGURATION) return true
        return false
    }

    fun sendClientSettings() {
        if (!canSendSettings()) return

        session.network.send(SettingsC2SP(
            locale = language,
            chatColors = session.profiles.gui.chat.chatColors,
            viewDistance = session.profiles.block.viewDistance,
            chatMode = session.profiles.gui.chat.chatMode,
            skinParts = profile.skin.parts.toTypedArray(),
            mainArm = profile.mainArm,
            disableTextFiltering = !session.profiles.gui.chat.textFiltering,
            allowListing = profile.playerListing,
        ))
    }
}
