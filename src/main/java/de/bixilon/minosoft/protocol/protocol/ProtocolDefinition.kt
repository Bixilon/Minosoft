/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.protocol.protocol

import de.bixilon.minosoft.data.entities.EntityRotation
import java.util.regex.Pattern
import kotlin.time.Duration.Companion.seconds

object ProtocolDefinition {
    const val STRING_MAX_LENGTH = 32767
    const val DEFAULT_PORT = 25565
    val SOCKET_TIMEOUT = 30.seconds
    const val STATUS_PROTOCOL_PACKET_MAX_SIZE = 1 shl 16
    const val ROTATION_ANGLE_DIVIDER = EntityRotation.CIRCLE_DEGREE / 256.0f
    const val SOUND_PITCH_DIVIDER = 100.0f / 63.0f


    const val FLATTENING_VERSION = ProtocolVersions.V_17W47A
    const val QUERY_PROTOCOL_VERSION_ID = -1

    const val TEXT_COMPONENT_FORMATTING_PREFIX = '§'

    const val AIR_ID = 0


    val MINECRAFT_NAME_VALIDATOR = Pattern.compile("\\w{3,16}")

    const val VELOCITY_NETWORK_DIVIDER = 8000.0f
}
