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

package de.bixilon.minosoft.config.profile.profiles.other

import com.fasterxml.jackson.databind.node.ObjectNode
import de.bixilon.kutil.cast.CastUtil.nullCast

object OtherProfileMigration {

    /**
     * log level `AUDIO_LOADING` got renamed to just `AUDIO`
     */
    fun migrate1(data: ObjectNode) {
        data["log"]?.get("levels")?.nullCast<ObjectNode>()?.let { it.remove("AUDIO_LOADING")?.let { audio -> it.replace("AUDIO", audio) } }
    }

    /**
     * Some log levels got renamed
     */
    fun migrate2(data: ObjectNode) {
        data["log"]?.get("levels")?.nullCast<ObjectNode>()?.let {
            it.remove("NETWORK_RESOLVING")?.let { level -> it.replace("NETWORK", level) }
            it.remove("NETWORK_STATUS")
            it.remove("NETWORK_PACKETS_IN")?.let { level -> it.replace("NETWORK_IN", level) }
            it.remove("NETWORK_PACKETS_OUT")?.let { level -> it.replace("NETWORK_OUT", level) }
            it.remove("RENDERING_GENERAL")?.let { level -> it.replace("RENDERING", level) }
            it.remove("VERSION_LOADING")?.let { level -> it.replace("LOADING", level) }
            it.remove("RENDERING_LOADING")
        }
    }
}
