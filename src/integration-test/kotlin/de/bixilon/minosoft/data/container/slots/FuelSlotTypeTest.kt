/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.container.slots

import de.bixilon.minosoft.data.container.ContainerTestUtil
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.items.LavaBucketTest0
import de.bixilon.minosoft.data.registries.items.WaterBucketTest0
import org.testng.Assert.assertFalse
import org.testng.Assert.assertTrue
import org.testng.annotations.Test

@Test(groups = ["container"], dependsOnGroups = ["block", "item"])
class FuelSlotTypeTest {

    fun apple() {
        assertFalse(FuelSlotType.canPut(ContainerTestUtil.createContainer(), 0, ItemStack(TestItem2)))
    }

    fun lavaBucket() {
        assertTrue(FuelSlotType.canPut(ContainerTestUtil.createContainer(), 0, ItemStack(LavaBucketTest0.item)))
    }

    fun waterBucket() {
        assertFalse(FuelSlotType.canPut(ContainerTestUtil.createContainer(), 0, ItemStack(WaterBucketTest0.item)))
    }

    fun coal() {
        assertTrue(FuelSlotType.canPut(ContainerTestUtil.createContainer(), 0, ItemStack(TestItem3)))
    }
}
