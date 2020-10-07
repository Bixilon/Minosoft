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

package de.bixilon.minosoft.game.datatypes.text;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public interface ChatComponent {
    static ChatComponent fromString(String raw) {
        if (raw == null) {
            return new BaseComponent();
        }
        try {
            return new BaseComponent(JsonParser.parseString(raw).getAsJsonObject());
        } catch (JsonParseException | IllegalStateException ignored) {
        }
        return new BaseComponent(raw);
    }

    String toString();

    String getANSIColoredMessage();

    String getLegacyText();

    String getMessage();
}