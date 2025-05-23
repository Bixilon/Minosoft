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

package de.bixilon.minosoft.protocol.protocol;

import java.util.regex.Pattern;

import static de.bixilon.minosoft.data.entities.EntityRotation.CIRCLE_DEGREE;

public final class ProtocolDefinition {
    public static final int STRING_MAX_LENGTH = 32767;
    public static final int DEFAULT_PORT = 25565;
    public static final int SOCKET_TIMEOUT = 30000;
    public static final int STATUS_PROTOCOL_PACKET_MAX_SIZE = 1 << 16;
    public static final float ROTATION_ANGLE_DIVIDER = CIRCLE_DEGREE / 256.0F;
    public static final float SOUND_PITCH_DIVIDER = 100.0F / 63.0F;


    public static final int FLATTENING_VERSION = ProtocolVersions.V_17W47A;
    public static final int QUERY_PROTOCOL_VERSION_ID = -1;

    public static final char TEXT_COMPONENT_FORMATTING_PREFIX = '§';

    public static final int AIR_BLOCK_ID = 0;


    public static final Pattern MINECRAFT_NAME_VALIDATOR = Pattern.compile("\\w{3,16}");


    public static final float VELOCITY_NETWORK_DIVIDER = 8000.0f;
}
