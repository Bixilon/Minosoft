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

package de.bixilon.minosoft.data.registries.blocks.types.building.snow

import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.blocks.factory.BlockFactory
import de.bixilon.minosoft.data.registries.blocks.light.TransparentProperty
import de.bixilon.minosoft.data.registries.blocks.properties.primitives.IntProperty
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.builder.BlockStateBuilder
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.legacy.FlatteningRenamedModel
import de.bixilon.minosoft.data.registries.blocks.types.properties.item.BlockWithItem
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.collision.CollidableBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.outline.OutlinedBlock
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.item.items.tool.shovel.ShovelRequirement
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.protocol.versions.Version

class SnowLayerBlock(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : Block(identifier, settings), OutlinedBlock, CollidableBlock, FlatteningRenamedModel, ShovelRequirement, BlockWithItem<Item>, AbstractSnowBlock {
    override val item: Item = this::item.inject(identifier)
    override val hardness get() = 0.1f
    override val legacyModelName get() = minecraft("snow_layer")

    override fun buildState(version: Version, settings: BlockStateBuilder): BlockState {
        val layer = settings.properties[LAYERS]?.toInt() ?: return super.buildState(version, settings)

        val collisionShape = AABB(0.0, 0.0, 0.0, 1.0, (layer - 1) * LAYER_HEIGHT, 1.0)
        val outlineShape = AABB(0.0, 0.0, 0.0, 1.0, layer * LAYER_HEIGHT, 1.0)

        return settings.build(block = this, collisionShape = collisionShape, outlineShape = outlineShape, lightProperties = TransparentProperty)
    }


    companion object : BlockFactory<SnowLayerBlock> {
        override val identifier = minecraft("snow")
        private const val LAYER_COUNT = 8
        private const val LAYER_HEIGHT = 1.0 / LAYER_COUNT
        val LAYERS = IntProperty("layers", 1..LAYER_COUNT)


        override fun build(registries: Registries, settings: BlockSettings) = SnowLayerBlock(settings = settings)
    }
}
