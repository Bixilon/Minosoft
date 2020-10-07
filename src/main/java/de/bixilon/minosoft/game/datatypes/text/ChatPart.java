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

import java.util.HashSet;
import java.util.Objects;

public class ChatPart implements BaseComponent {
    private final String text;
    private RGBColor color;
    private HashSet<ChatFormattingCodes> formatting;

    public ChatPart(String text, RGBColor color, HashSet<ChatFormattingCodes> formatting) {
        this.text = text;
        this.color = color;
        this.formatting = formatting;
    }

    public ChatPart(String text, RGBColor color) {
        this.text = text;
        this.color = color;
    }

    public ChatPart(String text) {
        this.text = text;
    }

    public ChatPart setObfuscated(boolean obfuscated) {
        if (obfuscated) {
            formatting.add(ChatFormattingCodes.OBFUSCATED);
        } else {
            formatting.remove(ChatFormattingCodes.OBFUSCATED);
        }
        return this;
    }

    public ChatPart setBold(boolean bold) {
        if (bold) {
            formatting.add(ChatFormattingCodes.BOLD);
        } else {
            formatting.remove(ChatFormattingCodes.BOLD);
        }
        return this;
    }

    public ChatPart setStrikethrough(boolean strikethrough) {
        if (strikethrough) {
            formatting.add(ChatFormattingCodes.STRIKETHROUGH);
        } else {
            formatting.remove(ChatFormattingCodes.STRIKETHROUGH);
        }
        return this;
    }

    public ChatPart setUnderlined(boolean underlined) {
        if (underlined) {
            formatting.add(ChatFormattingCodes.UNDERLINED);
        } else {
            formatting.remove(ChatFormattingCodes.UNDERLINED);
        }
        return this;
    }

    public ChatPart setItalic(boolean italic) {
        if (italic) {
            formatting.add(ChatFormattingCodes.ITALIC);
        } else {
            formatting.remove(ChatFormattingCodes.ITALIC);
        }
        return this;
    }

    public ChatPart setReset(boolean reset) {
        if (reset) {
            formatting.add(ChatFormattingCodes.RESET);
        } else {
            formatting.remove(ChatFormattingCodes.RESET);
        }
        return this;
    }

    public RGBColor getColor() {
        return color;
    }


    public ChatPart setColor(RGBColor color) {
        this.color = color;
        return this;
    }


    public HashSet<ChatFormattingCodes> getFormatting() {
        return formatting;
    }


    public ChatPart setFormatting(HashSet<ChatFormattingCodes> formatting) {
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
        ChatPart their = (ChatPart) obj;
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
                if (chatFormattingCodes.getPosition() == ChatFormattingCodes.ChatFormattingCodePosition.PRE) {
                    builder.append(chatFormattingCodes.getANSI());
                }
            }));
        }
        builder.append(text);
        if (formatting != null) {
            formatting.forEach((chatFormattingCodes -> {
                if (chatFormattingCodes.getPosition() == ChatFormattingCodes.ChatFormattingCodePosition.POST) {
                    builder.append(chatFormattingCodes.getANSI());
                }
            }));
        }
        builder.append(ChatFormattingCodes.RESET);
        return builder.toString();
    }

    @Override
    public String getLegacyText() {
        return getMessage(); //ToDo
    }

    @Override
    public String getMessage() {
        return text;
    }

    @Override
    public String toString() {
        return getANSIColoredMessage();
    }
}
