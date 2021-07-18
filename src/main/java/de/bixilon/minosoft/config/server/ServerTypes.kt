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

package de.bixilon.minosoft.config.server

import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.enum.ValuesEnum

enum class ServerTypes {
    /**
     * This server will be written to the config file, just the normal behavior you'd expect
     */
    NORMAL,

    /**
     * A server, that won't be saved to the config, but it can be edited
     */
    TEMPORARY,

    /**
     * This server is basically the same as TEMPORARY, but it can't be edited
     */
    LAN_SERVER,

    /**
     * This server won't be saved, it won't be shown in the list. Just used for direct connections
     */
    DIRECT_CONNECT,
    ;

    companion object : ValuesEnum<ServerTypes> {
        override val VALUES: Array<ServerTypes> = values()
        override val NAME_MAP: Map<String, ServerTypes> = KUtil.getEnumValues(VALUES)
    }
}
