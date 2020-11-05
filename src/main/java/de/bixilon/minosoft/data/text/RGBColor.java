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

public final class RGBColor {
    private final int color;

    public RGBColor(int color) {
        this.color = color;
    }

    public RGBColor(int red, int green, int blue) {
        this.color = blue | (green << 8) | (red << 16);
    }

    public RGBColor(String colorString) {
        if (colorString.startsWith("#")) {
            colorString = colorString.substring(1);
        }
        color = Integer.parseInt(colorString, 16);
    }

    public int getRed() {
        return (color >> 16) & 0xFF;
    }

    public int getGreen() {
        return (color >> 8) & 0xFF;
    }

    public int getBlue() {
        return color & 0xFF;
    }

    @Override
    public int hashCode() {
        return color;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        RGBColor their = (RGBColor) obj;
        return getColor() == their.getColor();
    }

    public int getColor() {
        return color;
    }

    @Override
    public String toString() {
        return String.format("#%06X", (0xFFFFFF & color));
    }
}
