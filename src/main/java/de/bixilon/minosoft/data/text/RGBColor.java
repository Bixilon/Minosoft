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

public final class RGBColor implements ChatCode {
    private final int color;

    public RGBColor(int red, int green, int blue, int alpha) {
        this.color = blue | (green << 8) | (red << 16) | (alpha << 24);
    }

    public RGBColor(int red, int green, int blue) {
        this.color = blue | (green << 8) | (red << 16);
    }

    public RGBColor(int color) {
        this.color = color;
    }

    public RGBColor(String colorString) {
        if (colorString.startsWith("#")) {
            colorString = colorString.substring(1);
        }
        this.color = Integer.parseInt(colorString, 16);
    }

    public int getAlpha() {
        return (this.color >> 24) & 0xFF;
    }

    public int getRed() {
        return (this.color >> 16) & 0xFF;
    }

    public int getGreen() {
        return (this.color >> 8) & 0xFF;
    }

    public int getBlue() {
        return this.color & 0xFF;
    }

    @Override
    public int hashCode() {
        return this.color;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        RGBColor their = (RGBColor) obj;
        return getColor() == their.getColor();
    }

    @Override
    public String toString() {
        if (getAlpha() > 0) {
            return String.format("#%08X", this.color);
        }
        return String.format("#%06X", (0xFFFFFF & this.color));
    }

    public int getColor() {
        return this.color;
    }
}
