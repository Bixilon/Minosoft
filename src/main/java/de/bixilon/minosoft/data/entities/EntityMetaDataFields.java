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

public enum EntityMetaDataFields {
    ENTITY_FLAGS((byte) 0),
    ENTITY_AIR_SUPPLY(300),
    ENTITY_CUSTOM_NAME,
    ENTITY_CUSTOM_NAME_VISIBLE(false),
    ENTITY_SILENT(false),
    ENTITY_NO_GRAVITY(false),
    ENTITY_POSE(Poses.STANDING),
    ENTITY_TICKS_FROZEN(0),

    LIVING_ENTITY_FLAGS(0),
    LIVING_ENTITY_HEALTH(1.0F),
    LIVING_ENTITY_EFFECT_COLOR(0),
    LIVING_ENTITY_EFFECT_AMBIENCE(false),
    LIVING_ENTITY_ARROW_COUNT(0),
    LIVING_ENTITY_ABSORPTION_HEARTS(0),
    LIVING_ENTITY_BED_POSITION,

    MOB_FLAGS(0),

    ZOMBIE_IS_BABY(false),
    ZOMBIE_SPECIAL_TYPE(0),
    ZOMBIE_DROWNING_CONVERSION(false);

    final Object defaultValue;

    EntityMetaDataFields() {
        defaultValue = null;
    }

    EntityMetaDataFields(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public <K> K getDefaultValue() {
        return (K) defaultValue;
    }
}
