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

package de.bixilon.minosoft.gui.rendering.models.block.state

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.cast.CollectionCast.asAnyList
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperty
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.gui.rendering.models.block.BlockModelPrototype
import de.bixilon.minosoft.gui.rendering.models.block.state.apply.BlockStateApply
import de.bixilon.minosoft.gui.rendering.models.block.state.builder.BuilderBlockModel
import de.bixilon.minosoft.gui.rendering.models.block.state.variant.VariantBlockModel
import de.bixilon.minosoft.gui.rendering.models.loader.BlockLoader
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager

interface DirectBlockModel {

    fun choose(properties: Map<BlockProperty<*>, Any>): BlockStateApply?


    fun load(textures: TextureManager) = BlockModelPrototype(this)

    companion object {

        fun deserialize(loader: BlockLoader, block: Block, data: JsonObject): DirectBlockModel? {
            data["variants"]?.toJsonObject()?.let { return VariantBlockModel.deserialize(loader, block, it) }
            data["multipart"]?.asAnyList()?.let { return BuilderBlockModel.deserialize(loader, block, it.unsafeCast()) }

            return null
        }
    }
}
