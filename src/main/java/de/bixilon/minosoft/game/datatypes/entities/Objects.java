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

package de.bixilon.minosoft.game.datatypes.entities;

import de.bixilon.minosoft.game.datatypes.Identifier;
import de.bixilon.minosoft.game.datatypes.entities.objects.Boat;

public enum Objects implements EntityEnumInterface {
    BOAT(new Identifier("boat"), 1, Boat.class);

    final Identifier identifier;
    final int type;
    final Class<? extends EntityObject> clazz;

    Objects(Identifier identifier, int type, Class<? extends EntityObject> clazz) {
        this.identifier = identifier;
        this.type = type;
        this.clazz = clazz;
    }

    public static Objects byIdentifier(Identifier identifier) {
        for (Objects b : values()) {
            if (b.getIdentifier().equals(identifier)) {
                return b;
            }
        }
        return null;
    }

    public static Objects byType(int type) {
        for (Objects b : values()) {
            if (b.getType() == type) {
                return b;
            }
        }
        return null;
    }

    @Override
    public Identifier getIdentifier() {
        return identifier;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public Class<? extends EntityObject> getClazz() {
        return clazz;
    }
}
