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

import de.bixilon.minosoft.data.VersionValueMap
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*

data class VillagerData(val type: VillagerTypes, val profession: VillagerProfessions, val level: VillagerLevels) {
    constructor(type: Int, profession: Int, level: Int, versionId: Int) : this(VillagerTypes.values()[type], VillagerProfessions.byId(profession, versionId)!!, VillagerLevels.values()[level])

    enum class VillagerProfessions(val values: Map<Int, Int>) {
        NONE(mapOf(V_18W50A to 0)),
        ARMORER(mapOf(V_18W50A to 1)),
        BUTCHER(mapOf(LOWEST_VERSION_SUPPORTED to 4, V_18W50A to 2)),
        CARTOGRAPHER(mapOf(V_18W50A to 3)),
        CLERIC(mapOf(V_18W50A to 4)),
        FARMER(mapOf(LOWEST_VERSION_SUPPORTED to 0, V_18W50A to 5)),
        FISHERMAN(mapOf(V_18W50A to 6)),
        FLETCHER(mapOf(V_18W50A to 7)),
        LEATHER_WORKER(mapOf(V_18W50A to 8)),
        LIBRARIAN(mapOf(LOWEST_VERSION_SUPPORTED to 1, V_18W50A to 9)),
        MASON(mapOf(V_18W50A to 10)),
        NITWIT(mapOf(LOWEST_VERSION_SUPPORTED to 5, V_18W50A to 11)),
        SHEPHERD(mapOf(V_18W50A to 12)),
        TOOL_SMITH(mapOf(V_18W50A to 13)),
        WEAPON_SMITH(mapOf(V_18W50A to 14)),
        PRIEST(mapOf(LOWEST_VERSION_SUPPORTED to 2, V_18W50A to -1)),
        BLACKSMITH(mapOf(LOWEST_VERSION_SUPPORTED to 3, V_18W50A to -1)),
        HUSK(mapOf(V_1_10_PRE1 to 5, V_1_11 to -100)),  // ToDo
        ZOMBIE(mapOf(V_1_10_PRE1 to -1, V_1_11 to -100));

        // used earlier (ZombieVillagerMeta)
        private val valueMap = VersionValueMap(values)


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
