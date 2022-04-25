/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.registries.tweaker

import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.vehicle.*
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions

@Deprecated("Use in ever class", level = DeprecationLevel.ERROR)
object VersionTweaker {

    @JvmStatic
    fun getRealEntityObjectClass(fakeClass: Class<out Entity>, data: Int, versionId: Int): Class<out Entity> {
        if (versionId > ProtocolVersions.V_1_8_9) { // ToDo: No clue here
            return fakeClass
        }
        when (fakeClass) {
            Minecart::class.java -> {
                return when (data) {
                    1 -> ChestMinecart::class.java
                    2 -> FurnaceMinecart::class.java
                    3 -> TNTMinecart::class.java
                    4 -> SpawnerMinecart::class.java
                    5 -> HopperMinecart::class.java
                    6 -> CommandBlockMinecart::class.java
                    else -> fakeClass
                }
            }
        }
        return fakeClass
    }
}
