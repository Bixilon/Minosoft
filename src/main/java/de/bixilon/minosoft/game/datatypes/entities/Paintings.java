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
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public enum Paintings {
    KEBAB(new Identifier("kebab"), 0),
    AZTEC(new Identifier("aztec"), 1),
    ALBAN(new Identifier("alban"), 2),
    AZTEC2(new Identifier("aztec2"), 3),
    BOMB(new Identifier("bomb"), 4),
    PLANT(new Identifier("plant"), 5),
    WASTELAND(new Identifier("wasteland"), 6),
    POOL(new Identifier("pool"), 7),
    COURBET(new Identifier("courbet"), 8),
    SEA(new Identifier("sea"), 9),
    SUNSET(new Identifier("sunset"), 10),
    CREEBET(new Identifier("creebet"), 11),
    WANDERER(new Identifier("wanderer"), 12),
    GRAHAM(new Identifier("graham"), 13),
    MATCH(new Identifier("match"), 14),
    BUST(new Identifier("bust"), 15),
    STAGE(new Identifier("stage"), 16),
    VOID(new Identifier("void"), 17),
    SKULL_AND_ROSES(new Identifier("skull_and_roses"), 18),
    WITHER(new Identifier("wither"), 19),
    FIGHTERS(new Identifier("fighters"), 20),
    POINTER(new Identifier("pointer"), 21),
    PIG_SCENE(new Identifier("pigscene"), 22),
    BURNING_SKULL(new Identifier("burning_skull"), 23),
    SKELETON(new Identifier("skeleton"), 24),
    DONKEY_KONG(new Identifier("donkey_kong"), 25);

    final Identifier identifier;
    final int id;

    Paintings(Identifier identifier, int id) {
        this.identifier = identifier;
        this.id = id;
    }


    public static Paintings byId(int type) {
        for (Paintings painting : values()) {
            if (painting.getId() == type) {
                return painting;
            }
        }
        return null;
    }

    public static Paintings byName(String name, ProtocolVersion version) {
        for (Paintings painting : values()) {
            if (painting.getIdentifier().isValidName(name, version)) {
                return painting;
            }
        }
        return null;
    }


    public int getId() {
        return id;
    }

    public Identifier getIdentifier() {
        return identifier;
    }
}
