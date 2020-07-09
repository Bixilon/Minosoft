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

public enum EntityPropertyKeys {
    MAX_HEALTH(new Identifier("generic.maxHealth")),
    FOLLOW_RANGE(new Identifier("generic.followRange")),
    KNOCKBACK_RESISTANCE(new Identifier("generic.knockbackResistance")),
    MOVEMENT_SPEED(new Identifier("generic.movementSpeed")),
    ATTACK_DAMAGE(new Identifier("generic.attackDamage")),
    HORSE_JUMP_STRENGTH(new Identifier("horse.jumpStrength")),
    ZOMBIE_SPAWN_REINFORCEMENT(new Identifier("zombie.spawnReinforcements"));

    final Identifier identifier;

    EntityPropertyKeys(Identifier identifier) {
        this.identifier = identifier;
    }

    public static EntityPropertyKeys byName(String name, ProtocolVersion version) {
        for (EntityPropertyKeys propertyKey : values()) {
            if (propertyKey.getIdentifier().isValidName(name, version)) {
                return propertyKey;
            }
        }
        return null;
    }

    public Identifier getIdentifier() {
        return identifier;
    }
}
