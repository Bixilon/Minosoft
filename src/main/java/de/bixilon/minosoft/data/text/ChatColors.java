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

package de.bixilon.minosoft.data.text;

import com.google.common.collect.HashBiMap;

public final class ChatColors {
    private static final HashBiMap<Integer, RGBColor> colors = HashBiMap.create();

    static {
        colors.put(0, new RGBColor(0, 0, 0));
        colors.put(1, new RGBColor(0, 0, 170));
        colors.put(2, new RGBColor(0, 170, 0));
        colors.put(3, new RGBColor(0, 170, 170));
        colors.put(4, new RGBColor(170, 0, 0));
        colors.put(5, new RGBColor(170, 0, 170));
        colors.put(6, new RGBColor(255, 170, 0));
        colors.put(7, new RGBColor(170, 170, 170));
        colors.put(8, new RGBColor(85, 85, 85));
        colors.put(9, new RGBColor(85, 85, 255));
        colors.put(10, new RGBColor(85, 255, 85));
        colors.put(11, new RGBColor(85, 255, 255));
        colors.put(12, new RGBColor(255, 85, 85));
        colors.put(13, new RGBColor(255, 85, 255));
        colors.put(14, new RGBColor(255, 255, 85));
        colors.put(15, new RGBColor(255, 255, 255));
    }

    public static String getANSIColorByFormattingChar(char c) {
        return getANSIColorByRGBColor(getColorByFormattingChar(c));
    }

    public static String getANSIColorByRGBColor(RGBColor color) {
        return String.format("\033[38;2;%d;%d;%dm", color.getRed(), color.getGreen(), color.getBlue());
    }

    public static RGBColor getColorByFormattingChar(char c) {
        return colors.get(Character.digit(c, 16));
    }

    public static RGBColor getColorById(int id) {
        return colors.get(id);
    }

    public static Integer getColorId(RGBColor color) {
        return colors.inverse().get(color);
    }

    public static RGBColor getColorByName(String name) {
        return colors.get(switch (name.toLowerCase()) {
            case "black" -> 0;
            case "dark_blue" -> 1;
            case "dark_green" -> 2;
            case "dark_aqua" -> 3;
            case "dark_red" -> 4;
            case "dark_purple" -> 5;
            case "gold" -> 6;
            case "gray", "grey" -> 7;
            case "dark_gray", "dark_grey" -> 8;
            case "blue" -> 9;
            case "green" -> 10;
            case "aqua" -> 11;
            case "red" -> 12;
            case "light_purple" -> 13;
            case "yellow" -> 14;
            case "white" -> 15;
            default -> throw new IllegalStateException("Unexpected value: " + name);
        });
    }
}
