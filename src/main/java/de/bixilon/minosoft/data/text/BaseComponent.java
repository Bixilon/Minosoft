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

package de.bixilon.minosoft.data.text;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.bixilon.minosoft.modding.event.events.annotations.Unsafe;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import de.bixilon.minosoft.util.hash.BetterHashSet;
import javafx.collections.ObservableList;
import javafx.scene.Node;

import javax.annotation.Nullable;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;

public class BaseComponent extends ChatComponent {
    private final ArrayList<ChatComponent> parts = new ArrayList<>();

    public BaseComponent() {
    }

    public BaseComponent(String text) {
        // legacy String
        StringBuilder currentText = new StringBuilder();
        RGBColor color = null;
        BetterHashSet<ChatFormattingCode> formattingCodes = new BetterHashSet<>();
        StringCharacterIterator iterator = new StringCharacterIterator(text);
        while (iterator.current() != CharacterIterator.DONE) {
            char c = iterator.current();
            iterator.next();
            if (c != ProtocolDefinition.TEXT_COMPONENT_SPECIAL_PREFIX_CHAR) {
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
                formattingCodes = new BetterHashSet<>();
                continue;
            }
            ChatFormattingCode nextFormattingCode = ChatFormattingCodes.getChatFormattingCodeByChar(nextFormattingChar);
            if (nextFormattingCode != null) {
                if (currentText.length() > 0) {
                    parts.add(new TextComponent(currentText.toString(), color, formattingCodes));
                    currentText = new StringBuilder();
                    color = null;
                    formattingCodes = new BetterHashSet<>();
                }
                formattingCodes.add(nextFormattingCode);
                if (nextFormattingCode == PostChatFormattingCodes.RESET) {
                    // special rule here
                    if (currentText.length() > 0) {
                        // color change, add text part
                        parts.add(new TextComponent(currentText.toString(), color, formattingCodes));
                        currentText = new StringBuilder();
                    }
                    color = null;
                    formattingCodes = new BetterHashSet<>();
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
            if (text.contains(String.valueOf(ProtocolDefinition.TEXT_COMPONENT_SPECIAL_PREFIX_CHAR))) {
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
            BetterHashSet<ChatFormattingCode> formattingCodes;
            if (parent != null && parent.getFormatting() != null) {
                formattingCodes = (BetterHashSet<ChatFormattingCode>) parent.getFormatting().clone();
            } else {
                formattingCodes = new BetterHashSet<>();
            }
            if (json.has("bold")) {
                formattingCodes.addOrRemove(PreChatFormattingCodes.BOLD, json.get("bold").getAsBoolean());
            }
            if (json.has("italic")) {
                formattingCodes.addOrRemove(PreChatFormattingCodes.ITALIC, json.get("italic").getAsBoolean());
            }
            if (json.has("underlined")) {
                formattingCodes.addOrRemove(PreChatFormattingCodes.UNDERLINED, json.get("underlined").getAsBoolean());
            }
            if (json.has("strikethrough")) {
                formattingCodes.addOrRemove(PreChatFormattingCodes.STRIKETHROUGH, json.get("strikethrough").getAsBoolean());
            }
            if (json.has("obfuscated")) {
                formattingCodes.addOrRemove(PreChatFormattingCodes.OBFUSCATED, json.get("obfuscated").getAsBoolean());
            }
            thisTextComponent = new TextComponent(text, color, formattingCodes);
        }

        if (thisTextComponent != null) {
            parts.add(thisTextComponent);
        }

        final TextComponent parentParameter = thisTextComponent == null ? parent : thisTextComponent;
        if (json.has("extra")) {
            JsonArray extras = json.getAsJsonArray("extra");
            extras.forEach((extra -> parts.add(new BaseComponent(parentParameter, extra.getAsJsonObject()))));
        }

        if (json.has("translate")) {
            parts.add(new TranslatableComponent(parentParameter, json.get("translate").getAsString(), json.getAsJsonArray("with")));
        }
    }

    @Override
    public String toString() {
        return PostChatFormattingCodes.RESET.getANSI() + getANSIColoredMessage();
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

    @Override
    public ObservableList<Node> getJavaFXText(ObservableList<Node> nodes) {
        parts.forEach((chatPart) -> chatPart.getJavaFXText(nodes));
        return nodes;
    }

    @Unsafe
    public ArrayList<ChatComponent> getParts() {
        return parts;
    }

    public BaseComponent append(ChatComponent component) {
        parts.add(component);
        return this;
    }

    public BaseComponent append(String message) {
        parts.add(new BaseComponent(message));
        return this;
    }
}
