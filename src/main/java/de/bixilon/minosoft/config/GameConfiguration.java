/*
 * Codename Minosoft
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

public enum GameConfiguration implements ConfigEnum {
    CONFIG_VERSION("version"),
    GAME_RENDER_DISTANCE("game.render-distance"),
    NETWORK_FAKE_CLIENT_BRAND("network.fake-client-brand"),
    GENERAL_LOG_LEVEL("general.log-level"),
    CLIENT_TOKEN("account.clientToken"),
    MAPPINGS_URL("download.mappings"),
    ACCOUNT_SELECTED("account.selected");

    final String path;

    GameConfiguration(String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }
}
