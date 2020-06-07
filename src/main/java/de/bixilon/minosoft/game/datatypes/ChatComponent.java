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

package de.bixilon.minosoft.game.datatypes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChatComponent {
    JSONObject json;
    //ToDo

    public ChatComponent(String raw) {
        try {
            this.json = new JSONObject(raw);
        } catch (JSONException e) {
            // not a text component, is a legacy string
            this.json = new JSONObject();
            this.json.put("text", raw);
        }
    }

    public ChatComponent(JSONObject json) {
        this.json = json;
    }

    //ToDo
    public String getRawMessage() {
        if (json.has("text") && json.getString("text").length() != 0) {
            return json.getString("text");
        }
        StringBuilder buffer = new StringBuilder();
        if (json.has("extra")) {
            JSONArray arr = json.getJSONArray("extra");
            for (int i = 0; i < arr.length(); i++) {
                buffer.append(arr.getJSONObject(i).getString("text"));
            }
            return buffer.toString();
        }
        return "";
    }

    public JSONObject getRaw() {
        return this.json;
    }

    public String getColoredMessage() {
        //ToDo
        return getRawMessage();
    }
}
