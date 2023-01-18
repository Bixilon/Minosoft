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

package de.bixilon.minosoft.data.registries.item.items.bucket

import de.bixilon.kutil.cast.CastUtil
import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.data.registries.fluid.FluidFactory
import de.bixilon.minosoft.data.registries.fluid.fluids.Fluid
import de.bixilon.minosoft.data.registries.fluid.fluids.flowable.lava.LavaFluid
import de.bixilon.minosoft.data.registries.fluid.fluids.flowable.water.WaterFluid
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.factory.ItemFactory
import de.bixilon.minosoft.data.registries.item.items.fluid.FluidItem
import de.bixilon.minosoft.data.registries.registries.Registries

open class FilledBucketItem<T : Fluid>(
    resourceLocation: ResourceLocation,
    factory: FluidFactory<T>,
) : BucketItem(resourceLocation), FluidItem {
    override val fluid: Fluid = CastUtil.unsafeNull()

    init {
        this::fluid.inject(factory.identifier)
    }

    open class LavaBucketItem(resourceLocation: ResourceLocation = this.identifier) : FilledBucketItem<LavaFluid>(resourceLocation, LavaFluid) {

        companion object : ItemFactory<LavaBucketItem> {
            override val identifier = minecraft("lava_bucket")

            override fun build(registries: Registries, data: JsonObject) = LavaBucketItem()
        }
    }

    open class WaterBucketItem(resourceLocation: ResourceLocation = this.identifier) : FilledBucketItem<WaterFluid>(resourceLocation, WaterFluid) {

        companion object : ItemFactory<WaterBucketItem> {
            override val identifier = minecraft("water_bucket")

            override fun build(registries: Registries, data: JsonObject) = WaterBucketItem()
        }
    }
}
