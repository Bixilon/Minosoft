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

package de.bixilon.minosoft.protocol.protocol;

import de.bixilon.kotlinglm.vec3.Vec3i;
import de.bixilon.minosoft.data.text.formatting.color.ChatColors;
import de.bixilon.minosoft.data.text.formatting.color.RGBColor;

import java.util.regex.Pattern;

public final class ProtocolDefinition {
    public static final int STRING_MAX_LENGTH = 32767;
    public static final int DEFAULT_PORT = 25565;
    public static final int SOCKET_TIMEOUT = 30000;
    public static final int STATUS_PROTOCOL_PACKET_MAX_SIZE = 1 << 16;
    public static final float ANGLE_CALCULATION_CONSTANT = 360.0F / 256.0F;
    public static final float PITCH_CALCULATION_CONSTANT = 100.0F / 63.0F;

    public static final int PLAYER_CONTAINER_ID = 0;

    public static final int FLATTING_VERSION_ID = ProtocolVersions.V_17W47A;
    public static final int PRE_FLATTENING_VERSION_ID = ProtocolVersions.V_17W46A;

    public static final int QUERY_PROTOCOL_VERSION_ID = -1;

    public static final int LAN_SERVER_BROADCAST_PORT = 4445;
    public static final String LAN_SERVER_BROADCAST_ADDRESS = "224.0.2.60";
    public static final int LAN_SERVER_MAXIMUM_SERVERS = 100; // maximum number of lan servers, set because otherwise dos attacks would be easy

    public static final String DEFAULT_NAMESPACE = "minecraft";
    public static final String MINOSOFT_NAMESPACE = "minosoft";
    public static final char TEXT_COMPONENT_SPECIAL_PREFIX_CHAR = '\u00A7';

    public static final int DEFAULT_BUFFER_SIZE = 4096;

    public static final int AIR_BLOCK_ID = 0;


    public static final Pattern MINECRAFT_NAME_VALIDATOR = Pattern.compile("\\w{3,16}");

    public static final int SECTION_LENGTH = 16;
    public static final int SECTION_WIDTH_X = SECTION_LENGTH;
    public static final int SECTION_MAX_X = SECTION_WIDTH_X - 1;
    public static final int SECTION_WIDTH_Z = SECTION_LENGTH;
    public static final int SECTION_MAX_Z = SECTION_WIDTH_Z - 1;
    public static final int SECTION_HEIGHT_Y = SECTION_LENGTH;
    public static final int SECTION_MAX_Y = SECTION_HEIGHT_Y - 1;
    public static final int BLOCKS_PER_SECTION = SECTION_WIDTH_X * SECTION_HEIGHT_Y * SECTION_WIDTH_X;
    public static final Vec3i CHUNK_SECTION_SIZE = new Vec3i(ProtocolDefinition.SECTION_WIDTH_X, ProtocolDefinition.SECTION_HEIGHT_Y, ProtocolDefinition.SECTION_WIDTH_Z);

    public static final int CHUNK_MIN_Y = -2048;
    public static final int CHUNK_MIN_SECTION = CHUNK_MIN_Y / SECTION_HEIGHT_Y;
    public static final int CHUNK_MAX_Y = 2048;
    public static final int CHUNK_MAX_SECTION = CHUNK_MAX_Y / SECTION_HEIGHT_Y;
    public static final int CHUNK_MAX_HEIGHT = CHUNK_MAX_Y - CHUNK_MIN_Y;
    public static final int CHUNK_MAX_SECTIONS = CHUNK_MAX_HEIGHT / SECTION_HEIGHT_Y;
    public static final int ITEM_STACK_MAX_SIZE = 64;

    public static final char[] OBFUSCATED_CHARS = "!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~".toCharArray();


    public static final int TICKS_PER_SECOND = 20;
    public static final int TICK_TIME = 1000 / TICKS_PER_SECOND;
    public static final float TICK_TIMEf = (float) TICK_TIME;
    public static final double TICK_TIMEd = TICK_TIME;

    public static final float VELOCITY_NETWORK_DIVIDER = 8000.0f;

    public static final int SEA_LEVEL_HEIGHT = 62;

    public static final float HEIGHT_SEA_LEVEL_MODIFIER = 0.00166667f;

    public static final RGBColor DEFAULT_COLOR = ChatColors.WHITE;

    public static final int TICKS_PER_DAY = 24000;
    public static final float TICKS_PER_DAYf = (float) TICKS_PER_DAY;

    public static final byte LIGHT_LEVELS = 16;
    public static final byte MAX_LIGHT_LEVEL = LIGHT_LEVELS - 1;
    public static final int MAX_LIGHT_LEVEL_I = MAX_LIGHT_LEVEL;


    public static final String VANILLA_BRAND = "vanilla";
    public static final String MINOSOFT_BRAND = "minosoft";
}
