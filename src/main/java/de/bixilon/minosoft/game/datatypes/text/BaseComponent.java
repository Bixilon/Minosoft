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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.HashSet;

public class BaseComponent implements ChatComponent {
    private final ArrayList<ChatComponent> parts = new ArrayList<>();

    public BaseComponent() {
    }

    public BaseComponent(String text) {
        // legacy String
        StringBuilder currentText = new StringBuilder();
        RGBColor color = null;
        HashSet<ChatFormattingCodes> formattingCodes = new HashSet<>();
        StringCharacterIterator iterator = new StringCharacterIterator(text);
        while (iterator.current() != CharacterIterator.DONE) {
            char c = iterator.current();
            iterator.next();
            if (c != '§') {
                currentText.append(c);
                continue;
            }
            // check next char
            char nextFormattingChar = iterator.current();
            iterator.next();
            RGBColor nextColor = ChatColors.getColorByFormattingChar(nextFormattingChar);
            if (nextColor != null && nextColor != color) {
                // color change, add text part
                if (currentText.length() > 0) {
                    parts.add(new TextComponent(currentText.toString(), color, formattingCodes));
                    currentText = new StringBuilder();
                }
                color = nextColor;
                formattingCodes = new HashSet<>();
                continue;
            }
            ChatFormattingCodes nextFormattingCode = ChatFormattingCodes.getChatFormattingCodeByChar(nextFormattingChar);
            if (nextFormattingCode != null) {
                if (currentText.length() > 0) {
                    parts.add(new TextComponent(currentText.toString(), color, formattingCodes));
                    currentText = new StringBuilder();
                    color = null;
                    formattingCodes = new HashSet<>();
                }
                formattingCodes.add(nextFormattingCode);
                if (nextFormattingCode == ChatFormattingCodes.RESET) {
                    // special rule here
                    if (currentText.length() > 0) {
                        // color change, add text part
                        parts.add(new TextComponent(currentText.toString(), color, formattingCodes));
                        currentText = new StringBuilder();
                    }
                    color = null;
                    formattingCodes = new HashSet<>();
                }
            }
        }
        if (currentText.length() > 0) {
            parts.add(new TextComponent(currentText.toString(), color, formattingCodes));
        }
    }


    public BaseComponent(JsonObject json) {
        this(null, json);
    }

    public BaseComponent(@Nullable TextComponent parent, JsonObject json) {
        TextComponent thisTextComponent = null;
        if (json.has("text")) {
            String text = json.get("text").getAsString();
            if (text.contains("§")) {
                // legacy text component
                parts.add(new BaseComponent(text));
                return;
            }
            RGBColor color;
            if (parent != null && parent.getColor() != null) {
                color = parent.getColor();
            } else {
                color = null;
            }
            if (json.has("color")) {
                String colorString = json.get("color").getAsString();
                if (colorString.startsWith("#")) {
                    // RGB
                    color = new RGBColor(colorString);
                } else {
                    color = ChatColors.getColorByName(colorString);
                }
            }
            HashSet<ChatFormattingCodes> formattingCodes;
            if (parent != null && parent.getFormatting() != null) {
                formattingCodes = (HashSet<ChatFormattingCodes>) parent.getFormatting().clone();
            } else {
                formattingCodes = new HashSet<>();
            }
            if (json.has("bold")) {
                if (json.get("bold").getAsBoolean()) {
                    formattingCodes.add(ChatFormattingCodes.BOLD);
                } else {
                    formattingCodes.remove(ChatFormattingCodes.BOLD);
                }
            }
            if (json.has("italic")) {
                if (json.get("italic").getAsBoolean()) {
                    formattingCodes.add(ChatFormattingCodes.ITALIC);
                } else {
                    formattingCodes.remove(ChatFormattingCodes.ITALIC);
                }
            }
            if (json.has("underlined")) {
                if (json.get("underlined").getAsBoolean()) {
                    formattingCodes.add(ChatFormattingCodes.UNDERLINED);
                } else {
                    formattingCodes.remove(ChatFormattingCodes.UNDERLINED);

                }
            }
            if (json.has("strikethrough")) {
                if (json.get("strikethrough").getAsBoolean()) {
                    formattingCodes.add(ChatFormattingCodes.STRIKETHROUGH);
                } else {
                    formattingCodes.remove(ChatFormattingCodes.STRIKETHROUGH);
                }
            }
            if (json.has("obfuscated")) {
                if (json.get("obfuscated").getAsBoolean()) {
                    formattingCodes.add(ChatFormattingCodes.OBFUSCATED);
                } else {
                    formattingCodes.remove(ChatFormattingCodes.OBFUSCATED);
                }
            }
            thisTextComponent = new TextComponent(text, color, formattingCodes);
        }


        if (json.has("extra")) {
            JsonArray extras = json.getAsJsonArray("extra");
            TextComponent finalThisChatPart = thisTextComponent;
            extras.forEach((extra -> parts.add(new BaseComponent(finalThisChatPart, extra.getAsJsonObject()))));
        }
        if (thisTextComponent != null) {
            parts.add(thisTextComponent);
        }
    }

    @Override
    public String toString() {
        return getANSIColoredMessage();
    }

    public String getANSIColoredMessage() {
        StringBuilder builder = new StringBuilder();
        parts.forEach((chatPart -> builder.append(chatPart.getANSIColoredMessage())));
        return builder.toString();
    }

    public String getLegacyText() {
        StringBuilder builder = new StringBuilder();
        parts.forEach((chatPart -> builder.append(chatPart.getLegacyText())));
        return builder.toString();
    }

    public String getMessage() {
        StringBuilder builder = new StringBuilder();
        parts.forEach((chatPart -> builder.append(chatPart.getMessage())));
        return builder.toString();
    }
}