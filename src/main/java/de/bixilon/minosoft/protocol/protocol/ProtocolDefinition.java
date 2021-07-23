/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
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

import de.bixilon.minosoft.data.registries.ResourceLocation;
import de.bixilon.minosoft.data.text.ChatColors;
import de.bixilon.minosoft.data.text.RGBColor;

import java.net.InetAddress;
import java.util.regex.Pattern;

public final class ProtocolDefinition {
    public static final int STRING_MAX_LENGTH = 32767;
    public static final int DEFAULT_PORT = 25565;
    public static final int SOCKET_CONNECT_TIMEOUT = 5000;
    public static final int SOCKET_TIMEOUT = 30000;
    public static final int PROTOCOL_PACKET_MAX_SIZE = 1 << 23; // ToDp: This was before 1.17.1-rc2 1 << 21
    public static final float ANGLE_CALCULATION_CONSTANT = 360.0F / 256.0F;
    public static final float PITCH_CALCULATION_CONSTANT = 100.0F / 63.0F;

    public static final int PLAYER_INVENTORY_ID = 0;

    public static final int FLATTING_VERSION_ID = ProtocolVersions.V_17W47A;
    public static final int PRE_FLATTENING_VERSION_ID = ProtocolVersions.V_17W46A;

    public static final int QUERY_PROTOCOL_VERSION_ID = -1;

    public static final int LAN_SERVER_BROADCAST_PORT = 4445;
    public static final String LAN_SERVER_BROADCAST_ADDRESS = "224.0.2.60";
    public static final InetAddress LAN_SERVER_BROADCAST_INET_ADDRESS;
    public static final int LAN_SERVER_MAXIMUM_SERVERS = 100; // maximum number of lan servers, set because otherwise dos attacks would be easy

    public static final String DEFAULT_NAMESPACE = "minecraft";
    public static final String MINOSOFT_NAMESPACE = "minosoft";
    public static final char TEXT_COMPONENT_SPECIAL_PREFIX_CHAR = '§';

    public static final int DEFAULT_BUFFER_SIZE = 4096;

    public static final int NULL_BLOCK_ID = 0;


    public static final Pattern MINECRAFT_NAME_VALIDATOR = Pattern.compile("\\w{3,16}");
    public static final Pattern RESOURCE_LOCATION_PATTERN = Pattern.compile("([a-z_0-9]+:)?[a-zA-Z_0-9.]+");
    public static final Pattern SCOREBOARD_OBJECTIVE_PATTERN = Pattern.compile("[a-zA-z-.+]{1,16}");

    public static final int SECTION_WIDTH_X = 16;
    public static final int SECTION_MAX_X = SECTION_WIDTH_X - 1;
    public static final int SECTION_WIDTH_Z = 16;
    public static final int SECTION_MAX_Z = SECTION_WIDTH_Z - 1;
    public static final int SECTION_HEIGHT_Y = 16;
    public static final int SECTION_MAX_Y = SECTION_HEIGHT_Y - 1;
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

    public static final String MICROSOFT_ACCOUNT_APPLICATION_ID = "00000000402b5328"; // ToDo: Should we use our own application id?
    // public static final String MICROSOFT_ACCOUNT_APPLICATION_ID = "fe6f0fbf-3038-486a-9c84-6a28b71e0455";
    public static final String MICROSOFT_ACCOUNT_OAUTH_FLOW_URL = "https://login.microsoftonline.com/consumers/oauth2/v2.0/authorize?client_id=" + MICROSOFT_ACCOUNT_APPLICATION_ID + "&scope=XboxLive.signin%20offline_access&response_type=code";
    public static final String MICROSOFT_ACCOUNT_AUTH_TOKEN_URL = "https://login.live.com/oauth20_token.srf";
    public static final String MICROSOFT_ACCOUNT_XBOX_LIVE_AUTHENTICATE_URL = "https://user.auth.xboxlive.com/user/authenticate";
    public static final String MICROSOFT_ACCOUNT_XSTS_URL = "https://xsts.auth.xboxlive.com/xsts/authorize";
    public static final String MICROSOFT_ACCOUNT_MINECRAFT_LOGIN_WITH_XBOX_URL = "https://api.minecraftservices.com/authentication/login_with_xbox";
    public static final String MICROSOFT_ACCOUNT_GET_MOJANG_PROFILE_URL = "https://api.minecraftservices.com/minecraft/profile";

    public static final char[] OBFUSCATED_CHARS = "!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~".toCharArray();


    public static final int TICKS_PER_SECOND = 20;
    public static final int TICK_TIME = 1000 / TICKS_PER_SECOND;
    public static final float TICK_TIMEf = (float) TICK_TIME;
    public static final double TICK_TIMEd = TICK_TIME;

    public static final float VELOCITY_CONSTANT = 8000.0f;

    public static final float GRAVITY = 32;
    public static final boolean FAST_MOVEMENT = true;

    public static final int SEA_LEVEL_HEIGHT = 62;

    public static final float HEIGHT_SEA_LEVEL_MODIFIER = 0.00166667f;

    public static final ResourceLocation AIR_RESOURCE_LOCATION = new ResourceLocation("air");

    public static final RGBColor DEFAULT_COLOR = ChatColors.WHITE;

    public static final char[] LINE_BREAK_CHARS = {'\n', '\r'};


    public static final String[] RELEVANT_MINECRAFT_ASSETS = {"minecraft/lang/", "minecraft/sounds.json", "minecraft/sounds/", "minecraft/textures/", "minecraft/font/"}; // whitelist for all assets we care (we have our own block models, etc)

    public static final int TICKS_PER_DAY = 24000;
    public static final float TICKS_PER_DAYf = (float) TICKS_PER_DAY;

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
