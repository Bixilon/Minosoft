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

package de.bixilon.minosoft.protocol.status

import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.util.KUtil.toInt
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.compoundCast
import java.util.*

class ServerStatus(
    data: Map<String, Any>,
) {
    var protocolId: Int? = null
        private set
    var serverBrand: String? = null
        private set

    var usedSlots: Int? = null
        private set
    var slots: Int? = null
        private set

    var motd: ChatComponent? = null
        private set

    var favicon: ByteArray? = null
        private set

    init {
        data["version"]?.compoundCast()?.let {
            protocolId = it["protocol"]?.toInt()
            serverBrand = it["name"]?.toString()
        }
        data["players"]?.compoundCast()?.let {
            usedSlots = it["online"]?.toInt()
            slots = it["max"]?.toInt()

            // ToDo: Players (hover text)
        }
        data["description"]?.let { motd = ChatComponent.of(it) }

        data["favicon"]?.toString()?.let {
            val favicon = Base64.getDecoder().decode(it.replace("data:image/png;base64,", "").replace("\n", ""))
            this.favicon = favicon
        }
    }

    override fun toString(): String {
        return "ServerStatus(protocolId=$protocolId, usedSlots=$usedSlots, slots=$slots)"
    }
}
