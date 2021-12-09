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

package de.bixilon.minosoft.config.profile.profiles.hud.chat

import de.bixilon.minosoft.config.profile.profiles.hud.HUDProfileManager.delegate
import de.bixilon.minosoft.config.profile.profiles.hud.chat.internal.InternalC
import de.bixilon.minosoft.protocol.packets.c2s.play.ClientSettingsC2SP

class ChatC {
    val internal = InternalC()

    /**
     * Hides the chat
     */
    var hidden by delegate(false)

    /**
     * The width of the chat in scaled pixels
     */
    var width by delegate(320)

    /**
     * The height of the chat in scaled pixels
     */
    var height by delegate(180)

    /**
     * ToDo: Unknown purpose
     * Will be sent to the server
     */
    var textFiltering by delegate(false) // ToDo: Implement in the client

    /**
     * If false, the chat will appear in white
     * Will be sent to the server
     */
    var chatColors by delegate(true) // ToDo: Implement in the client

    /**
     * Chat mode
     * Will be sent to the server
     */
    var chatMode by delegate(ClientSettingsC2SP.ChatModes.EVERYTHING)

}
