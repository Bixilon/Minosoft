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

public class TextComponent {
    JSONObject json;

    public TextComponent(String raw) {
        try {
            this.json = new JSONObject(raw);
        } catch (JSONException e) {
            // not a text component, is a legacy string
            this.json = new JSONObject();
            this.json.put("text", raw);
        }
    }

    public TextComponent(JSONObject json) {
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
                JSONObject object;
                try {
                    object = arr.getJSONObject(i);
                } catch (JSONException e) {
                    // reset text
                    buffer.append(arr.getString(i));
                    continue;
                }
                buffer.append(object.getString("text"));
            }
            buffer.append(ChatAttributes.RESET);
            return buffer.toString();
        }
        return "";
    }

    public JSONObject getRaw() {
        return this.json;
    }

    public String getColoredMessage() {
        if (json.has("text") && json.getString("text").length() != 0) {
            return json.getString("text");
        }
        StringBuilder buffer = new StringBuilder();
        if (json.has("extra")) {
            JSONArray arr = json.getJSONArray("extra");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject object;
                try {
                    object = arr.getJSONObject(i);
                } catch (JSONException e) {
                    // reset text
                    buffer.append(ChatAttributes.RESET);
                    buffer.append(arr.getString(i));
                    continue;
                }
                if (object.has("bold") && object.getBoolean("bold")) {
                    buffer.append(ChatAttributes.BOLD);
                }
                if (object.has("color")) {
                    buffer.append(ChatAttributes.byName(object.getString("color")));
                }
                if (object.has("italic") && object.getBoolean("italic")) {
                    buffer.append(ChatAttributes.ITALIC);
                }
                if (object.has("underlined") && object.getBoolean("underlined")) {
                    buffer.append(ChatAttributes.UNDERLINED);
                }
                if (object.has("strikethrough") && object.getBoolean("strikethrough")) {
                    buffer.append(ChatAttributes.STRIKETHROUGH);
                }
                if (object.has("obfuscated") && object.getBoolean("obfuscated")) {
                    buffer.append(ChatAttributes.OBFUSCATED);
                }
                buffer.append(object.getString("text"));
            }
            buffer.append(ChatAttributes.RESET);
            return buffer.toString();
        }
        return "";
    }

    public enum ChatAttributes {
        BLACK("\033[38;2;0;0;0m", Color.BLACK),
        DARK_BLUE("\033[38;2;0;0;170m", Color.DARK_BLUE),
        DARK_GREEN("\033[38;2;0;170;0m", Color.DARK_GREEN),
        DARK_AQUA("\033[38;2;0;170;170m", Color.DARK_AQUA),
        DARK_RED("\033[38;2;170;0;0m", Color.DARK_RED),
        DARK_PURPLE("\033[38;2;170;0;170m", Color.DARK_PURPLE),
        GOLD("\033[38;2;255;170;0m", Color.GOLD),
        GRAY("\033[38;2;170;170;170m", Color.GRAY),
        DARK_GRAY("\033[38;2;85;85;85m", Color.DARK_GRAY),
        BLUE("\033[38;2;85;85;255m", Color.DARK_BLUE),
        GREEN("\033[38;2;85;255;85m", Color.GREEN),
        AQUA("\033[38;2;85;255;255m", Color.AQUA),
        RED("\033[38;2;255;85;85m", Color.RED),
        PURPLE("\033[38;2;255;85;255m", Color.PURPLE, "light_purple"),
        YELLOW("\033[38;2;255;255;85m", Color.YELLOW),
        WHITE("\033[38;2;255;255;255m", Color.WHITE),

        RESET("\u001b[0m", "r"),
        BOLD("\u001b[1m", "l"),
        STRIKETHROUGH("\u001b[9m", "m"),
        UNDERLINED("\u001b[4m", "n"),
        ITALIC("\u001b[3m", "o"),
        OBFUSCATED("\u001b[47;1m", "k"); //ToDo

        final String consolePrefix;
        final Color color;
        final String prefix;
        final String name;

        ChatAttributes(String consolePrefix, Color color, String name) {
            this.consolePrefix = consolePrefix;
            this.color = color;
            this.name = name;
            this.prefix = null;
        }

        ChatAttributes(String consolePrefix, Color color) {
            this.consolePrefix = consolePrefix;
            this.color = color;
            this.name = null;
            this.prefix = null;
        }

        ChatAttributes(String consolePrefix, String prefix, String name) {
            this.consolePrefix = consolePrefix;
            this.prefix = prefix;
            this.color = null;
            this.name = name;
        }

        ChatAttributes(String consolePrefix, String prefix) {
            this.consolePrefix = consolePrefix;
            this.prefix = prefix;
            this.color = null;
            this.name = null;
        }


        public static ChatAttributes byName(String name) {
            for (ChatAttributes c : values()) {
                if ((c.getName() != null && c.getName().toLowerCase().equals(name.toLowerCase())) || c.name().toLowerCase().equals(name.toLowerCase())) {
                    return c;
                }
            }
            return null;
        }

        public static ChatAttributes byColor(Color color) {
            for (ChatAttributes c : values()) {
                if (c.getColor() == color) {
                    return c;
                }
            }
            return null;
        }

        public String getConsolePrefix() {
            return consolePrefix;
        }

        public String getPrefix() {
            if (prefix == null) {
                return color.getPrefix();
            }
            return prefix;
        }

        public Color getColor() {
            return color;
        }

        public String getName() {
            if (name == null) {
                return name();
            }
            return name;
        }

        @Override
        public String toString() {
            return getConsolePrefix();
        }
    }
}
