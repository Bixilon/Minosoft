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

import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.config.ConfigurationPaths;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import de.bixilon.minosoft.util.Util;
import de.bixilon.minosoft.util.hash.BetterHashSet;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.Objects;

public class TextComponent extends ChatComponent {
    private final String text;
    private RGBColor color;
    private BetterHashSet<ChatFormattingCode> formatting;

    public TextComponent(String text, RGBColor color, BetterHashSet<ChatFormattingCode> formatting) {
        this.text = text;
        this.color = color;
        this.formatting = formatting;
    }

    public TextComponent(String text, RGBColor color) {
        this.text = text;
        this.color = color;
    }

    public TextComponent(String text) {
        this.text = text;
    }

    public TextComponent setObfuscated(boolean obfuscated) {
        this.formatting.addOrRemove(PreChatFormattingCodes.OBFUSCATED, obfuscated);
        return this;
    }

    public TextComponent setBold(boolean bold) {
        this.formatting.addOrRemove(PreChatFormattingCodes.BOLD, bold);
        return this;
    }

    public TextComponent setStrikethrough(boolean strikethrough) {
        this.formatting.addOrRemove(PreChatFormattingCodes.STRIKETHROUGH, strikethrough);
        return this;
    }

    public TextComponent setUnderlined(boolean underlined) {
        this.formatting.addOrRemove(PreChatFormattingCodes.UNDERLINED, underlined);
        return this;
    }

    public TextComponent setItalic(boolean italic) {
        this.formatting.addOrRemove(PreChatFormattingCodes.ITALIC, italic);
        return this;
    }

    public TextComponent setReset(boolean reset) {
        this.formatting.addOrRemove(PostChatFormattingCodes.RESET, reset);
        return this;
    }

    public RGBColor getColor() {
        return this.color;
    }

    public TextComponent setColor(RGBColor color) {
        this.color = color;
        return this;
    }

    public BetterHashSet<ChatFormattingCode> getFormatting() {
        return this.formatting;
    }

    public TextComponent setFormatting(BetterHashSet<ChatFormattingCode> formatting) {
        this.formatting = formatting;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.text, this.color, this.formatting);
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        if (hashCode() != obj.hashCode()) {
            return false;
        }
        TextComponent their = (TextComponent) obj;
        return this.text.equals(their.getMessage()) && this.color.equals(their.getColor()) && this.formatting.equals(their.getFormatting());
    }

    @Override
    public String toString() {
        return getANSIColoredMessage();
    }

    @Override
    public String getANSIColoredMessage() {
        StringBuilder builder = new StringBuilder();
        if (this.color != null) {
            builder.append(ChatColors.getANSIColorByRGBColor(this.color));
        }
        if (this.formatting != null) {
            this.formatting.forEach((chatFormattingCodes -> {
                if (chatFormattingCodes instanceof PreChatFormattingCodes code) {
                    builder.append(code.getANSI());
                }
            }));
        }
        builder.append(this.text);
        if (this.formatting != null) {
            this.formatting.forEach((chatFormattingCodes -> {
                if (chatFormattingCodes instanceof PostChatFormattingCodes code) {
                    builder.append(code.getANSI());
                }
            }));
        }
        builder.append(PostChatFormattingCodes.RESET);
        return builder.toString();
    }

    @Override
    public String getLegacyText() {
        StringBuilder output = new StringBuilder();
        Integer colorChar = ChatColors.getColorId(this.color);
        if (colorChar != null) {
            output.append(ProtocolDefinition.TEXT_COMPONENT_SPECIAL_PREFIX_CHAR).append(Integer.toHexString(colorChar));
        }
        this.formatting.forEach((chatFormattingCode -> output.append(ProtocolDefinition.TEXT_COMPONENT_SPECIAL_PREFIX_CHAR).append(chatFormattingCode.getChar())));
        output.append(this.text);
        output.append(ProtocolDefinition.TEXT_COMPONENT_SPECIAL_PREFIX_CHAR).append(PostChatFormattingCodes.RESET.getChar());
        return output.toString();
    }

    @Override
    public String getMessage() {
        return this.text;
    }

    @Override
    public ObservableList<Node> getJavaFXText(ObservableList<Node> nodes) {
        Text text = new Text(this.text);
        text.setFill(Color.WHITE);
        if (Minosoft.getConfig().getBoolean(ConfigurationPaths.BooleanPaths.CHAT_COLORED) && this.color != null) {
            text.setFill(Color.web(this.color.toString()));
        }
        this.formatting.forEach((chatFormattingCode -> {
            if (chatFormattingCode instanceof PreChatFormattingCodes code) {
                switch (code) {
                    case OBFUSCATED -> {
                        // ToDo: potential memory leak: Stop timeline, when TextComponent isn't shown anymore
                        Timeline obfuscatedTimeline;
                        if (Minosoft.getConfig().getBoolean(ConfigurationPaths.BooleanPaths.CHAT_OBFUSCATED)) {
                            obfuscatedTimeline = new Timeline(new KeyFrame(Duration.millis(50), e -> {
                                char[] chars = text.getText().toCharArray();
                                for (int i = 0; i < chars.length; i++) {
                                    chars[i] = Util.getRandomChar(ProtocolDefinition.OBFUSCATED_CHARS);
                                }
                                text.setText(new String(chars));
                            }));
                        } else {
                            obfuscatedTimeline = new Timeline(new KeyFrame(Duration.millis(500), e -> text.setVisible(false)), new KeyFrame(Duration.millis(1000), e -> text.setVisible(true)));
                        }
                        obfuscatedTimeline.setCycleCount(Animation.INDEFINITE);
                        obfuscatedTimeline.play();
                        text.getStyleClass().add("obfuscated");
                    }
                    case BOLD -> text.setStyle("-fx-font-weight: bold;");
                    case STRIKETHROUGH -> text.setStyle("-fx-strikethrough: true;");
                    case UNDERLINED -> text.setStyle("-fx-underline: true;");
                    case ITALIC -> text.setStyle("-fx-font-weight: italic;");
                }
            }
        }));
        nodes.add(text);
        return nodes;
    }
}
