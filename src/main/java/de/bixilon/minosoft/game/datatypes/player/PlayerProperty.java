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

package de.bixilon.minosoft.game.datatypes.player;

public class PlayerProperty {
    final PlayerProperties property;
    final String value;
    final String signature;

    public PlayerProperty(PlayerProperties property, String value, String signature) {
        this.property = property;
        this.value = value;
        this.signature = signature;
    }

    public PlayerProperty(PlayerProperties property, String value) {
        this.property = property;
        this.value = value;
        this.signature = null;
    }

    public PlayerProperties getProperty() {
        return property;
    }

    public String getValue() {
        return value;
    }

    public boolean isSigned() {
        // ToDo check signature
        return signature != null;
    }

    public String getSignature() {
        return signature;
    }
}
