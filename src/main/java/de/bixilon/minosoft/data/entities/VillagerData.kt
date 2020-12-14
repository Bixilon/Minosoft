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
package de.bixilon.minosoft.data.entities

import de.bixilon.minosoft.data.MapSet
import de.bixilon.minosoft.data.VersionValueMap

data class VillagerData(val type: VillagerTypes, val profession: VillagerProfessions, val level: VillagerLevels) {
    constructor(type: Int, profession: Int, level: Int, versionId: Int) : this(VillagerTypes.values()[type], VillagerProfessions.byId(profession, versionId)!!, VillagerLevels.values()[level])

    enum class VillagerProfessions(val values: Array<MapSet<Int, Int>>) {
        NONE(arrayOf(MapSet(451, 0))),
        ARMORER(arrayOf(MapSet(451, 1))),
        BUTCHER(arrayOf(MapSet(0, 4), MapSet(451, 2))),
        CARTOGRAPHER(arrayOf(MapSet(451, 3))),
        CLERIC(arrayOf(MapSet(451, 4))),
        FARMER(arrayOf(MapSet(0, 0), MapSet(451, 5))),
        FISHERMAN(arrayOf(MapSet(451, 6))),
        FLETCHER(arrayOf(MapSet(451, 7))),
        LEATHER_WORKER(arrayOf(MapSet(451, 8))),
        LIBRARIAN(arrayOf(MapSet(0, 1), MapSet(451, 9))),
        MASON(arrayOf(MapSet(451, 10))),
        NITWIT(arrayOf(MapSet(0, 5), MapSet(451, 11))),
        SHEPHERD(arrayOf(MapSet(451, 12))),
        TOOL_SMITH(arrayOf(MapSet(451, 13))),
        WEAPON_SMITH(arrayOf(MapSet(451, 14))),
        PRIEST(arrayOf(MapSet(0, 2), MapSet(451, -1))),
        BLACKSMITH(arrayOf(MapSet(0, 3), MapSet(451, -1))),
        HUSK(arrayOf(MapSet(204, 5), MapSet(315, -100))),  // ToDo
        ZOMBIE(arrayOf(MapSet(204, -1), MapSet(315, -100)));

        // used earlier (ZombieVillagerMeta)
        private val valueMap = VersionValueMap(values as Array<out MapSet<Int, Int>>, true)


        fun getId(versionId: Int): Int {
            return valueMap[versionId]
        }

        companion object {
            @JvmStatic
            fun byId(id: Int, versionId: Int): VillagerProfessions? {
                for (profession in values()) {
                    if (profession.getId(versionId) == id) {
                        return profession
                    }
                }
                return null
            }
        }

    }

    enum class VillagerTypes {
        DESSERT, JUNGLE, PLAINS, SAVANNA, SNOW, SWAMP, TAIGA;

        companion object {
            private val VILLAGER_TYPES: Array<VillagerTypes> = values()

            @JvmStatic
            fun byId(id: Int): VillagerTypes {
                return VILLAGER_TYPES[id]
            }
        }
    }

    enum class VillagerLevels {
        NOVICE, APPRENTICE, JOURNEYMAN, EXPERT, MASTER;

        companion object {
            private val VILLAGER_LEVELS: Array<VillagerLevels> = values()

            @JvmStatic
            fun byId(id: Int): VillagerLevels {
                return VILLAGER_LEVELS[id]
            }
        }
    }
}
