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

package de.bixilon.minosoft.data.registries.blocks.types.dirt

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.data.registries.blocks.properties.list.MapPropertyList
import de.bixilon.minosoft.data.registries.blocks.properties.primitives.BooleanProperty
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.models.block.state.DirectBlockModel
import de.bixilon.minosoft.gui.rendering.models.block.state.render.BlockRender
import de.bixilon.minosoft.gui.rendering.models.block.state.render.PickedBlockRender
import de.bixilon.minosoft.gui.rendering.models.loader.legacy.ModelChooser
import de.bixilon.minosoft.protocol.versions.Version

abstract class SnowyBlock(identifier: ResourceLocation, settings: BlockSettings) : Block(identifier, settings), ModelChooser {

    override fun bakeModel(context: RenderContext, model: DirectBlockModel) {
        if (context.connection.version.flattened) return super.bakeModel(context, model)

        val normal = model.choose(mapOf(SNOWY to false))?.bake()
        val snowy = model.choose(mapOf(SNOWY to true))?.bake()

        this.model = SnowyRenderer(normal, snowy)
    }

    override fun register(version: Version, list: MapPropertyList) {
        super<Block>.register(version, list)
        list += SNOWY
    }

    class SnowyRenderer(
        val normal: BlockRender?,
        val snowy: BlockRender?,
    ) : PickedBlockRender {
        override val default: BlockRender? get() = normal

        override fun pick(neighbours: Array<BlockState?>): BlockRender? {
            val above = neighbours[Directions.O_UP] ?: return normal
            if (above.block == MinecraftBlocks.SNOW || above.block == MinecraftBlocks.SNOW_BLOCK) return snowy

            return normal
        }
    }


    companion object {
        val SNOWY = BooleanProperty("snowy")
    }
}
