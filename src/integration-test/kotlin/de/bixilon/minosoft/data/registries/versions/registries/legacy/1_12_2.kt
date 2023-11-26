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

package de.bixilon.minosoft.data.registries.versions.registries.legacy

import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.item.ItemEntity
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["pixlyzer"], dependsOnGroups = ["version"], priority = Int.MAX_VALUE, timeOut = 15000L)
class `1_12_2` : LegacyLoadingTest("1.12.2") {

    fun `entity data`() {
        assertEquals(registries.getEntityDataIndex(Entity.FLAGS_DATA), 0)
        assertEquals(registries.getEntityDataIndex(ItemEntity.ITEM_DATA), 6)
        assertEquals(registries.getEntityDataIndex(PlayerEntity.SKIN_PARTS_DATA), 13)
    }

}
