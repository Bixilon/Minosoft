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

package de.bixilon.minosoft.game.datatypes.particle;

import de.bixilon.minosoft.game.datatypes.Identifier;

public enum Particles {
    AMBIENT_ENTITY_EFFECT(new Identifier("ambient_entity_effect"), 0),
    ANGRY_VILLAGER(new Identifier("angry_villager"), 1),
    BARRIER(new Identifier("barrier"), 2),
    BLOCK(new Identifier("block"), 3, BlockParticle.class);
    // ToDo other particles

    final Identifier identifier;
    final int id;
    final Class<? extends Particle> clazz;

    Particles(Identifier identifier, int id, Class<? extends Particle> clazz) {
        this.identifier = identifier;
        this.id = id;
        this.clazz = clazz;
    }

    Particles(Identifier identifier, int id) {
        this.identifier = identifier;
        this.id = id;
        this.clazz = OtherParticles.class;
    }

    public static Particles byIdentifier(Identifier identifier) {
        for (Particles b : values()) {
            if (b.getIdentifier().equals(identifier)) {
                return b;
            }
        }
        return null;
    }

    public static Particles byType(int type) {
        for (Particles b : values()) {
            if (b.getId() == type) {
                return b;
            }
        }
        return null;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public int getId() {
        return id;
    }

    public Class<? extends Particle> getClazz() {
        return clazz;
    }
}
