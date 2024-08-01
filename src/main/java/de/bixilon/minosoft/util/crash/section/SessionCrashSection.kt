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

package de.bixilon.minosoft.util.crash.section

import de.bixilon.minosoft.protocol.network.session.play.PlaySession

class SessionCrashSection(
    sessions: Array<PlaySession> = PlaySession.collectSessions(),
) : ArrayCrashSection<PlaySession>("Sessions", sessions) {

    override fun format(entry: PlaySession, builder: StringBuilder, intent: String) {
        builder.appendProperty(intent, "Id", entry.id)
        builder.appendProperty(intent, "Version", entry.version)
        builder.appendProperty(intent, "Account", entry.account.username)
        builder.appendProperty(intent, "Address", entry.address)
        builder.appendProperty(intent, "Brand", entry.serverInfo.brand)
        builder.appendProperty(intent, "Events", entry.events.size)
        builder.appendProperty(intent, "State", entry.state)
        builder.appendProperty(intent, "DefaultPacketMapping state", entry.network.state)
        builder.appendProperty(intent, "Compression threshold", entry.network.compressionThreshold)
        builder.appendProperty(intent, "Encrypted", entry.network.encrypted)
        builder.appendProperty(intent, "Was connected", entry.established)
        builder.appendProperty(intent, "Rendering", entry.rendering != null)
        builder.appendProperty(intent, "Error", entry.error)
    }
}
