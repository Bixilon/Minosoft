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

package de.bixilon.minosoft.game.datatypes.objectLoader.items;

public class Item {
    final String mod;
    final String identifier;

    public Item(String fullIdentifier) {
        String[] split = fullIdentifier.split(":");
        this.mod = split[0];
        this.identifier = split[1];
    }

    public Item(String mod, String identifier) {
        this.mod = mod;
        this.identifier = identifier;
    }

    public String getMod() {
        return mod;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String toString() {
        return String.format("%s:%s", getMod(), getIdentifier());
    }

    @Override
    public int hashCode() {
        return mod.hashCode() * identifier.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        if (hashCode() != obj.hashCode()) {
            return false;
        }
        Item their = (Item) obj;
        return getIdentifier().equals(their.getIdentifier()) && getMod().equals(their.getMod());
    }
}
