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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.util.ArrayList;

public class TextComponent {
    JsonObject json;

    public TextComponent(String raw) {
        if (raw == null) {
            this.json = new JsonObject();
            return;
        }
        try {
            this.json = JsonParser.parseString(raw).getAsJsonObject();
        } catch (Exception e) {
            // not a text component, is a legacy string
            this.json = new JsonObject();
            JsonArray extra = new JsonArray();

            String[] paragraphSplit = raw.split("§");

            StringBuilder message = new StringBuilder();
            ArrayList<ChatAttributes> attributesList = new ArrayList<>();
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
                    ChatColors colorCheck = null;
                    try {
                        colorCheck = ChatColors.byId(Integer.parseInt(paragraph.substring(0, 1), 16));
                    } catch (NumberFormatException ignored) {
                    }
                    if (colorCheck == null) {
                        //this is not a color, append attribute to list
                        //save and clear
                        switch (paragraph.substring(0, 1)) {
                            case "k" -> attributesList.add(ChatAttributes.OBFUSCATED);
                            case "l" -> attributesList.add(ChatAttributes.BOLD);
                            case "m" -> attributesList.add(ChatAttributes.STRIKETHROUGH);
                            case "n" -> attributesList.add(ChatAttributes.UNDERLINED);
                            case "o" -> attributesList.add(ChatAttributes.ITALIC);
                            case "r" -> {
                                extra.add(getExtraByAttributes(message.toString(), color, attributesList));
                                attributesList.clear();
                                color = ChatAttributes.WHITE;
                                message = new StringBuilder();
                            }
                        }
                    } else {
                        // save current
                        if (!message.toString().isEmpty()) {
                            extra.add(getExtraByAttributes(message.toString(), color, attributesList));
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
            extra.add(getExtraByAttributes(message.toString(), color, attributesList));

            this.json.add("extra", extra);
        }
    }

    public TextComponent(JsonObject json) {
        this.json = json;
    }

    static JsonObject getExtraByAttributes(String message, ChatAttributes color, ArrayList<ChatAttributes> formatting) {
        JsonObject ret = new JsonObject();
        ret.addProperty("text", message);
        if (color != null) {
            ret.addProperty("color", color.getName());
        }
        for (ChatAttributes attribute : formatting) {
            if (attribute == ChatAttributes.BOLD && !ret.has("bold")) {
                ret.addProperty("bold", true);
            } else if (attribute == ChatAttributes.ITALIC && !ret.has("italic")) {
                ret.addProperty("italic", true);
            } else if (attribute == ChatAttributes.UNDERLINED && !ret.has("underlined")) {
                ret.addProperty("underlined", true);
            } else if (attribute == ChatAttributes.STRIKETHROUGH && !ret.has("strikethrough")) {
                ret.addProperty("strikethrough", true);
            } else if (attribute == ChatAttributes.OBFUSCATED && !ret.has("obfuscated")) {
                ret.addProperty("obfuscated", true);
            }
        }
        return ret;
    }

    public String getRawMessage() {
        if (json.has("text") && json.get("text").getAsString().length() != 0) {
            return json.get("text").getAsString();
        }
        StringBuilder buffer = new StringBuilder();
        if (json.has("extra")) {
            JsonArray arr = json.getAsJsonArray("extra");
            for (int i = 0; i < arr.size(); i++) {
                JsonObject object;
                try {
                    object = arr.get(i).getAsJsonObject();
                } catch (JsonParseException e) {
                    // reset text
                    buffer.append(arr.get(i).getAsString());
                    continue;
                }
                buffer.append(object.get("text").getAsString());
            }
            buffer.append(ChatAttributes.RESET);
            return buffer.toString();
        }
        return "";
    }

    public JsonObject getRaw() {
        return this.json;
    }

    public String getColoredMessage() {
        if (json.has("text") && json.get("text").getAsString().length() != 0) {
            return json.get("text").getAsString();
        }
        StringBuilder buffer = new StringBuilder();
        if (json.has("extra")) {
            JsonArray arr = json.getAsJsonArray("extra");
            for (int i = 0; i < arr.size(); i++) {
                if (arr.get(i).isJsonPrimitive()) {
                    buffer.append(ChatAttributes.RESET);
                    buffer.append(" ");
                    buffer.append(arr.get(i).getAsString());
                    continue;
                }
                JsonObject object = arr.get(i).getAsJsonObject();
                if (object.has("bold") && object.get("bold").getAsBoolean()) {
                    buffer.append(ChatAttributes.BOLD);
                }
                if (object.has("color")) {
                    buffer.append(ChatAttributes.byName(object.get("color").getAsString()));
                }
                if (object.has("italic") && object.get("italic").getAsBoolean()) {
                    buffer.append(ChatAttributes.ITALIC);
                }
                if (object.has("underlined") && object.get("underlined").getAsBoolean()) {
                    buffer.append(ChatAttributes.UNDERLINED);
                }
                if (object.has("strikethrough") && object.get("strikethrough").getAsBoolean()) {
                    buffer.append(ChatAttributes.STRIKETHROUGH);
                }
                if (object.has("obfuscated") && object.get("obfuscated").getAsBoolean()) {
                    buffer.append(ChatAttributes.OBFUSCATED);
                }
                buffer.append(object.get("text").getAsString());
            }
            buffer.append(ChatAttributes.RESET);
        }
        if (json.has("with")) {
            JsonArray arr = json.getAsJsonArray("with");
            for (int i = 0; i < arr.size(); i++) {
                if (arr.get(i).isJsonPrimitive()) {
                    buffer.append(ChatAttributes.RESET);
                    buffer.append(" ");
                    buffer.append(arr.get(i).getAsString());
                    continue;
                }
                JsonObject object = arr.get(i).getAsJsonObject();
                buffer.append(object.get("text").getAsString());
            }
            buffer.append(ChatAttributes.RESET);
        }
        return buffer.toString();
    }

    @Override
    public String toString() {
        return getColoredMessage();
    }

    public enum ChatAttributes {
        BLACK("\033[38;2;0;0;0m", ChatColors.BLACK),
        DARK_BLUE("\033[38;2;0;0;170m", ChatColors.DARK_BLUE),
        DARK_GREEN("\033[38;2;0;170;0m", ChatColors.DARK_GREEN),
        DARK_AQUA("\033[38;2;0;170;170m", ChatColors.DARK_AQUA),
        DARK_RED("\033[38;2;170;0;0m", ChatColors.DARK_RED),
        DARK_PURPLE("\033[38;2;170;0;170m", ChatColors.DARK_PURPLE),
        GOLD("\033[38;2;255;170;0m", ChatColors.GOLD),
        GRAY("\033[38;2;170;170;170m", ChatColors.GRAY),
        DARK_GRAY("\033[38;2;85;85;85m", ChatColors.DARK_GRAY),
        BLUE("\033[38;2;85;85;255m", ChatColors.DARK_BLUE),
        GREEN("\033[38;2;85;255;85m", ChatColors.GREEN),
        AQUA("\033[38;2;85;255;255m", ChatColors.AQUA),
        RED("\033[38;2;255;85;85m", ChatColors.RED),
        PURPLE("\033[38;2;255;85;255m", ChatColors.PURPLE, "light_purple"),
        YELLOW("\033[38;2;255;255;85m", ChatColors.YELLOW),
        WHITE("\033[38;2;255;255;255m", ChatColors.WHITE),

        RESET("\u001b[0m", "r"),
        BOLD("\u001b[1m", "l"),
        STRIKETHROUGH("\u001b[9m", "m"),
        UNDERLINED("\u001b[4m", "n"),
        ITALIC("\u001b[3m", "o"),
        OBFUSCATED("\u001b[47;1m", "k"); // ToDo

        final String consolePrefix;
        final ChatColors color;
        final String prefix;
        final String name;

        ChatAttributes(String consolePrefix, ChatColors color, String name) {
            this.consolePrefix = consolePrefix;
            this.color = color;
            this.name = name;
            this.prefix = null;
        }

        ChatAttributes(String consolePrefix, ChatColors color) {
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
            for (ChatAttributes attribute : values()) {
                if ((attribute.getName() != null && attribute.getName().toLowerCase().equals(name.toLowerCase())) || attribute.name().toLowerCase().equals(name.toLowerCase())) {
                    return attribute;
                }
            }
            return null;
        }

        public static ChatAttributes byColor(ChatColors color) {
            for (ChatAttributes attribute : values()) {
                if (attribute.getColor() == color) {
                    return attribute;
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

        public ChatColors getColor() {
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
