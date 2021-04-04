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

package de.bixilon.minosoft.data.entities.entities.monster.raid;

import de.bixilon.minosoft.data.entities.EntityMetaDataFields;
import de.bixilon.minosoft.data.entities.EntityRotation;
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction;
import de.bixilon.minosoft.data.mappings.entities.EntityType;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import glm_.vec3.Vec3;

public class SpellcasterIllager extends AbstractIllager {

    public SpellcasterIllager(PlayConnection connection, EntityType entityType, Vec3 position, EntityRotation rotation) {
        super(connection, entityType, position, rotation);
    }

    @EntityMetaDataFunction(name = "Spell")
    public Spells getSpell() {
        return Spells.byId(getEntityMetaData().getSets().getInt(EntityMetaDataFields.SPELLCASTER_ILLAGER_SPELL));
    }

    public enum Spells {
        NONE,
        SUMMON_VEX,
        ATTACK,
        WOLOLO,
        DISAPPEAR,
        BLINDNESS;

        private static final Spells[] SPELLS = values();

        public static Spells byId(int id) {
            return SPELLS[id];
        }
    }
}
