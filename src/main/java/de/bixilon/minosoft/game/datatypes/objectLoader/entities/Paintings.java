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

package de.bixilon.minosoft.game.datatypes.objectLoader.entities;

import de.bixilon.minosoft.game.datatypes.ChangeableIdentifier;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public enum Paintings {
    KEBAB(new ChangeableIdentifier("kebab"), 0),
    AZTEC(new ChangeableIdentifier("aztec"), 1),
    ALBAN(new ChangeableIdentifier("alban"), 2),
    AZTEC2(new ChangeableIdentifier("aztec2"), 3),
    BOMB(new ChangeableIdentifier("bomb"), 4),
    PLANT(new ChangeableIdentifier("plant"), 5),
    WASTELAND(new ChangeableIdentifier("wasteland"), 6),
    POOL(new ChangeableIdentifier("pool"), 7),
    COURBET(new ChangeableIdentifier("courbet"), 8),
    SEA(new ChangeableIdentifier("sea"), 9),
    SUNSET(new ChangeableIdentifier("sunset"), 10),
    CREEBET(new ChangeableIdentifier("creebet"), 11),
    WANDERER(new ChangeableIdentifier("wanderer"), 12),
    GRAHAM(new ChangeableIdentifier("graham"), 13),
    MATCH(new ChangeableIdentifier("match"), 14),
    BUST(new ChangeableIdentifier("bust"), 15),
    STAGE(new ChangeableIdentifier("stage"), 16),
    VOID(new ChangeableIdentifier("void"), 17),
    SKULL_AND_ROSES(new ChangeableIdentifier("skull_and_roses"), 18),
    WITHER(new ChangeableIdentifier("wither"), 19),
    FIGHTERS(new ChangeableIdentifier("fighters"), 20),
    POINTER(new ChangeableIdentifier("pointer"), 21),
    PIG_SCENE(new ChangeableIdentifier("pigscene"), 22),
    BURNING_SKULL(new ChangeableIdentifier("burning_skull"), 23),
    SKELETON(new ChangeableIdentifier("skeleton"), 24),
    DONKEY_KONG(new ChangeableIdentifier("donkey_kong"), 25);

    final ChangeableIdentifier changeableIdentifier;
    final int id;

    Paintings(ChangeableIdentifier changeableIdentifier, int id) {
        this.changeableIdentifier = changeableIdentifier;
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
            if (painting.getChangeableIdentifier().isValidName(name, version)) {
                return painting;
            }
        }
        return null;
    }


    public int getId() {
        return id;
    }

    public ChangeableIdentifier getChangeableIdentifier() {
        return changeableIdentifier;
    }
}
