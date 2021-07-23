/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.entities.entities.monster.raid

import de.bixilon.minosoft.data.entities.EntityMetaDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import glm_.vec3.Vec3d

abstract class SpellcasterIllager(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation) : AbstractIllager(connection, entityType, position, rotation) {

    @get:EntityMetaDataFunction(name = "Spell")
    val spell: Spells
        get() = Spells.byId(entityMetaData.sets.getInt(EntityMetaDataFields.SPELLCASTER_ILLAGER_SPELL))

    enum class Spells {
        NONE,
        SUMMON_VEX,
        ATTACK,
        WOLOLO,
        DISAPPEAR,
        BLINDNESS,
        ;

        companion object {
            private val SPELLS = values()
            fun byId(id: Int): Spells {
                return SPELLS[id]
            }
        }
    }
}
