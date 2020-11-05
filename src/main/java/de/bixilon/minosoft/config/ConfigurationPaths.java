/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.config;

public abstract class ConfigurationPaths {
    public enum StringPaths {
        GENERAL_LOG_LEVEL,
        CLIENT_TOKEN,
        MAPPINGS_URL,
        ACCOUNT_SELECTED,
        GENERAL_LANGUAGE,
    }

    public enum BooleanPaths {
        NETWORK_FAKE_CLIENT_BRAND,
        NETWORK_SHOW_LAN_SERVERS,
        DEBUG_VERIFY_ASSETS
    }

    public enum IntegerPaths {
        GENERAL_CONFIG_VERSION,
        GAME_RENDER_DISTANCE
    }
}
