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

public enum StatusEffects {
    SPEED("speed", 1, Impact.POSITIVE),
    SLOWNESS("slowness", 2, Impact.NEGATIVE),
    HASTE("haste", 3, Impact.POSITIVE),
    MINING_FATIGUE("mining_fatigue", 4, Impact.NEGATIVE),
    STRENGTH("strength", 5, Impact.POSITIVE),
    INSTANT_HEALTH("instant_health", 6, Impact.POSITIVE),
    INSTANT_DAMAGE("instant_damage", 7, Impact.POSITIVE),
    JUMP_BOOST("jump_boost", 8, Impact.POSITIVE),
    NAUSEA("nausea", 9, Impact.NEGATIVE),
    REGENERATION("regeneration", 10, Impact.POSITIVE),
    RESISTANCE("resistance", 11, Impact.POSITIVE),
    FIRE_RESISTANCE("fire_resistance", 12, Impact.POSITIVE),
    WATER_BREATHING("water_breathing", 13, Impact.POSITIVE),
    INVISIBILITY("invisibility", 14, Impact.POSITIVE),
    BLINDNESS("blindness", 15, Impact.NEGATIVE),
    NIGHT_VISION("night_vision", 16, Impact.POSITIVE),
    HUNGER("hunger", 17, Impact.NEGATIVE),
    WEAKNESS("weakness", 18, Impact.NEGATIVE),
    POISON("poison", 19, Impact.NEGATIVE),
    WITHER("wither", 20, Impact.NEGATIVE),
    HEALTH_BOOST("health_boost", 21, Impact.POSITIVE),
    ABSORPTION("absorption", 22, Impact.POSITIVE),
    SATURATION("saturation", 23, Impact.POSITIVE),
    GLOWING("glowing", 24, Impact.NEGATIVE),
    LEVITATION("levitation", 25, Impact.NEGATIVE),
    LUCK("luck", 26, Impact.POSITIVE),
    UNLUCK("unluck", 27, Impact.NEGATIVE),
    SLOW_FALLING("slow_falling", 28, Impact.POSITIVE),
    CONDUIT_POWER("conduit_power", 29, Impact.POSITIVE),
    DOLPHINS_GRACE("dolphins_grace", 30, Impact.POSITIVE),
    BAD_OMEN("bad_omen", 31, Impact.NEGATIVE),
    HERO_OF_THE_VILLAGE("hero_of_the_village", 32, Impact.POSITIVE);

    final String name;
    final int id;
    final Impact impact;

    StatusEffects(String name, int id, Impact impact) {
        this.name = name;
        this.id = id;
        this.impact = impact;
    }

    public static StatusEffects byId(int id) {
        for (StatusEffects s : values()) {
            if (s.getId() == id) {
                return s;
            }
        }
        return null;
    }

    public static StatusEffects byName(String name) {
        for (StatusEffects s : values()) {
            if (s.getName().equals(name)) {
                return s;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public enum Impact {
        POSITIVE,
        NEGATIVE
    }
}
