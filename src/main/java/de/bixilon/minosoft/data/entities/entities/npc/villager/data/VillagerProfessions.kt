/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.entities.entities.npc.villager.data

import de.bixilon.minosoft.data.VersionValueMap
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.enum.ValuesEnum

enum class VillagerProfessions(val values: Map<Int, Int>) {
    NONE(mapOf(ProtocolVersions.V_18W50A to 0)),
    ARMORER(mapOf(ProtocolVersions.V_18W50A to 1)),
    BUTCHER(mapOf(ProtocolVersions.LOWEST_VERSION_SUPPORTED to 4, ProtocolVersions.V_18W50A to 2)),
    CARTOGRAPHER(mapOf(ProtocolVersions.V_18W50A to 3)),
    CLERIC(mapOf(ProtocolVersions.V_18W50A to 4)),
    FARMER(mapOf(ProtocolVersions.LOWEST_VERSION_SUPPORTED to 0, ProtocolVersions.V_18W50A to 5)),
    FISHERMAN(mapOf(ProtocolVersions.V_18W50A to 6)),
    FLETCHER(mapOf(ProtocolVersions.V_18W50A to 7)),
    LEATHER_WORKER(mapOf(ProtocolVersions.V_18W50A to 8)),
    LIBRARIAN(mapOf(ProtocolVersions.LOWEST_VERSION_SUPPORTED to 1, ProtocolVersions.V_18W50A to 9)),
    MASON(mapOf(ProtocolVersions.V_18W50A to 10)),
    NITWIT(mapOf(ProtocolVersions.LOWEST_VERSION_SUPPORTED to 5, ProtocolVersions.V_18W50A to 11)),
    SHEPHERD(mapOf(ProtocolVersions.V_18W50A to 12)),
    TOOL_SMITH(mapOf(ProtocolVersions.V_18W50A to 13)),
    WEAPON_SMITH(mapOf(ProtocolVersions.V_18W50A to 14)),
    PRIEST(mapOf(ProtocolVersions.LOWEST_VERSION_SUPPORTED to 2, ProtocolVersions.V_18W50A to -1)),
    BLACKSMITH(mapOf(ProtocolVersions.LOWEST_VERSION_SUPPORTED to 3, ProtocolVersions.V_18W50A to -1)),
    HUSK(mapOf(ProtocolVersions.V_1_10_PRE1 to 5, ProtocolVersions.V_1_11 to -100)),  // ToDo
    ZOMBIE(mapOf(ProtocolVersions.V_1_10_PRE1 to -1, ProtocolVersions.V_1_11 to -100));

    // used earlier (ZombieVillagerMeta)
    private val valueMap = VersionValueMap(values)


    fun getId(versionId: Int): Int {
        return valueMap[versionId]
    }

    companion object : ValuesEnum<VillagerProfessions> {
        override val VALUES = values()
        override val NAME_MAP = KUtil.getEnumValues(VALUES)

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
