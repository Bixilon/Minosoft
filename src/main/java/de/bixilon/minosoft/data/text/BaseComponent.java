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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
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
    private static final String LEGACY_RESET_SUFFIX = String.valueOf(ProtocolDefinition.TEXT_COMPONENT_SPECIAL_PREFIX_CHAR) + PostChatFormattingCodes.RESET.getChar();
    private final ArrayList<ChatComponent> parts = new ArrayList<>();

    public BaseComponent() {
    }

    public BaseComponent(String text) {
        this(null, text);
    }

    public BaseComponent(@Nullable ChatComponent parent, String text) {
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
            if (nextColor != null) {
                // color change, add text part
                if (!currentText.isEmpty()) {
                    this.parts.add(new TextComponent(currentText.toString(), color, formattingCodes));
                    currentText = new StringBuilder();
                }
                color = nextColor;
                formattingCodes = new BetterHashSet<>();
                continue;
            }
            ChatFormattingCode nextFormattingCode = ChatFormattingCodes.getChatFormattingCodeByChar(nextFormattingChar);
            if (nextFormattingCode != null) {
                if (!currentText.isEmpty()) {
                    this.parts.add(new TextComponent(currentText.toString(), color, formattingCodes));
                    currentText = new StringBuilder();
                    color = null;
                    formattingCodes = new BetterHashSet<>();
                }
                formattingCodes.add(nextFormattingCode);
                if (nextFormattingCode == PostChatFormattingCodes.RESET) {
                    // special rule here
                    if (!currentText.isEmpty()) {
                        // color change, add text part
                        this.parts.add(new TextComponent(currentText.toString(), color, formattingCodes));
                        currentText = new StringBuilder();
                    }
                    color = null;
                    formattingCodes = new BetterHashSet<>();
                }
            }
        }
        if (!currentText.isEmpty()) {
            this.parts.add(new TextComponent(currentText.toString(), color, formattingCodes));
        }
    }

    public BaseComponent(JsonObject json) {
        this(null, json);
    }

    @SuppressWarnings("unchecked")
    public BaseComponent(@Nullable TextComponent parent, JsonElement data) {
        MultiChatComponent thisTextComponent = null;
        if (data instanceof JsonObject json) {
            if (json.has("text")) {
                String text = json.get("text").getAsString();
                if (text.contains(String.valueOf(ProtocolDefinition.TEXT_COMPONENT_SPECIAL_PREFIX_CHAR))) {
                    // legacy text component
                    this.parts.add(new BaseComponent(text));
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

                thisTextComponent = new MultiChatComponent(text, color, formattingCodes);
                if (json.has("clickEvent")) {
                    thisTextComponent.setClickEvent(new ClickEvent(json.get("clickEvent").getAsJsonObject()));
                }
                if (json.has("hoverEvent")) {
                    thisTextComponent.setHoverEvent(new HoverEvent(json.get("hoverEvent").getAsJsonObject()));
                }
            }

            if (thisTextComponent != null) {
                this.parts.add(thisTextComponent);
            }

            final TextComponent parentParameter = thisTextComponent == null ? parent : thisTextComponent;
            if (json.has("extra")) {
                JsonArray extras = json.getAsJsonArray("extra");
                extras.forEach((extra -> this.parts.add(new BaseComponent(parentParameter, extra))));
            }

            if (json.has("translate")) {
                this.parts.add(new TranslatableComponent(parentParameter, json.get("translate").getAsString(), json.getAsJsonArray("with")));
            }
        } else if (data instanceof JsonPrimitive primitive) {
            this.parts.add(new BaseComponent(parent, primitive.getAsString()));
        }
    }

    @Override
    public String toString() {
        return PostChatFormattingCodes.RESET.getANSI() + getANSIColoredMessage();
    }

    @Override
    public String getANSIColoredMessage() {
        StringBuilder builder = new StringBuilder();
        this.parts.forEach((chatPart -> builder.append(chatPart.getANSIColoredMessage())));
        return builder.toString();
    }

    @Override
    public String getLegacyText() {
        StringBuilder builder = new StringBuilder();
        this.parts.forEach((chatPart -> builder.append(chatPart.getLegacyText())));
        String string = builder.toString();
        if (string.endsWith(LEGACY_RESET_SUFFIX)) {
            string = string.substring(0, string.length() - LEGACY_RESET_SUFFIX.length());
        }
        return string;
    }

    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder();
        this.parts.forEach((chatPart -> builder.append(chatPart.getMessage())));
        return builder.toString();
    }

    @Override
    public ObservableList<Node> getJavaFXText(ObservableList<Node> nodes) {
        this.parts.forEach((chatPart) -> chatPart.getJavaFXText(nodes));
        return nodes;
    }

    @Unsafe
    public ArrayList<ChatComponent> getParts() {
        return this.parts;
    }

    public BaseComponent append(ChatComponent component) {
        this.parts.add(component);
        return this;
    }

    public BaseComponent append(String message) {
        this.parts.add(new BaseComponent(message));
        return this;
    }

    public boolean isEmpty() {
        return this.parts.isEmpty();
    }
}
