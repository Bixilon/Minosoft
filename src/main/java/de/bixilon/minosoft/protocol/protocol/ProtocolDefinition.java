/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.protocol;

import java.net.InetAddress;
import java.util.regex.Pattern;

public final class ProtocolDefinition {
    public static final int STRING_MAX_LEN = 32767;
    public static final int DEFAULT_PORT = 25565;
    public static final int SOCKET_CONNECT_TIMEOUT = 5000;
    public static final int SOCKET_TIMEOUT = 30000;
    public static final int PROTOCOL_PACKET_MAX_SIZE = 1 << 21;
    public static final float ANGLE_CALCULATION_CONSTANT = 360.0F / 256.0F;
    public static final float PITCH_CALCULATION_CONSTANT = 100.0F / 63.0F;

    public static final int PLAYER_INVENTORY_ID = 0;

    public static final int FLATTING_VERSION_ID = ProtocolVersions.V_17W47A;
    public static final int PRE_FLATTENING_VERSION_ID = ProtocolVersions.V_17W46A;

    public static final int FALLBACK_PROTOCOL_VERSION_ID = ProtocolVersions.V_1_8_9; // some servers (like cytooxien.de) send us version id -1.
    public static final int QUERY_PROTOCOL_VERSION_ID = -1;

    public static final int LAN_SERVER_BROADCAST_PORT = 4445;
    public static final String LAN_SERVER_BROADCAST_ADDRESS = "224.0.2.60";
    public static final InetAddress LAN_SERVER_BROADCAST_INET_ADDRESS;
    public static final int LAN_SERVER_MAXIMUM_SERVERS = 100; // maximum number of lan servers, set because otherwise dos attacks would be easy

    public static final String DEFAULT_MOD = "minecraft";
    public static final char TEXT_COMPONENT_SPECIAL_PREFIX_CHAR = '§';

    public static final int DEFAULT_BUFFER_SIZE = 4096;

    public static final int NULL_BLOCK_ID = 0;

    public static final String COMMAND_SEPARATOR = " ";

    public static final Pattern MINECRAFT_NAME_VALIDATOR = Pattern.compile("\\w{3,16}");
    public static final Pattern IDENTIFIER_PATTERN = Pattern.compile("([a-z_]+:)?[a-z_]+");
    public static final Pattern SCOREBOARD_OBJECTIVE_PATTERN = Pattern.compile("[a-zA-z-.+]{1,16}");

    public static final int SECTION_WIDTH_X = 16;
    public static final int SECTION_WIDTH_Z = 16;
    public static final int SECTION_HEIGHT_Y = 16;
    public static final int SECTIONS_PER_CHUNK = 16;
    public static final int BLOCKS_PER_SECTION = SECTION_WIDTH_X * SECTION_HEIGHT_Y * SECTION_WIDTH_X;

    public static final int SIGN_LINES = 4;
    public static final int ITEM_STACK_MAX_SIZE = 64;


    public static final String MOJANG_URL_VERSION_MANIFEST = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    public static final String MINECRAFT_URL_RESOURCES = "https://resources.download.minecraft.net/%s/%s";
    public static final String MOJANG_URL_PACKAGES = "https://launchermeta.mojang.com/v1/packages/%s/%s";
    public static final String MOJANG_LAUNCHER_URL_PACKAGES = "https://launcher.mojang.com/v1/objects/%s/%s";

    public static final String MOJANG_URL_BLOCKED_SERVERS = "https://sessionserver.mojang.com/blockedservers";
    public static final String MOJANG_URL_LOGIN = "https://authserver.mojang.com/authenticate";
    public static final String MOJANG_URL_JOIN = "https://sessionserver.mojang.com/session/minecraft/join";
    public static final String MOJANG_URL_REFRESH = "https://authserver.mojang.com/refresh";

    public static final char[] OBFUSCATED_CHARS = "!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~".toCharArray();

    static {
        // java does (why ever) not allow to directly assign a null
        InetAddress tempInetAddress;
        try {
            tempInetAddress = InetAddress.getByName(LAN_SERVER_BROADCAST_ADDRESS);
        } catch (Exception e) {
            e.printStackTrace();
            tempInetAddress = null;
        }
        LAN_SERVER_BROADCAST_INET_ADDRESS = tempInetAddress;
    }
}
