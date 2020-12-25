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

import com.google.common.collect.HashBiMap;
import org.checkerframework.common.value.qual.IntRange;

public final class ChatColors {
    public static final RGBColor BLACK = new RGBColor(0, 0, 0);
    public static final RGBColor DARK_BLUE = new RGBColor(0, 0, 170);
    public static final RGBColor DARK_GREEN = new RGBColor(0, 170, 0);
    public static final RGBColor DARK_AQUA = new RGBColor(0, 170, 170);
    public static final RGBColor DARK_RED = new RGBColor(170, 0, 0);
    public static final RGBColor DARK_PURPLE = new RGBColor(170, 0, 170);
    public static final RGBColor GOLD = new RGBColor(255, 170, 0);
    public static final RGBColor GRAY = new RGBColor(170, 170, 170);
    public static final RGBColor DARK_GRAY = new RGBColor(85, 85, 85);
    public static final RGBColor BLUE = new RGBColor(85, 85, 255);
    public static final RGBColor GREEN = new RGBColor(85, 255, 85);
    public static final RGBColor AQUA = new RGBColor(85, 255, 255);
    public static final RGBColor RED = new RGBColor(255, 85, 85);
    public static final RGBColor LIGHT_PURPLE = new RGBColor(255, 85, 255);
    public static final RGBColor YELLOW = new RGBColor(255, 255, 85);
    public static final RGBColor WHITE = new RGBColor(255, 255, 255);

    private static final HashBiMap<RGBColor, Integer> COLOR_ID_MAP = HashBiMap.create();
    private static final RGBColor[] COLORS = {BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE};

    static {
        for (int i = 0; i < COLORS.length; i++) {
            COLOR_ID_MAP.put(COLORS[i], i);
        }
    }

    public static String getANSIColorByFormattingChar(char c) {
        return getANSIColorByRGBColor(getColorByFormattingChar(c));
    }

    public static String getANSIColorByRGBColor(RGBColor color) {
        return String.format("\033[38;2;%d;%d;%dm", color.getRed(), color.getGreen(), color.getBlue());
    }

    public static RGBColor getColorByFormattingChar(char c) {
        return getColorById(Character.digit(c, 16));
    }

    public static ChatCode getFormattingById(@IntRange(from = 0, to = 21) int id) {
        if (id <= 15) {
            return getColorById(id);
        }
        return switch (id) {
            case 16 -> PreChatFormattingCodes.OBFUSCATED;
            case 17 -> PreChatFormattingCodes.BOLD;
            case 18 -> PreChatFormattingCodes.STRIKETHROUGH;
            case 19 -> PreChatFormattingCodes.UNDERLINED;
            case 20 -> PreChatFormattingCodes.ITALIC;
            case 21 -> PostChatFormattingCodes.RESET;
            default -> null;
        };
    }

    public static RGBColor getColorById(@IntRange(from = 0, to = 15) int id) {
        if (id < 0) {
            return null;
        }
        if (id <= 15) {
            return COLORS[id];
        }
        return null;
    }

    public static Integer getColorId(RGBColor color) {
        return COLOR_ID_MAP.get(color);
    }

    public static String getColorChar(RGBColor color) {
        return String.format("%x", COLOR_ID_MAP.get(color));
    }

    public static RGBColor getColorByName(String name) {
        return (RGBColor) getChatFormattingByName(name);
    }

    public static ChatCode getChatFormattingByName(String name) {
        return switch (name.toLowerCase()) {
            case "black" -> BLACK;
            case "dark_blue" -> DARK_BLUE;
            case "dark_green" -> DARK_GREEN;
            case "dark_aqua" -> DARK_AQUA;
            case "dark_red" -> DARK_RED;
            case "dark_purple" -> DARK_PURPLE;
            case "gold" -> GOLD;
            case "gray", "grey" -> GRAY;
            case "dark_gray", "dark_grey" -> DARK_GRAY;
            case "blue" -> BLUE;
            case "green" -> GREEN;
            case "aqua" -> AQUA;
            case "red" -> RED;
            case "light_purple" -> LIGHT_PURPLE;
            case "yellow" -> YELLOW;
            case "white", "reset" -> WHITE;
            case "bold" -> PreChatFormattingCodes.BOLD;
            case "italic" -> PreChatFormattingCodes.ITALIC;
            case "underlined" -> PreChatFormattingCodes.UNDERLINED;
            case "strikethrough" -> PreChatFormattingCodes.STRIKETHROUGH;
            case "obfuscated" -> PreChatFormattingCodes.OBFUSCATED;
            default -> throw new IllegalArgumentException("Unexpected value: " + name);
        };
    }
}
