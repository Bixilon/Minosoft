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

import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.camera.target.targets.BlockTarget
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.snow.PowderSnowBlock
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.factory.ItemFactory
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.item.items.block.PlaceableItem
import de.bixilon.minosoft.data.registries.item.items.fluid.FluidDrainable
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection


abstract class BucketItem(
    resourceLocation: ResourceLocation,
) : Item(resourceLocation) {

    open class EmptyBucketItem(resourceLocation: ResourceLocation = this.identifier) : BucketItem(resourceLocation), FluidDrainable {

        companion object : ItemFactory<BucketItem> {
            override val identifier = minecraft("bucket")

            override fun build(registries: Registries, data: JsonObject) = EmptyBucketItem()
        }
    }

    open class PowderSnowBucketItem(resourceLocation: ResourceLocation = this.identifier) : BucketItem(resourceLocation), PlaceableItem {
        protected val block: PowderSnowBlock = this::block.inject(PowderSnowBlock)

        override fun getPlacementState(connection: PlayConnection, target: BlockTarget, stack: ItemStack) = block.states.default

        companion object : ItemFactory<BucketItem> {
            override val identifier = minecraft("powder_snow_bucket")

            override fun build(registries: Registries, data: JsonObject) = EmptyBucketItem()
        }
    }
}
