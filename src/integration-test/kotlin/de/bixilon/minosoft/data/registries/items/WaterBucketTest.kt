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

package de.bixilon.minosoft.data.registries.items

import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.minosoft.data.registries.fluid.fluids.WaterFluid
import de.bixilon.minosoft.data.registries.item.items.bucket.FilledBucketItem
import org.testng.Assert.assertTrue
import org.testng.annotations.Test

@Test(groups = ["item"])
class WaterBucketTest : ItemTest<FilledBucketItem.WaterBucketItem>() {

    init {
        WaterBucketTest0 = this
    }

    fun getWater() {
        super.retrieveItem(FilledBucketItem.WaterBucketItem)
        assertTrue(item is FilledBucketItem.WaterBucketItem)
        assertTrue(item.fluid is WaterFluid)
    }
}

var WaterBucketTest0: WaterBucketTest = unsafeNull()
