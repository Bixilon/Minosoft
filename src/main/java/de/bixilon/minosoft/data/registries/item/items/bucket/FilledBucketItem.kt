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

package de.bixilon.minosoft.data.registries.item.items.bucket

import de.bixilon.kutil.cast.CastUtil
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.fluid.Fluid
import de.bixilon.minosoft.data.registries.fluid.FluidFactory
import de.bixilon.minosoft.data.registries.fluid.lava.LavaFluid
import de.bixilon.minosoft.data.registries.fluid.water.WaterFluid
import de.bixilon.minosoft.data.registries.item.factory.ItemFactory
import de.bixilon.minosoft.data.registries.item.items.fluid.FluidItem
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.util.KUtil.minecraft

open class FilledBucketItem<T : Fluid>(
    resourceLocation: ResourceLocation,
    factory: FluidFactory<T>,
) : BucketItem(resourceLocation), FluidItem {
    override val fluid: Fluid = CastUtil.unsafeNull()

    init {
        this::fluid.inject(factory.resourceLocation)
    }

    open class LavaBucketItem(resourceLocation: ResourceLocation = this.resourceLocation) : FilledBucketItem<LavaFluid>(resourceLocation, LavaFluid) {

        companion object : ItemFactory<LavaBucketItem> {
            override val resourceLocation = minecraft("lava_bucket")

            override fun build(registries: Registries) = LavaBucketItem()
        }
    }

    open class WaterBucketItem(resourceLocation: ResourceLocation = this.resourceLocation) : FilledBucketItem<WaterFluid>(resourceLocation, WaterFluid) {

        companion object : ItemFactory<WaterBucketItem> {
            override val resourceLocation = minecraft("water_bucket")

            override fun build(registries: Registries) = WaterBucketItem()
        }
    }
}
