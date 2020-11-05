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

import com.google.common.collect.HashBiMap;

public enum EntityPropertyKeys {
    MAX_HEALTH("generic.maxHealth"),
    FOLLOW_RANGE("generic.followRange"),
    KNOCKBACK_RESISTANCE("generic.knockbackResistance"),
    MOVEMENT_SPEED("generic.movementSpeed"),
    ATTACK_DAMAGE("generic.attackDamage"),
    HORSE_JUMP_STRENGTH("horse.jumpStrength"),
    ZOMBIE_SPAWN_REINFORCEMENT("zombie.spawnReinforcements");

    final static HashBiMap<String, EntityPropertyKeys> keys = HashBiMap.create();

    static {
        for (EntityPropertyKeys key : values()) {
            keys.put(key.getIdentifier(), key);
        }
    }

    final String identifier;

    EntityPropertyKeys(String identifier) {
        this.identifier = identifier;
    }

    public static EntityPropertyKeys byName(String name) {
        return keys.get(name);
    }

    public String getIdentifier() {
        return identifier;
    }
}
