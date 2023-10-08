/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.assets.minecraft

import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_15W31A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_16W32A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_18W48A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_15_PRE1
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_16_2_RC1
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_19_4_PRE1
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_20_2_PRE2
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_20W45A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_21W39A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_22W11A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_22W42A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_22W45A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_23W14A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_23W17A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_23W31A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_23W32A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_23W40A
import de.bixilon.minosoft.protocol.versions.Version

object MinecraftAssetsVersion {
    const val LATEST_PACK_FORMAT = 19

    val Version.packFormat: Int
        get() = when {
            versionId < V_15W31A -> 1
            versionId < V_16W32A -> 2
            versionId < V_18W48A -> 3
            versionId < V_1_15_PRE1 -> 4
            versionId < V_1_16_2_RC1 -> 5 // TODO: That should be lower, walls changed earlier
            versionId < V_20W45A -> 6
            versionId < V_21W39A -> 7
            versionId < V_22W11A -> 8
            versionId < V_22W42A -> 9
            versionId < V_22W45A -> 11
            versionId < V_1_19_4_PRE1 -> 12
            versionId < V_23W14A -> 13
            versionId < V_23W17A -> 14
            versionId < V_23W31A -> 15
            versionId < V_23W32A -> 16
            versionId < V_1_20_2_PRE2 -> 17
            versionId < V_23W40A -> 18
            else -> LATEST_PACK_FORMAT
        }

}
