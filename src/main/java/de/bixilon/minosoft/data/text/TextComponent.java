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
        formatting.addOrRemove(PreChatFormattingCodes.OBFUSCATED, obfuscated);
        return this;
    }

    public TextComponent setBold(boolean bold) {
        formatting.addOrRemove(PreChatFormattingCodes.BOLD, bold);
        return this;
    }

    public TextComponent setStrikethrough(boolean strikethrough) {
        formatting.addOrRemove(PreChatFormattingCodes.STRIKETHROUGH, strikethrough);
        return this;
    }

    public TextComponent setUnderlined(boolean underlined) {
        formatting.addOrRemove(PreChatFormattingCodes.UNDERLINED, underlined);
        return this;
    }

    public TextComponent setItalic(boolean italic) {
        formatting.addOrRemove(PreChatFormattingCodes.ITALIC, italic);
        return this;
    }

    public TextComponent setReset(boolean reset) {
        formatting.addOrRemove(PostChatFormattingCodes.RESET, reset);
        return this;
    }

    public RGBColor getColor() {
        return color;
    }

    public TextComponent setColor(RGBColor color) {
        this.color = color;
        return this;
    }

    public BetterHashSet<ChatFormattingCode> getFormatting() {
        return formatting;
    }

    public TextComponent setFormatting(BetterHashSet<ChatFormattingCode> formatting) {
        this.formatting = formatting;
        return this;
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
        return text.equals(their.getMessage()) && color.equals(their.getColor()) && formatting.equals(their.getFormatting());
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, color, formatting);
    }

    @Override
    public String getANSIColoredMessage() {
        StringBuilder builder = new StringBuilder();
        if (color != null) {
            builder.append(ChatColors.getANSIColorByRGBColor(color));
        }
        if (formatting != null) {
            formatting.forEach((chatFormattingCodes -> {
                if (chatFormattingCodes instanceof PreChatFormattingCodes code) {
                    builder.append(code.getANSI());
                }
            }));
        }
        builder.append(text);
        if (formatting != null) {
            formatting.forEach((chatFormattingCodes -> {
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
        Integer colorChar = ChatColors.getColorId(color);
        if (colorChar != null) {
            output.append('§').append(Integer.toHexString(colorChar));
        }
        formatting.forEach((chatFormattingCode -> output.append('§').append(chatFormattingCode.getChar())));
        output.append(text);
        output.append('§').append(PostChatFormattingCodes.RESET.getChar());
        return output.toString();
    }

    @Override
    public String getMessage() {
        return text;
    }

    @Override
    public ObservableList<Node> getJavaFXText(ObservableList<Node> nodes) {
        Text text = new Text(this.text);
        if (color != null) {
            text.setFill(Color.web(color.toString()));
        }
        formatting.forEach((chatFormattingCode -> {
            if (chatFormattingCode instanceof PreChatFormattingCodes code) {
                switch (code) {
                    case OBFUSCATED -> {
                        Timeline flasher = new Timeline(new KeyFrame(Duration.seconds(1), e -> text.setVisible(false)), new KeyFrame(Duration.seconds(2), e -> text.setVisible(true)));
                        flasher.setCycleCount(Animation.INDEFINITE);
                        flasher.play();
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

    @Override
    public String toString() {
        return getANSIColoredMessage();
    }
}
