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

package de.bixilon.minosoft.gui.rendering.models.block.state.builder

import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.json.JsonUtil.asJsonObject
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperty
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.gui.rendering.models.block.BlockModelPrototype
import de.bixilon.minosoft.gui.rendering.models.block.state.DirectBlockModel
import de.bixilon.minosoft.gui.rendering.models.block.state.apply.BlockStateApply
import de.bixilon.minosoft.gui.rendering.models.block.state.builder.condition.AndCondition
import de.bixilon.minosoft.gui.rendering.models.block.state.builder.condition.BuilderCondition
import de.bixilon.minosoft.gui.rendering.models.block.state.builder.condition.PrimitiveCondition
import de.bixilon.minosoft.gui.rendering.models.loader.BlockLoader
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager

class BuilderBlockModel(
    val parts: List<Apply>,
) : DirectBlockModel {

    override fun choose(properties: Map<BlockProperty<*>, Any>): BlockStateApply? {
        val applies: MutableList<BlockStateApply> = mutableListOf()

        for ((condition, apply) in parts) {
            if (!condition.matches(properties)) continue

            applies += apply
        }

        if (applies.isEmpty()) return null

        return BuilderApply(applies)
    }


    override fun load(textures: TextureManager): BlockModelPrototype {
        for (part in parts) {
            part.apply.load(textures)
        }

        return super.load(textures)
    }

    data class Apply(
        val condition: BuilderCondition,
        val apply: BlockStateApply,
    )

    companion object {

        fun deserialize(loader: BlockLoader, block: Block, data: List<JsonObject>): BuilderBlockModel? {
            val parts: MutableList<Apply> = mutableListOf()

            for (entry in data) {
                val apply = entry["apply"]?.let { BlockStateApply.deserialize(loader, it) } ?: continue

                val condition = entry["when"]?.asJsonObject()?.let { AndCondition.deserialize(block, it) } ?: PrimitiveCondition.TRUE

                parts += Apply(condition, apply)
            }

            if (parts.isEmpty()) return null

            return BuilderBlockModel(parts)
        }
    }
}
