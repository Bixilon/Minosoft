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

package de.bixilon.minosoft.game.datatypes;

public enum Enchantments {
    PROTECTION(0),
    FIRE_PROTECTION(1),
    FEATHER_FALLING(2),
    BLAST_PROTECTION(3),
    PROJECTILE_PROTECTION(4),
    RESPIRATION(5),
    AQUA_AFFINITY(6),
    THORNS(7),
    DEPTH_STRIDER(8),
    FROST_WALKER(9),
    BINDING_CURSE(10),
    SHARPNESS(11),
    SMITE(12),
    BANE_OF_ARTHROPODS(13),
    KNOCKBACK(14),
    FIRE_ASPECT(15),
    LOOTING(16),
    SWEEPING(17),
    EFFICIENCY(18),
    SILK_TOUCH(19),
    UNBREAKING(20),
    FORTUNE(21),
    POWER(22),
    PUNCH(23),
    FLAME(24),
    INFINITY(25),
    LUCK_OF_THE_SEA(26),
    LURE(27),
    LOYALTY(28),
    IMPALING(29),
    RIPTIDE(30),
    CHANNELING(31),
    MENDING(32),
    VANISHING_CURSE(33);

    final int id;

    Enchantments(int id) {
        this.id = id;
    }

    public static Enchantments byId(int id) {
        for (Enchantments enchantment : values()) {
            if (enchantment.getId() == id) {
                return enchantment;
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }
}
