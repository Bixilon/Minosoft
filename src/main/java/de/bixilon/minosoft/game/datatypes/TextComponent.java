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

import java.util.ArrayList;
import java.util.List;

public class TextComponent {
    JSONObject json;

    public TextComponent(String raw) {
        if (raw == null) {
            this.json = new JSONObject();
            return;
        }
        try {
            this.json = new JSONObject(raw);
        } catch (JSONException e) {
            // not a text component, is a legacy string
            this.json = new JSONObject();
            JSONArray extra = new JSONArray();

            String[] paragraphSplit = raw.split("§");

            StringBuilder message = new StringBuilder();
            List<ChatAttributes> attributesList = new ArrayList<>();
            ChatAttributes color = ChatAttributes.WHITE;
            boolean first = true;
            for (String paragraph : paragraphSplit) {
                if (paragraph.length() >= 1) {
                    if (first) {
                        // skip first, just append.
                        message.append(paragraph);
                        first = false;
                        continue;
                    }
                    // only 1 code without message, append to list
                    ChatColor colorCheck = null;
                    try {
                        colorCheck = ChatColor.byId(Integer.parseInt(paragraph.substring(0, 1), 16));
                    } catch (NumberFormatException ignored) {
                    }
                    if (colorCheck == null) {
                        //this is not a color, append attribute to list
                        switch (paragraph.substring(0, 1)) {
                            case "k":
                                attributesList.add(ChatAttributes.OBFUSCATED);
                                break;
                            case "l":
                                attributesList.add(ChatAttributes.BOLD);
                                break;
                            case "m":
                                attributesList.add(ChatAttributes.STRIKETHROUGH);
                                break;
                            case "n":
                                attributesList.add(ChatAttributes.UNDERLINED);
                                break;
                            case "o":
                                attributesList.add(ChatAttributes.ITALIC);
                                break;
                            case "r":
                                //save and clear
                                extra.put(getExtraByAttributes(message.toString(), color, attributesList));
                                attributesList.clear();
                                color = ChatAttributes.WHITE;
                                message = new StringBuilder();
                                break;
                        }
                    } else {
                        // save current
                        if (!message.toString().isEmpty()) {
                            extra.put(getExtraByAttributes(message.toString(), color, attributesList));
                            message = new StringBuilder();
                        }
                        color = ChatAttributes.byColor(colorCheck);
                    }
                    message.append(paragraph.substring(1));
                } else {
                    if (first) {
                        // skip first
                        first = false;
                    }
                }
            }
            // save
            extra.put(getExtraByAttributes(message.toString(), color, attributesList));

            this.json.put("extra", extra);
        }
    }

    public TextComponent(JSONObject json) {
        this.json = json;
    }

    static JSONObject getExtraByAttributes(String message, ChatAttributes color, List<ChatAttributes> formatting) {
        JSONObject ret = new JSONObject();
        ret.put("text", message);
        if (color != null) {
            ret.put("color", color.getName());
        }
        for (ChatAttributes attribute : formatting) {
            if (attribute == ChatAttributes.BOLD && !ret.has("bold")) {
                ret.put("bold", true);
            } else if (attribute == ChatAttributes.ITALIC && !ret.has("italic")) {
                ret.put("italic", true);
            } else if (attribute == ChatAttributes.UNDERLINED && !ret.has("underlined")) {
                ret.put("underlined", true);
            } else if (attribute == ChatAttributes.STRIKETHROUGH && !ret.has("strikethrough")) {
                ret.put("strikethrough", true);
            } else if (attribute == ChatAttributes.OBFUSCATED && !ret.has("obfuscated")) {
                ret.put("obfuscated", true);
            }
        }
        return ret;
    }

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

    @Override
    public String toString() {
        return getColoredMessage();
    }

    public enum ChatAttributes {
        BLACK("\033[38;2;0;0;0m", ChatColor.BLACK),
        DARK_BLUE("\033[38;2;0;0;170m", ChatColor.DARK_BLUE),
        DARK_GREEN("\033[38;2;0;170;0m", ChatColor.DARK_GREEN),
        DARK_AQUA("\033[38;2;0;170;170m", ChatColor.DARK_AQUA),
        DARK_RED("\033[38;2;170;0;0m", ChatColor.DARK_RED),
        DARK_PURPLE("\033[38;2;170;0;170m", ChatColor.DARK_PURPLE),
        GOLD("\033[38;2;255;170;0m", ChatColor.GOLD),
        GRAY("\033[38;2;170;170;170m", ChatColor.GRAY),
        DARK_GRAY("\033[38;2;85;85;85m", ChatColor.DARK_GRAY),
        BLUE("\033[38;2;85;85;255m", ChatColor.DARK_BLUE),
        GREEN("\033[38;2;85;255;85m", ChatColor.GREEN),
        AQUA("\033[38;2;85;255;255m", ChatColor.AQUA),
        RED("\033[38;2;255;85;85m", ChatColor.RED),
        PURPLE("\033[38;2;255;85;255m", ChatColor.PURPLE, "light_purple"),
        YELLOW("\033[38;2;255;255;85m", ChatColor.YELLOW),
        WHITE("\033[38;2;255;255;255m", ChatColor.WHITE),

        RESET("\u001b[0m", "r"),
        BOLD("\u001b[1m", "l"),
        STRIKETHROUGH("\u001b[9m", "m"),
        UNDERLINED("\u001b[4m", "n"),
        ITALIC("\u001b[3m", "o"),
        OBFUSCATED("\u001b[47;1m", "k"); // ToDo

        final String consolePrefix;
        final ChatColor color;
        final String prefix;
        final String name;

        ChatAttributes(String consolePrefix, ChatColor color, String name) {
            this.consolePrefix = consolePrefix;
            this.color = color;
            this.name = name;
            this.prefix = null;
        }

        ChatAttributes(String consolePrefix, ChatColor color) {
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

        public static ChatAttributes byColor(ChatColor color) {
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

        public ChatColor getColor() {
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
