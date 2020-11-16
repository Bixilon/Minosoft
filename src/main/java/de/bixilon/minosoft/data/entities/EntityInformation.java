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

package de.bixilon.minosoft.data.entities;

public class EntityInformation {
    private final String mod;
    private final String identifier;

    private final int length;
    private final int width;
    private final int height;

    public EntityInformation(String mod, String identifier, int length, int width, int height) {
        this.mod = mod;
        this.identifier = identifier;
        this.length = length;
        this.width = width;
        this.height = height;
    }

    public EntityInformation(String mod, String identifier, int width, int height) {
        this.mod = mod;
        this.identifier = identifier;
        this.width = this.length = width;
        this.height = height;
    }

    public String getMod() {
        return mod;
    }

    public String getIdentifier() {
        return identifier;
    }

    public int getLength() {
        return length;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
